(ns hackerhud.utils
  (:require [clojure.string :as str]
            [goog.object]
            [goog]))

;; Constants

(def tz-str "America/New_York")
(def tz -5)
(def http (js/require "http"))
(def light-sensor-pin js/D35)

;; JS === dumb

(defn obj->clj
  [obj]
  (if (goog.isObject obj)
    (-> (fn [result key]
          (let [v (goog.object/get obj key)]
            (if (= "function" (goog/typeOf v))
              result
              (assoc result key (obj->clj v)))))
        (reduce {} (.getKeys goog/object obj)))
    obj))

;; Espruino Wrappers

(defn serial-setup
  ([serial baud]
   (.setup serial baud)
   nil)
  ([serial baud & rest]
   (.setup serial baud (clj->js (hash-map rest)))
   nil))

(defn serial-println [serial s]
  (.println serial s)
  nil)

(defn serial-print [serial s]
  (.print serial s)
  nil)

(defn serial-write [serial b]
  (.write serial b)
  nil)

;; Clock Utilities

(defn date-suffix [day]
  (case (mod (quot day 10) 10)
    1 "th"
    (case (mod day 10)
      1 "st"
      2 "nd"
      3 "rd"
      "th")))

(def days ["Sunday" "Monday" "Tuesday" "Wednesday" "Thursday" "Friday" "Saturday"])

(defn clock-api [tz]
  (str "http://worldtimeapi.org/api/timezone/" tz))

(defn get-formatted-time []
  (let [date (js/Date.)
        hours24 (.getHours date)
        hours12 (mod hours24 12)
        mins (.getMinutes date)]
    (str (case hours12
           0 12
           hours12)
         ":"
         (cond
           (< mins 10) (str "0" mins)
           :otherwise mins)
         (case (quot hours24 12)
           0 " AM"
           1 " PM"))))

(defn get-day []
  (get days (.getDay (js/Date.))))

(defn get-date []
  (.getDate (js/Date.)))

;; HTTP Utilities

(def !response (atom nil))

(defn get-uri [uri cb]
  (doto http
    (.get uri (fn [res]
                (do
                  (reset! !response nil)
                  (.on res "data" #(swap! !response str %))
                  (.on res "close" (fn []
                                     (swap! !response (.-parse js/JSON))
                                     (cb))))))))
;; HTTP-Time Utilities

(defn update-time []
  (let [resp @!response
        unixtime (.-unixtime resp)
        offset (-> (.-utc_offset resp)
                   (str/split #":")
                   first
                   int)]
    (.setTimeZone js/E offset)
    (js/setTime unixtime)))

(defn set-new-time
  "Perform a GET to a clock api to update system time"
  []
  (get-uri (clock-api tz-str) update-time))

;; Sensors

(defn get-light []
  (if-let [value (js/analogRead light-sensor-pin)]
    value
    0))

;; Weather

(def city "Tampa")
(goog-define weather-api-key "")
(def current-weather (atom nil))

(defn weather-api [query api-key]
  (str "http://api.openweathermap.org/data/2.5/weather?q="
       query
       "&appid="
       api-key))

(defn K-to-F [K]
  (- (* K (/ 9 5)) 459.67))

(defn update-weather []
  (let [resp @!response
        weather (get (.-weather resp) 0)
        main (.-main resp)
        now (.now js/Date)]
    (reset! current-weather
            {:description (.-main weather)
             :feels-like (.round js/Math (K-to-F (.-feels_like main)))})))

(defn set-new-weather
  "Perform a GET to the weather API to update current weather"
  []
  (get-uri (weather-api city weather-api-key) update-weather))

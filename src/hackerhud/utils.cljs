(ns hackerhud.utils
  (:require [clojure.string :as str]))

;; Constants

(def tz-str "America/New_York")
(def tz -5)
(def http (js/require "http"))

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

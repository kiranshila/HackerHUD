(ns hackerhud.core
  (:require [esprit.repl] ;Enable networked REPL
            [hackerhud.vfd :as vfd]
            [hackerhud.utils :as utils]
            [hackerhud.time :as time]
            [hackerhud.weather :as weather]
            [hackerhud.alerts :as alerts]
            [clojure.string :as str])
  (:require-macros [hackerhud.macros :refer [set-interval
                                             set-timeout]]))

; Configure network objects
(def wifi (js/require "Wifi"))

;; These get set by secrets.edn
(goog-define pass "")
(goog-define ssid "")

;; Atom to hold connection state
(def connected? (atom nil))

(vfd/setup)

; Pages

(defn day-page []
  (let [date (time/get-date)
        day (time/get-day)
        month (time/get-month)]
    (vfd/display (str "Today is " day "\n\r"
                      month " the " date (time/date-suffix date)) 0 0)))

(defn time-page []
  (vfd/display (time/get-formatted-time) 6 0))

(defn weather-page []
  (when-let [weather @weather/current-weather]
    (vfd/display (str (:description weather)
                      "\n\rFeels like "
                      (:feels-like weather)
                      (char weather/degree)) 0 0)))

(defn weather-2-page []
  (when-let [weather @weather/current-weather]
    (vfd/display (str "UV: " (:uvi weather) "\n\r"
                      "AQ: " (:aqi weather)) 0 0)))

(def pages [day-page time-page weather-page weather-2-page])
(def current-page (atom 0))

(defn update-page []
  (when @connected?
    (vfd/command :clear)
    (let [page @current-page
          alerts @alerts/alerts]
      (if-let [[alert & xs] (seq alerts)]
        (do
          (vfd/display alerts/right-chevron 0 0)
          (vfd/display (:message alert) 2 0)
          (vfd/display alerts/left-chevron 19 0)
          (when-let [contents (:contents alert)]
            (vfd/display (:contents alert) 0 1))
          (reset! alerts/alerts (into [] xs)))
        (do
          ((get pages page))
          (if (= (count pages) (inc page))
            (reset! current-page 0)
            (swap! current-page inc)))))))

(defn update-brightness []
  (let [light (utils/get-light)]
    (cond
      (> light 0.1) (vfd/set-brightness 4)
      :finally (vfd/set-brightness 1))))

(defn update-apis []
  (when @connected?
    (.. (weather/get-new-weather)
        (then weather/get-new-aqi))))

;; Set the update fn to trigger on alerts
(reset! alerts/update-page-cb update-page)

;; Startup the timers
(set-interval update-brightness 1000)
(set-interval update-page 20000)
(set-interval update-apis 60000)

(defn on-connect [_]
  (vfd/display (str "IP: " (.-ip (.getIP wifi))) 0 1)
  (.. (time/get-new-time)
      (then weather/get-new-weather)
      (then weather/get-new-aqi))
  (reset! connected? true))

; This needs a timeout so it happens after the event loop actually starts
(set-timeout #(.connect wifi ssid #js{:password pass}) 0)

; When wifi connects, update system time
(.on wifi "connected" on-connect)

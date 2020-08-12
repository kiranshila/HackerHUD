(ns hackerhud.core
  (:require [esprit.repl] ;Enable networked REPL
            [hackerhud.vfd :as vfd]
            [hackerhud.utils :as utils])
  (:require-macros [hackerhud.macros :refer [set-interval]]))

; Configure network objects
(def wifi (js/require "Wifi"))

;; These get set by secrets.edn
(goog-define pass "")
(goog-define ssid "")

#_(.connect wifi ssid #js{:password pass})

; Init the VFD
(vfd/setup)

; Use light to update vfd brightness
(defn update-brightness []
  (let [light (utils/get-light)]
    (cond
      (> light 0.1) (vfd/set-brightness 4)
      :finally (vfd/set-brightness 1))))

; Update brightness every second
(set-interval update-brightness 1000)

; When wifi connects, update system time
(.on wifi "connected" (do (utils/set-new-time)
                          (utils/set-new-weather)))

; Pages

(defn day-page []
  (let [date (utils/get-date)
        day (utils/get-day)]
    (vfd/display (str "Today is") 0 0)
    (vfd/display (str day " the " date (utils/date-suffix date)) 0 1)))

(defn time-page []
  (vfd/display (utils/get-formatted-time) 6 0))

(defn weather-page []
  (let [weather @utils/current-weather]
    (vfd/display (str "Outside is "
                      (:description weather)) 0 0)
    (vfd/display (str "Feels like "
                      (:feels-like weather) "F") 0 1)))

; Update weather every 15 min
(set-interval utils/set-new-weather 900000)

(def pages [day-page time-page weather-page])
(def current-page (atom 0))

(defn update-page []
  (let [page @current-page]
    (vfd/command :clear)
    ((get pages page))
    (if (= (count pages) (inc page))
      (reset! current-page 0)
      (swap! current-page inc))))

; Update the page every 30 sec
(set-interval update-page 30000)

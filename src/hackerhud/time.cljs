(ns hackerhud.time
  (:require [hackerhud.utils :as utils]
            [clojure.string :as str]))

(def tz-str "America/New_York")
(def tz -5)

(defn date-suffix [day]
  (case (mod (quot day 10) 10)
    1 "th"
    (case (mod day 10)
      1 "st"
      2 "nd"
      3 "rd"
      "th")))

(def days ["Sunday" "Monday" "Tuesday" "Wednesday" "Thursday" "Friday" "Saturday"])
(def months ["January" "February" "March" "April" "May" "June" "July" "August" "September" "October" "November" "December"])

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

(defn get-month []
  (get months (.getMonth (js/Date.))))

(defn get-date []
  (.getDate (js/Date.)))

(defn update-time [resp]
  (let [resp (.-state resp)
        unixtime (.-unixtime resp)
        offset (-> (.-utc_offset resp)
                   (str/split #":")
                   first
                   int)]
    (.setTimeZone js/E offset)
    (js/setTime unixtime)))

(defn get-new-time
  "Perform a GET to a clock api to update system time"
  []
  (.. (utils/get-uri (clock-api tz-str))
      (then update-time)))

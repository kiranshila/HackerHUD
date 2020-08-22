(ns hackerhud.alerts
  (:require [cljs.reader :refer [read-string]]))

; Alert string is at most 16 characters
; Contents is full 20

(def dgram (js/require "dgram"))
(def alert-port 6969)
(def alerts (atom []))

(def right-chevron (char 0xAF))
(def left-chevron (char 0xAE))

(def sckt (.bind (.createSocket dgram "udp4") alert-port (fn [] (println (str "Ready for UDP Alerts on: " alert-port)))))

(defn print-edn-alert [s]
  (try
    (let [alert (:alert (read-string s))]
      (swap! alerts conj alert))
    (catch :default ex
      (.log js/console "Could not parse EDN"))))

; Parse alerts on every incoming message
(.on sckt "message" print-edn-alert)

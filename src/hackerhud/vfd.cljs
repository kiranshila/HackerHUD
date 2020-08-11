(ns hackerhud.vfd
  (:require [hackerhud.utils :as utils]))

; Which serial obj we will use for VFD communication
(def serial js/Serial2)
(def tx-pin js/D4)

(def commands {:init #js[0x1B 0x40]
               :clear 0x0C
               :move #js[0x1B 0x6C]
               :brightness #js[0x1B 0x2A]
               :cursor-down 0x0A
               :cursor-right 0x09
               :cursor-home 0x0B})

(defn command [cmd]
  (utils/serial-write serial (cmd commands)))

(defn set-cursor [x y]
  (command :move)
  (utils/serial-write serial (inc x))
  (utils/serial-write serial (inc y)))

(defn set-brightness
  "Set brightness level 1 to 4"
  [n]
  (command :brightness)
  (utils/serial-write serial n))

(defn display
  ([s] (utils/serial-print serial s))
  ([s x y] (set-cursor x y) (display s)))

(defn setup []
  (utils/serial-setup serial 9600 :tx tx-pin :rx js/D15)
  (command :init)
  (command :clear)
  (display "Hello from CLJS!" 2 0))

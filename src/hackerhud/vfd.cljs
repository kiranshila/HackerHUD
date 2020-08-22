(ns hackerhud.vfd
  (:require [hackerhud.utils :as utils]
            [esprit.board :as board]))

(def serial (if-let [s (::serial board/items)]
              s
              (println "NOTE: hackerhud.vfd/serial not setup in boards.edn")))

(def commands {:init #js[0x1B 0x40]
               :clear 0x0C
               :overwrite-mode #js[0x1B 0x11]
               :vertical-scroll-mode #js[0x1B 0x12]
               :horizontal-scroll-mode #js[0x1B 0x13]
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
  (command :init)
  (command :clear)
  (display "Hello from CLJS!" 2 0))

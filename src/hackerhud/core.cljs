(ns hackerhud.core
  (:require [esprit.repl]))

; Configure network objects
(def http (js/require "http"))
(def wifi (js/require "Wifi"))
(def ntp-server "us.pool.ntp.org")
(def tz-offset (str -5))

;; These get set by secrets.edn
(goog-define pass "")
(goog-define ssid "")

(.connect wifi ssid #js{:password pass})
(.setSNTP wifi ntp-server tz-offset)

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

; VFD Configuration
(def vfd-serial js/Serial2)

(def vfd-commands {:init 0x1B40
                   :clear 0x0C
                   :move 0x1B6C
                   :brightness 0x1B2A
                   :cursor-down 0x0A
                   :cursor-right 0x09
                   :cursor-home 0x0B})

(defn write-command [cmd]
  (serial-write vfd-serial (cmd vfd-commands)))

(defn set-cursor [x y]
  (write-command :cursor-home)
  (serial-print vfd-serial (.repeat "\t" x))
  (serial-print vfd-serial (.repeat "\n" y)))

(defn set-brightness [n]
  "Set brightness level 1 to 4"
  (serial-write vfd-serial (:brightness vfd-commands))
  (serial-write vfd-serial n))

(defn vfd-print
  ([s] (serial-print vfd-serial s))
  ([s x y] (set-cursor x y) (vfd-print s)))

(defn vfd-setup []
  (doto vfd-serial
    (serial-setup 9600 :tx js/D4 :rx js/D15)
    (serial-write (:init vfd-commands))
    (serial-write (:clear vfd-commands)))
  (vfd-print "Hello from CLJS!" 2 0))

; Init the VFD
(vfd-setup)

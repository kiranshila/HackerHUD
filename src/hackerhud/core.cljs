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

(ns hackerhud.macros)

(defmacro set-interval [cb timeout]
  "For calling js/setInterval on named fns, to allow for dynamic redefinition"
  `(js/setInterval (fn [] (~cb)) ~timeout))

(defmacro set-timeout [cb timeout]
  "For calling js/setTimeout on named fns"
  `(js/setTimeout (fn [] (~cb)) ~timeout))

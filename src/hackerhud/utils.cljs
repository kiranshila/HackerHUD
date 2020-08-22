(ns hackerhud.utils
  (:require [clojure.string :as str]
            [esprit.board :as board]))

;; Constants
(def http (js/require "http"))
(def light-sensor (::light-sensor board/items))

;; Espruino Wrappers

(defn serial-setup
  ([serial baud]
   (.setup serial baud))
  ([serial baud & rest]
   (.setup serial baud (clj->js (hash-map rest)))))

(defn serial-println [serial s]
  (.println serial s))

(defn serial-print [serial s]
  (.print serial s))

(defn serial-write [serial b]
  (.write serial b))

;; HTTP Utilities
(defn get-uri [uri]
  (js/Promise.
   (fn [resolve reject]
     (let [response (atom nil)]
       (.get http uri (fn [res]
                        (do
                          (reset! response nil)
                          (.on res "data" #(swap! response str %))
                          (.on res "close" (fn []
                                             (swap! response (.-parse js/JSON))
                                             (resolve response))))))))))

(defn get-light []
  (or (js/analogRead light-sensor) 0))

(defn clear-interval
  ([] (js/clearInterval))
  ([id] (js/clearInterval id)))

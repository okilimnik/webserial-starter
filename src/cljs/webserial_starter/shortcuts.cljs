(ns webserial-starter.shortcuts
  (:require
   [applied-science.js-interop :as j]
   [clojure.string :as str]))

(defn key-combo [e]
  (let [k (j/get e :key)]
    (->> [(when (or (j/get e :metaKey) (= k "Meta")) "META")
          (when (or (j/get e :ctrlKey) (= k "Ctrl")) "CTRL")
          (when (or (j/get e :altKey) (= k "Alt")) "ALT")
          (when (or (j/get e :shiftKey) (= k "Shift")) "SHIFT")
          (when (and (not= k "Meta")
                     (not= k "Shift")
                     (not= k "Ctrl")
                     (not= k "Alt"))
            (str/upper-case k))]
         (filter identity)
         (str/join "+"))))

(def shortcuts
  (if (> (j/call (j/get js/navigator :appVersion) :indexOf "Mac") 0)
    {"META+K" :CLEAR
     "META+ENTER" :IGNORE_LF
     "ENTER" :SEND
     "ARROWUP" :UP
     "ARROWDOWN" :DOWN
     "META+D" :TOGGLE_CONNECTION}
    {"CTRL+L" :CLEAR
     "CTRL+ENTER" :IGNORE_LF
     "ENTER" :SEND
     "ARROWUP" :UP
     "ARROWDOWN" :DOWN
     "CTRL+D" :TOGGLE_CONNECTION}))
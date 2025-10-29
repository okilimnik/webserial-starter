(ns webserial-starter.shortcuts
  (:require 
   [clojure.string :as str]))

(defn key-combo [e]
  (->> [(when (or (.-metaKey e) (= (.-key e) "Meta")) "META")
        (when (or (.-ctrlKey e) (= (.-key e) "Ctrl")) "CTRL")
        (when (or (.-altKey e) (= (.-key e) "Alt")) "ALT")
        (when (or (.-shiftKey e) (= (.-key e) "Shift")) "SHIFT")
        (when (and (not= (.-key e) "Meta")
                   (not= (.-key e) "Shift")
                   (not= (.-key e) "Ctrl")
                   (not= (.-key e) "Alt"))
          (.toUpperCase (.-key e)))]
       (filter identity)
       (str/join "+")))

(def shortcuts
  (if (> (.indexOf (.-appVersion js/navigator) "Mac") 0)
    {:CLEAR "META+K"
     :IGNORE_LF "META+ENTER"
     :SEND "ENTER"
     :UP "ARROWUP"
     :DOWN "ARROWDOWN"
     :TOGGLE_CONNECTION "META+D"}
    {:CLEAR "CTRL+L"
     :IGNORE_LF "CTRL+ENTER"
     :SEND "ENTER"
     :UP "ARROWUP"
     :DOWN "ARROWDOWN"
     :TOGGLE_CONNECTION "CTRL+D"}))
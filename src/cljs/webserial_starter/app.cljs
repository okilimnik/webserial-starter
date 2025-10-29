(ns webserial-starter.app
  (:require
   [webserial-starter.ascii-encoder :refer [encode-with-html decode]]
   [webserial-starter.shortcuts :refer [shortcuts key-combo]]))

(defn input-keyup [e]
  (when-not (or (= (.-key e) "ArrowUp")
                (= (.-key e) "ArrowDown"))
    (reset! wip (.. e -target -value))))

(defn console-scroll [e]
  (let [scroll-point (+ (.. e -target -scrollTop)
                        (.. e -target -clientHeight))
        scrolled-to-bottom? (>= (+ scroll-point 10)
                                (.. e -target -scrollHeight))]
    (reset! scrolled-to-bottom scrolled-to-bottom?)))

(defn set-input-value! [target value]
  (set! (.-selectionStart target) 0)
  (set! (.-selectionEnd target) (.. target -value -length))
  (.execCommand target "insertText" false value))

(defn input-keydown [e history history-index]
  (case (key-combo e)
    :CLEAR
    (do
      (.preventDefault e)
      (reset! (-> connection :messages) [])
      true)

    :IGNORE_LF
    (do
      (.execCommand js/document "insertText" false "␊")
      true)

    :SEND
    (let [target-value (.. e -target -value)
          cmd (str @prepend target-value @append)]
      (.preventDefault e)
      (when-not (= (peek @history) target-value)
        (swap! history conj target-value))
      (reset! history-index (count @history))
      (reset! wip "")
      (write! connection (decode cmd))
      (set-input-value! (.-target e) "")
      true)

    :UP
    (when-not (zero? @history-index)
      (swap! history-index dec)
      (set-input-value! (.-target e) (nth @history @history-index))
      true)

    :DOWN
    (do
      (swap! history-index inc)
      (if (>= @history-index (count @history))
        (do
          (set-input-value! (.-target e) @wip)
          (reset! history-index (count @history))
          true)
        (when (< @history-index (count @history))
          (set-input-value! (.-target e) (nth @history @history-index))
          true)))

    nil)) ;; default case - return nil if no shortcuts matched

(defn render-app [{:keys [content]}]
  (let  [{:keys [connection supported? history history-index]} content
         connection-state @(:state connection)]
    [:div

     [:main#console
      {:on {:scroll console-scroll}}
      [:section#output.newlines
       (for [message (:messages connection-state)]
         [:pre {:innerHTML (encode-with-html message)}])]]

     [:footer
      [:webserial/toolbar
       {:prepend ""
        :on-prepend-change #()
        :append ""
        :on-append-change #()}]
      [:webserial/ascii-input
       {:id "input"
        :on-change #()
        :on-key-up input-keyup
        :placeholder "Enter data. Press RETURN to send!"
        :interceptor [:input-keydown]}]
      [:div#attribution
       "© Oleh Kylymnyk"
       [:a {:rel "license"
            :href "http://creativecommons.org/licenses/by-nc-sa/4.0/"}
        [:img {:alt "Creative Commons License"
               :style {:border-width 0}
               :src "https://i.creativecommons.org/l/by-nc-sa/4.0/80x15.png"}]]
       "This work is licensed under a "
       [:a {:rel "license"
            :href "http://creativecommons.org/licenses/by-nc-sa/4.0/"}
        "Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License"]
       "."]]

     (when-not supported?
       [:div#na
        [:h1 "This Web Browser does not support the WebSerial API."]
        [:p "Maybe consider using Chrome for this?"]])]))


(ns webserial-starter.ui
  (:require
   [webserial-starter.actions :refer [interceptor]]
   [webserial-starter.ascii-encoder :refer [encode-with-html]]))

(defn render-app [{:keys [connection new-lines?] :as state}]
  (let  [{:keys [input messages]} connection]
    [:div#app

     [:header
      [:h1 "WebSerial"]
      [:span {:id "cred"} " by "
       [:a {:href "https://github.com/okilimnik/webserial-starter"
            :target "_blank"} "Oleh Kylymnyk"]]
      [:aside
       [:label {:for "checkbox"} "New Lines"]
       [:input {:id "checkbox"
                :type "checkbox"
                :checked new-lines?
                :on {:change [[:new-lines/change [:event.target/checked]]]}}]]
      [:webserial/connect-modal state]]

     [:main#console
      {:on {:scroll (fn [e] [:main/console-scroll e])}}
      [:section#output.newlines
       (for [message messages]
         [:pre {:innerHTML (encode-with-html message)}])]]

     [:footer
      [:webserial/toolbar state]
      [:webserial/ascii-input
       {:id "input"
        :on-change #()
        :content input
        :on-key-up (fn [e] [:footer/input-keyup e])
        :placeholder "Enter data. Press RETURN to send!"
        :interceptor (partial interceptor state)}]
      [:div#attribution.flex.justify-center
       [:div "© Oleh Kylymnyk"]
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

     (when-not (.-serial js/navigator)
       [:div#na
        [:h1 "This Web Browser does not support the WebSerial API."]
        [:p "Maybe consider using Chrome for this?"]])]))


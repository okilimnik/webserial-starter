(ns webserial-starter.ui
  (:require
   [webserial-starter.actions :refer [interceptor]]
   [webserial-starter.ascii-encoder :refer [encode encode-with-html]]
   [webserial-starter.utils :refer [class-names]]))

(defn render-app [{:keys [input connection new-lines?] :as state}]
  (let  [{:keys [messages]} connection]
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
      [:section#output {:class (class-names {:newlines new-lines?})}
       (for [[idx message] (map-indexed vector messages)]
         [:pre {:replicant/key idx
                :innerHTML (encode-with-html message)}])]]

     [:footer
      [:webserial/toolbar state]
      [:webserial/ascii-input
       {:id "input"
        :on-change [[:input/change [:event.target/value]]]
        :content (encode input)
        :on-key-down [[:input/on-key-down [:dom/event] [:event/key]]]
        :on-key-up [[:input/keyup [:event/key] [:event.target/value]]]
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


(ns webserial-starter.ui.ascii-input
  (:require
   [replicant.alias :as alias]
   [webserial-starter.ascii-encoder :refer [encode-with-html]]))

(defn ascii-input [{:keys [id content on-key-up max-length on-change placeholder on-key-down] :or {on-key-down [[:ascii-input/on-key-down [:dom/event] [:event/key]]]}}]
  [:div.ascii-input-wrap {:id id}
   [:pre {:innerHTML (when content (encode-with-html content))}]
   [:textarea
    (merge
     {:value content
      :on (merge
           {:input on-change
            :keydown on-key-down}
           (when on-key-up
             {:keyup on-key-up}))
      :spellCheck false
      :autoCapitalize "off"
      :autoComplete "off"
      :autoCorrect "off"
      :placeholder placeholder}
     (when max-length
       {:max-length max-length}))]])

(alias/register! :webserial/ascii-input ascii-input)
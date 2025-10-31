(ns webserial-starter.ui.ascii-input
  (:require
   [replicant.alias :as alias]
   [webserial-starter.ascii-encoder :refer [encode-with-html]]))

(defn on-key-down [interceptor e]
  (prn (.-key e))
  (when-not (and interceptor (interceptor e))
    (case (.-key e)
      "Tab" (do (.preventDefault e)
                (js/document.execCommand "insertText" false \␋))
      "Enter" (do (.preventDefault e)
                  (js/document.execCommand "insertText" false \␊))
      nil)))

(defn ascii-input [{:keys [id content on-key-up max-length on-change placeholder interceptor]}]
  [:div.ascii-input-wrap {:id id}
   [:pre {:innerHTML (when content (encode-with-html content))}]
   [:textarea
    (merge
     {:value content
      :on (merge
           {:input on-change
            :keydown (partial on-key-down interceptor)}
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
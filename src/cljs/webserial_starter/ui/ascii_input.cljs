(ns webserial-starter.ui.ascii-input 
  (:require
   [replicant.alias :as alias]))

(defn ascii-input [{:keys [id content max-length on-change on-key-down placeholder]}]
  (let [{:keys [input-data displayed-input]} content]
    [:div.ascii-input-wrap
     [:pre {:id id :innerHTML displayed-input}]
     [:textarea
      {:value input-data
       :on {:key-down on-key-down
            :change on-change}
       :spellCheck false
       :autoCapitalize "off"
       :autoComplete "off"
       :autoCorrect "off"
       :placeholder placeholder
       :maxLength max-length}]]))

(alias/register! :webserial/ascii-input ascii-input)
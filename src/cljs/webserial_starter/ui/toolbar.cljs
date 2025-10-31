(ns webserial-starter.ui.toolbar
  (:require
   [replicant.alias :as alias]))

(defn toolbar [state]
  (let [{:keys [prepend append]} state]
    [:div#toolbar {:on {:click [[:toolbar/click]]}}
     [:button "␀"]
     [:button "␁"]
     [:button "␂"]
     [:button "␃"]
     [:button "␄"]
     [:button "␅"]
     [:button "␆"]
     [:button "␇"]
     [:button "␈"]
     [:button "␉"]
     [:button "␊"]
     [:button "␋"]
     [:button "␌"]
     [:button "␍"]
     [:button "␎"]
     [:button "␏"]
     [:button "␐"]
     [:button "␑"]
     [:button "␒"]
     [:button "␓"]
     [:button "␔"]
     [:button "␕"]
     [:button "␖"]
     [:button "␗"]
     [:button "␘"]
     [:button "␙"]
     [:button "␚"]
     [:button "␛"]
     [:button "␜"]
     [:button "␝"]
     [:button "␞"]
     [:button "␟"]
     [:button "␡"]
     [:span
      [:label {:for "ascii-input-prepend"} "PREPEND"]
      [:webserial/ascii-input {:id "ascii-input-prepend"
                               :max-length 5
                               :content prepend
                               :on-change #()}]]
     [:span
      [:label {:for "ascii-input-append"} "APPEND"]
      [:webserial/ascii-input {:id "ascii-input-append"
                               :max-length 5
                               :content append
                               :on-change #()}]]]))

(alias/register! :webserial/toolbar toolbar)
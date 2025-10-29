(ns webserial-starter.toolbar
  (:require
   [replicant.alias :as alias]))

(defn handle-toolbar-click [e]
  (when (= "BUTTON" (.. e -target -tagName))
    (.preventDefault e)
    (let [textarea (.querySelector js/document "#input textarea")]
      (.focus textarea)
      (js/setTimeout #(.execCommand js/document "insertText" false (.. e -target -textContent)) 10))))

(defn toolbar [{:keys [content on-prepend-change on-append-change]}]
  (let [{:keys [prepend append]} content]
    [:div#toolbar {:on {:click handle-toolbar-click}}
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
      [:webserial/ascii-input {:id "prepend"
                               :max-length 5
                               :content prepend
                               :on-change on-prepend-change}]]
     [:span
      [:label {:for "ascii-input-append"} "APPEND"]
      [:webserial/ascii-input {:id "append"
                               :max-length 5
                               :content append
                               :on-change on-append-change}]]]))

(alias/register! :webserial/toolbar toolbar)
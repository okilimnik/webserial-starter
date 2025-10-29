(ns webserial-starter.scenes
  (:require
   [portfolio.replicant :refer-macros [defscene]]
   [webserial-starter.ascii-input]
   [webserial-starter.connect-modal]
   [webserial-starter.toolbar]
   [portfolio.ui :as portfolio]
   [webserial-starter.ascii-encoder :refer [encode-with-html]]
   [webserial-starter.stores.connection :as connection]))

(defscene connect-modal
  :params (atom {:connection (connection/make-store)})
  [store]
  [:webserial/options-component {:content @store}])

(defscene toolbar
  :params (atom {:connection (connection/make-store)
                 :prepend {:input-data ""
                           :displayed-input ""}
                 :append {:input-data \n
                          :displayed-input "<x class=\"LF\">␊</x><br>"}})
  [store]
  [:webserial/toolbar {:content @store
                       :on {:append-change (fn [e]
                                             (let [data (.. e -target -value)]
                                               (swap! store #(-> %
                                                                 (assoc :input-data data)
                                                                 (assoc :displayed-input (encode-with-html data))))))
                            :prepend-change (fn [e]
                                              (let [data (.. e -target -value)]
                                                (swap! store #(-> %
                                                                  (assoc :input-data data)
                                                                  (assoc :displayed-input (encode-with-html data))))))}}])


(defn main []
  (portfolio/start!
   {:config
    {:css-paths ["/styles.css"]
     :viewport/defaults
     {:background/background-color "#fdeddd"}}}))
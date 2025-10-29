(ns webserial-starter.core
  (:require
   [nexus.registry :as nxr]
   [replicant.dom :as r]
   [webserial-starter.actions]
   [webserial-starter.ui.ascii-input]
   [webserial-starter.ui.connect-modal]
   [webserial-starter.ui.toolbar]
   [webserial-starter.ui :refer [render-app]]
   [webserial-starter.stores.connection :as connection]))

(defn main [store el]
  (add-watch
   store ::render
   (fn [_ _ _ state]
     (r/render el (render-app state))))

  (r/set-dispatch!
   (fn [dispatch-data actions]
     (nxr/dispatch store dispatch-data actions)))

  ;; Trigger the initial render
  (swap! store merge {:connection (connection/make-store)}))
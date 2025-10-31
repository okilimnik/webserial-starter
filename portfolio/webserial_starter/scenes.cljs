(ns webserial-starter.scenes
  (:require
   [dataspex.core :as dataspex]
   [nexus.registry :as nxr]
   [portfolio.replicant :refer-macros [defscene]]
   [portfolio.ui :as portfolio]
   [replicant.dom :as r]
   [webserial-starter.actions]
   [webserial-starter.db :refer [store]]
   [webserial-starter.ui.ascii-input]
   [webserial-starter.ui.connect-modal]
   [webserial-starter.ui.toolbar]))

(r/set-dispatch!
 (fn [dispatch-data actions]
   (nxr/dispatch store dispatch-data actions)))

(dataspex/inspect "App state" store)

(defscene connect-modal
  :params store
  [store]
  [:webserial/connect-modal @store])

(defscene toolbar
  :params store
  [store]
  [:webserial/toolbar @store])


(defn main []
  (portfolio/start!
   {:config
    {:css-paths ["/styles.css"]
     :viewport/defaults
     {:background/background-color "#fdeddd"}}}))
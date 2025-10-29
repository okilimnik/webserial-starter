(ns webserial-starter.dev
  (:require
   [dataspex.core :as dataspex]
   [nexus.action-log :as action-log]
   [webserial-starter.core :as app]
   [webserial-starter.stores.db :refer [store]]))

(defonce el (js/document.getElementById "app"))
(dataspex/inspect "App state" store)
(action-log/inspect)

(defn ^:dev/after-load main []
  ;; Add additional dev-time tooling here
  (app/main store el))
(ns webserial-starter.prod
  (:require
   [webserial-starter.core :as app]
   [webserial-starter.db :refer [store]]))

(defn ^:dev/after-load main []
  (app/main store (js/document.getElementById "app-container")))
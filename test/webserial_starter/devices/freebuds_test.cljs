(ns webserial-starter.devices.freebuds-test
  (:require
   [cljs.test :refer [deftest is testing]]
   [webserial-starter.devices.freebuds :as sut]))

(deftest encode-test
  (is (= "" (sut/encode {:command-id "+␄"}))))
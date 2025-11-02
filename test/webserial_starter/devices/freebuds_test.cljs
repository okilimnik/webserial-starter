(ns webserial-starter.devices.freebuds-test
  (:require
   [cljs.test :refer [deftest is testing]]
   [webserial-starter.ascii-encoder :refer [decode]]
   [webserial-starter.devices.freebuds :as sut]))

(deftest encode-test
  (testing "Noise cancelling on"
    (is (= "5a0006002b040101017800" (.toHex (sut/encode (decode "+␄␁␁␁"))))))
  (testing "Noise cancelling off"
    (is (= "5a0006002b040101006821" (.toHex (sut/encode (decode "+␄␁␁␀")))))))
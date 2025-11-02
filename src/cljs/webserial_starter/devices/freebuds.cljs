(ns webserial-starter.devices.freebuds
  (:require
   ["@finwo/crc16-xmodem" :refer [crc16b]]
   [applied-science.js-interop :as j]))

;; https://mmk.pw/en/posts/freebuds-4i-proto/

(def DEVICE-ID "00001101-0000-1000-8000-00805f9b34fb")

(def CMD_DESCRIPTORS
  {"0107" "DEVICE_INFO"
   "0108" "BATTERY_INFO"
   "011f" "SET_DOUBLE_TAP"
   "0120" "GET_DOUBLE_TAP"
   "0127" "BATTERY_STATE_CHANGED"
   "012d" "GET_TOUCH_PAD"
   "2b03" "IN_EAR_CHANGED"
   "2b04" "SET_ANC_MODE"
   "2b10" "SET_AUTO_PAUSE"
   "2b11" "GET_AUTO_PAUSE"
   "2b16" "SET_LONG_TAP"
   "2b17" "GET_LONG_TAP"
   "2b18" "SET_ANC_PREF"
   "2b19" "GET_ANC_PREF"
   "2b2a" "CURRENT_ANC_MODE"
   "0a0d" "LOG_SPAM"
   "0c02" "LIST_LANGUAGES"})

(defn encode [{:keys [command-id parameters]}]
  (try
    (let [build-param (fn [[p-type p-value]]
                        (let [type-byte (js/Buffer.from [(js/Number p-type)])
                              len-byte (js/Buffer.from [(count p-value)])
                              val-bytes (js/Buffer.from p-value)]
                          (js/Buffer.concat [type-byte len-byte val-bytes])))

          body (js/Buffer.concat
                (cons (js/Buffer.from command-id)
                      (map build-param parameters)))

          header (js/Buffer.from
                  (concat ["Z"]  ;; Start marker
                          [(+ (.-length body) 1)]  ;; Length + 1
                          [0]))  ;; \x00 byte
          
          message (js/Buffer.concat [header body])
          checksum (js/Buffer.from (crc16b message))
          result (js/Buffer.concat [message checksum])]
      result)
    (catch js/Error e :failed)))
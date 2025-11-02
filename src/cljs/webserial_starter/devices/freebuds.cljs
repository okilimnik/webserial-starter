(ns webserial-starter.devices.freebuds
  (:require
   ["@finwo/crc16-xmodem" :refer [crc16b]]
   [applied-science.js-interop :as j]
   [webserial-starter.utils :refer [decoder encoder]]))

;; https://mmk.pw/en/posts/freebuds-4i-proto/

(def DEVICE-ID "00001101-0000-1000-8000-00805f9b34fb")

(defn encode [cmd]
  (let [command-id (subs cmd 0 2)
        parameters (subs cmd 2)
        built-params (j/call encoder :encode parameters)
        _ (prn "built-params: " built-params)
        built-command (j/call encoder :encode command-id)
        _ (prn "built-command: " built-command)

        body (js/Uint8Array. (+ (.-length built-command) (.-length built-params)))
        _ (.set body built-command)
        _ (.set body built-params (.-length built-command))

        buffer (js/ArrayBuffer. 4)
        _ (.setInt8 (js/DataView. buffer) 0 0x5A false)                   ;; Z - Start marker
        _ (.setInt16 (js/DataView. buffer) 1 (inc (.-length body)) false) ;; Length + 1
        _ (.setInt8 (js/DataView. buffer) 3 0x00 false)                   ;; [NUL]
        header (js/Uint8Array. buffer)
        _ (prn "header: " header)

        message (js/Uint8Array. (+ (.-length header) (.-length body)))
        _ (.set message header)
        _ (.set message body (.-length header))

        checksum (js/Uint8Array. (crc16b message))
        _ (prn "crc16-value: " checksum)

        result (js/Uint8Array. (+ (.-length message) 2))
        _ (.set result message)
        _ (.set result checksum (.-length message))]
    (prn "hex: "  (.toHex result))
    result))
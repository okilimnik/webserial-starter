(ns webserial-starter.utils
  (:require
   ["classnames" :as classnames]
   [applied-science.js-interop :as j]))

(defn hex [i]
  (when i
    (-> i
        (.toString 16)
        (.padStart 4 "0")
        (.toLowerCase))))

(defn class-names [class-map]
  (classnames (clj->js class-map)))

(defn get-device-id [^js/SerialPort port]
  (j/let [^:js {:keys [usbVendorId usbProductId bluetoothServiceClassId]} (.getInfo port)]
    (if bluetoothServiceClassId
      (hex bluetoothServiceClassId)
      (str (hex usbVendorId) ":" (hex usbProductId)))))

(def encoder (js/TextEncoder.))
(def decoder (js/TextDecoder.))
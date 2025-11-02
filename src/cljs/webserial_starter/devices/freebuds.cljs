(ns webserial-starter.devices.freebuds
  (:require
   [applied-science.js-interop :as j]
   [webserial-starter.utils :refer [encoder]]))

;; https://mmk.pw/en/posts/freebuds-4i-proto/

(def DEVICE-ID "00001101-0000-1000-8000-00805f9b34fb")

(def crc16-tab
  [0, 4129, 8258, 12387, 16516, 20645, 24774, 28903, -32504, -28375, -24246, -20117, -15988, -11859,
   -7730,
   -3601, 4657, 528, 12915, 8786, 21173, 17044, 29431, 25302, -27847, -31976, -19589, -23718, -11331,
   -15460, -3073, -7202, 9314, 13379, 1056, 5121, 25830, 29895, 17572, 21637, -23190, -19125, -31448,
   -27383, -6674, -2609, -14932, -10867, 13907, 9842, 5649, 1584, 30423, 26358, 22165, 18100, -18597,
   -22662, -26855, -30920, -2081, -6146, -10339, -14404, 18628, 22757, 26758, 30887, 2112, 6241, 10242,
   14371, -13876, -9747, -5746, -1617, -30392, -26263, -22262, -18133, 23285, 19156, 31415, 27286, 6769,
   2640, 14899, 10770, -9219, -13348, -1089, -5218, -25735, -29864, -17605, -21734, 27814, 31879, 19684,
   23749, 11298, 15363, 3168, 7233, -4690, -625, -12820, -8755, -21206, -17141, -29336, -25271, 32407,
   28342, 24277, 20212, 15891, 11826, 7761, 3696, -97, -4162, -8227, -12292, -16613, -20678, -24743,
   -28808, -28280, -32343, -20022, -24085, -12020, -16083, -3762, -7825, 4224, 161, 12482, 8419, 20484,
   16421, 28742, 24679, -31815, -27752, -23557, -19494, -15555, -11492, -7297, -3234, 689, 4752, 8947,
   13010, 16949, 21012, 25207, 29270, -18966, -23093, -27224, -31351, -2706, -6833, -10964, -15091, 13538,
   9411, 5280, 1153, 29798, 25671, 21540, 17413, -22565, -18438, -30823, -26696, -6305, -2178, -14563,
   -10436, 9939, 14066, 1681, 5808, 26199, 30326, 17941, 22068, -9908, -13971, -1778, -5841, -26168,
   -30231, -18038, -22101, 22596, 18533, 30726, 26663, 6336, 2273, 14466, 10403, -13443, -9380, -5313,
   -1250, -29703, -25640, -21573, -17510, 19061, 23124, 27191, 31254, 2801, 6864, 10931, 14994, -722,
   -4849, -8852, -12979, -16982, -21109, -25112, -29239, 31782, 27655, 23652, 19525, 15522, 11395, 7392,
   3265, -4321, -194, -12451, -8324, -20581, -16454, -28711, -24584, 28183, 32310, 20053, 24180, 11923,
   16050, 3793, 7920])

(defn crc16
  "I don't found good implementation of CRC16-XModem in NPM
   and don't want to implement it by myself, for now, so I use
   this, with table =)"
  [data]
  (let [result (reduce (fn [s byte]
                         (let [idx (bit-and (bit-xor (bit-shift-right s 8) byte) 0xff)
                               new-s (bit-xor (get crc16-tab idx) (bit-shift-left s 8))]
                           (bit-and new-s 0xffff))) ; use only 16 bits
                       0
                       data)
        buffer (js/ArrayBuffer. 2)
        view (js/DataView. buffer)]
    (.setInt16 view 0 result false)
    (js/Uint8Array. buffer)))

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

        checksum (crc16 message)
        _ (prn "crc16-value: " checksum)

        result (js/Uint8Array. (+ (.-length message) 2))
        _ (.set result message)
        _ (.set result checksum (.-length message))]
    (prn "hex: "  (.toHex result))
    result))
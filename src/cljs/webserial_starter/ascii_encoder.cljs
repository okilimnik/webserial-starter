(ns webserial-starter.ascii-encoder
  (:require
   [applied-science.js-interop :as j]))

;; const substitutions = [ '␀','␁','␂','␃','␄','␅','␆','␇','␈','␉','␊','␋','␌','␍','␎','␏','␐','␑','␒','␓','␔','␕','␖','␗','␘','␙','␚','␛','␜','␝','␞','␟']
;; substitutions[127] = '␡'

(def class-map (-> #js ["NUL" "SOH" "STX" "ETX" "EOT" "ENQ" "ACK" "BEL" "BS" "HT" "LF" "VT"
                        "FF" "CR" "SO" "SI" "DLE" "DC1" "DC2" "DC3" "DC4" "NAK" "SYN" "ETB"
                        "CAN" "EM" "SUB" "ESC" "FS" "GS" "RS" "US"]
                   (j/assoc! 127 "DEL")))

(defn substitute [c]
  (cond
    (= c 127) "␡"
    (= c 9216) "␀"
    :else (js/String.fromCodePoint (+ c 9216))))

(defn encode [msg]
  (.replaceAll msg (js/RegExp. "[\\x00-\\x1F\\x7F]" "g")
               (fn [m] (substitute (.charCodeAt m 0)))))

(defn decode [msg]
  (.replaceAll msg (js/RegExp. "[\\u2400-\\u241F\\u2421]" "g")
               (fn [m]
                 (js/String.fromCodePoint
                  (if (= m "␡")
                    127
                    (- (.charCodeAt m 0) 9216))))))

(defn encode-with-html [msg & [new-lines?]]
  (.replaceAll msg (js/RegExp. "[\\x00-\\x1F\\x7F\\u2400-\\u241F\\u2421]" "g")
               (fn [m]
                 (let [c (let [code (.charCodeAt m 0)]
                           (cond
                             (= code 9249) 127
                             (> code 9216) (- code 9216)
                             :else code))]
                   (if (and new-lines? (= c 10))
                     "<x class=\"LF\">␊</x><br>"
                     (str "<x class=\"" (j/get class-map c "") "\">"
                          (substitute c)
                          "</x>"))))))
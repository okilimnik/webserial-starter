(ns webserial-starter.usb-ids
  (:require
   [webserial-starter.util :refer [hex]]
   [promesa.core :as p]))

(defn log [& args]
  (apply js/console.log args))

(defn make-text-file-line-iterator [file-url]
  (let [utf8-decoder (js/TextDecoder. "utf-8")]
    (-> (js/fetch file-url)
        (p/then #(.. % -body getReader))
        (p/then
         (fn [reader]
           (letfn [(process-chunk [chunk start-index lines]
                     (let [re (js/RegExp. "\\r\\n|\\n|\\r" "gm")]
                       (loop [current-index start-index
                              result-lines lines]
                         (let [result (.exec re chunk)]
                           (if result
                             (recur (.-lastIndex re)
                                    (conj result-lines
                                          (.substring chunk current-index (.-index result))))
                             [current-index result-lines])))))]
             (p/loop [acc []
                      remainder ""]
               (p/let [{:keys [value done]} (p/create #(.read reader %))]
                 (if (and done (empty? remainder))
                   acc
                   (let [chunk (str remainder
                                    (when value
                                      (.decode utf8-decoder value #js {:stream true})))
                         [index lines] (process-chunk chunk 0 [])]
                     (if done
                       (if (< index (count chunk))
                         (conj acc (.substr chunk index))
                         acc)
                       (p/recur (into acc lines)
                                (.substr chunk index)))))))))))))

  (defn get-usb-info [vid pid]
    (p/let [info (if (instance? js/SerialPort vid)
                   (let [port-info (.getInfo vid)]
                     {:vid (hex (.-usbVendorId port-info))
                      :pid (hex (.-usbProductId port-info))})
                   {:vid vid :pid pid})
            _ (log (str "searching for " (:vid info) ":" (:pid info)))
            lines (make-text-file-line-iterator "/usb-ids.txt")]
      (loop [[line & rest-lines] lines
             vendor nil
             result info]
        (cond
          (or (nil? line)
              (= line "# List of known device classes, subclasses and protocols"))
          result

          (or (.startsWith line "#") (empty? line))
          (recur rest-lines vendor result)

          vendor
          (if-let [pid-match (re-matches #"^\t[0-9a-f]{4}  .*" line)]
            (if (= (:pid info) (.substr line 1 4))
              (assoc result :product (.substr line 7))
              (recur rest-lines vendor result))
            (recur rest-lines nil (assoc result :vendor vendor)))

          :else
          (if-let [vid-match (re-matches #"^[0-9a-f]{4}  .*" line)]
            (if (= (:vid info) (.substr line 0 4))
              (recur rest-lines (.substr line 6) result)
              (recur rest-lines vendor result))
            (recur rest-lines vendor result))))))
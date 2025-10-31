(ns webserial-starter.ui.connect-modal
  (:require
   [replicant.alias :as alias]
   [webserial-starter.utils :refer [class-names]]))

(defn info [{:keys [id product vendor]}]
  (if id
    (str " - " (or product vendor "Unknown device") "(" id ")")
    ""))

(defn connect-modal [{:keys [connection]}]
  [:div#options {:replicant/on-mount [[:connect-modal/on-mount]]
                 :class (class-names {:open (:open connection)})}
   [:h2
    (cond
      (:open connection) [:strong "CONNECTED"]
      (or (:id connection)
          (:physically-connected connection)) [:strong "CONNECTED"]
      :else [:strong "UNPLUGGED"])
    (info connection)]

   [:fieldset
    [:legend "Options"]
    [:dl
     [:dt "Baud Rate"]
     [:dd [:input {:type "number"
                   :value (get-in connection [:options :baudRate])
                   :on {:change [[:baud-rate/change [:event.target/value]]]}}]]

     [:dt "Buffer Size"]
     [:dd [:input {:type "number"
                   :value (get-in connection [:options :bufferSize])
                   :on {:change [[:buffer-size/change [:event.target/value]]]}}]]

     [:dt "Data Bits"]
     [:dd
      [:input#data7 {:type "radio"
                     :value "7"
                     :name "data-bits"
                     :checked (= (get-in connection [:options :dataBits]) 7)
                     :on {:change [[:data-bits/change [:event.target/value]]]}}]
      [:label {:for "data7"} "7"]
      [:input#data8 {:type "radio"
                     :value "8"
                     :name "data-bits"
                     :checked (= (get-in connection [:options :dataBits]) 8)
                     :on {:change [[:data-bits/change [:event.target/value]]]}}]
      [:label {:for "data8"} "8"]]

     [:dt "Flow Control"]
     [:dd
      [:input#flowNone {:type "radio"
                        :value "none"
                        :name "flow-control"
                        :checked (= (get-in connection [:options :flowControl]) "none")
                        :on {:change [[:flow-control/change [:event.target/value]]]}}]
      [:label {:for "flowNone"} "none"]
      [:input#flowHardware {:type "radio"
                            :value "hardware"
                            :name "flow-control"
                            :checked (= (get-in connection [:options :flowControl]) "hardware")
                            :on {:change [[:flow-control/change [:event.target/value]]]}}]
      [:label {:for "flowHardware"} "hardware"]]

     [:dt "Parity"]
     [:dd
      [:input#parityNone {:type "radio"
                          :value "none"
                          :name "parity"
                          :checked (= (get-in connection [:options :parity]) "none")
                          :on {:change [[:parity/change [:event.target/value]]]}}]
      [:label {:for "parityNone"} "none"]
      [:input#parityEven {:type "radio"
                          :value "even"
                          :name "parity"
                          :checked (= (get-in connection [:options :parity]) "even")
                          :on {:change [[:parity/change [:event.target/value]]]}}]
      [:label {:for "parityEven"} "even"]
      [:input#parityOdd {:type "radio"
                         :value "odd"
                         :name "parity"
                         :checked (= (get-in connection [:options :parity]) "odd")
                         :on {:change [[:parity/change [:event.target/value]]]}}]
      [:label {:for "parityOdd"} "odd"]]

     [:dt "Stop Bits"]
     [:dd
      [:input#stop1 {:type "radio"
                     :value 1
                     :name "stop-bits"
                     :checked (= (get-in connection [:options :stopBits]) 1)
                     :on {:change [[:stop-bits/change [:event.target/value]]]}}]
      [:label {:for "stop1"} "1"]
      [:input#stop2 {:type "radio"
                     :value 2
                     :name "stop-bits"
                     :checked (= (get-in connection [:options :stopBits]) 2)
                     :on {:change [[:stop-bits/change [:event.target/value]]]}}]
      [:label {:for "stop2"} "2"]]]

    (if (or (:id connection)
            (:physically-connected connection))
      [:button {:on {:click [[:connection/connect]]}} "Connect"]
      [:button {:on {:click [[:connection/select-port]]}} "Select Serial Port..."])]])

(alias/register! :webserial/connect-modal connect-modal)
(ns webserial-starter.connect-modal 
  (:require
   [replicant.alias :as alias]
   [webserial-starter.stores.connection :as conn]))

(defn info [{:keys [id product vendor]}]
  (if id
    (str " - " (or product vendor "Unknown device") "(" id ")")
    ""))

(defn options-component [{:keys [content]}]
  (let [{:keys [connection]} content
        connection-state @(:state connection)]
    [:div#options {:class {:start (empty? (:messages connection))
                           :open (:open connection-state)}}
     [:h2
      (cond
        (:open connection-state) [:strong "CONNECTED"]
        (or (not (:id connection-state))
            (:physically-connected connection-state)) [:strong "CONNECTED"]
        :else [:strong "UNPLUGGED"])
      (info connection-state)]

     [:fieldset
      [:legend "Options"]
      [:dl
       [:dt "Baud Rate"]
       [:dd [:input {:type "number"
                     :value (get-in connection-state [:options :baud-rate])}]]

       [:dt "Buffer Size"]
       [:dd [:input {:type "number"
                     :value (get-in connection-state [:options :buffer-size])}]]

       [:dt "Data Bits"]
       [:dd
        [:input#data7 {:type "radio"
                       :value "7"
                       :name "data-bits"
                       :checked (= (get-in connection-state [:options :data-bits]) 7)}]
        [:label {:for "data7"} "7"]
        [:input#data8 {:type "radio"
                       :value "8"
                       :name "data-bits"
                       :checked (= (get-in connection-state [:options :data-bits]) 8)}]
        [:label {:for "data8"} "8"]]

       [:dt "Flow Control"]
       [:dd
        [:input#flowNone {:type "radio"
                          :value "none"
                          :name "flow-control"
                          :checked (= (get-in connection-state [:options :flow-control]) "none")}]
        [:label {:for "flowNone"} "none"]
        [:input#flowHardware {:type "radio"
                              :value "hardware"
                              :name "flow-control"
                              :checked (= (get-in connection-state [:options :flow-control]) "hardware")}]
        [:label {:for "flowHardware"} "hardware"]]

       [:dt "Parity"]
       [:dd
        [:input#parityNone {:type "radio"
                            :value "none"
                            :name "parity"
                            :checked (= (get-in connection-state [:options :parity]) "none")}]
        [:label {:for "parityNone"} "none"]
        [:input#parityEven {:type "radio"
                            :value "even"
                            :name "parity"
                            :checked (= (get-in connection-state [:options :parity]) "even")}]
        [:label {:for "parityEven"} "even"]
        [:input#parityOdd {:type "radio"
                           :value "odd"
                           :name "parity"
                           :checked (= (get-in connection-state [:options :parity]) "odd")}]
        [:label {:for "parityOdd"} "odd"]]

       [:dt "Stop Bits"]
       [:dd
        [:input#stop1 {:type "radio"
                       :value "1"
                       :name "stop-bits"
                       :checked (= (get-in connection-state [:options :stop-bits]) 1)}]
        [:label {:for "stop1"} "1"]
        [:input#stop2 {:type "radio"
                       :value "2"
                       :name "stop-bits"
                       :checked (= (get-in connection-state [:options :stop-bits]) 2)}]
        [:label {:for "stop2"} "2"]]]

      (if (or (not (:id connection-state))
              (not (:physically-connected connection-state)))
        [:button {:on {:click #(conn/select-port connection)}} "Select Serial Port..."]
        [:button {:on {:click #(conn/connect connection)}} "Connect"])]]))

(alias/register! :webserial/options-component options-component)
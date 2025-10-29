(ns webserial-starter.stores.connection 
  (:require
   [webserial-starter.utils :refer [hex]]))

(defn vid-pid [^js/SerialPort port]
  (let [info (.getInfo port)]
    (str (hex (.usbVendorId info)) ":" (hex (.usbProductId info)))))

(def encoder (js/TextEncoder.))
(def decoder (js/TextDecoder.))

;; Initial state
(def initial-state
  {:id nil
   :vendor nil
   :product nil
   :port nil
   :physically-connected false
   :open false
   :_reader nil
   :options {:baud-rate 115200
             :buffer-size 255
             :data-bits 8
             :flow-control "none"
             :parity "none"
             :stop-bits 1}
   :signals {}
   :messages []
   :prepend ""
   :append "\n"})

(defprotocol IConnectionStore
  (select-port [this])
  (init [this vid pid])
  (connect [this])
  (monitor [this])
  (write [this data])
  (close [this]))

(defrecord ConnectionStore [state]
  IConnectionStore
  (select-port [_]
    (-> (.. js/navigator -serial (requestPort))
        (.then (fn [port]
                 (let [info (vid-pid port)]
                   (set! js/window.location.search (str "?vid=" info.vid "&pid=" info.pid))
                   true)))
        (.catch (fn [_] false))))

  (init [_ vid pid]
    (-> (.. js/navigator -serial (getPorts))
        (.then (fn [ports]
                 (let [id (str vid ":" pid)
                       port (first (filter #(= (vid-pid %) id) ports))]
                   (when-not port
                     (set! js/window.location.search "")
                     (js/Promise.reject "Port not found"))

                   (swap! state assoc
                          :id id
                          :port port
                          :physically-connected true)

                   ;; Add event listeners
                   (.addEventListener (.. js/navigator -serial)
                                      "connect"
                                      (fn [e]
                                        (js/console.log (str id " device connected") e)
                                        (swap! state assoc
                                               :port (.-target e)
                                               :physically-connected true)))

                   (.addEventListener (.. js/navigator -serial)
                                      "disconnect"
                                      (fn [_]
                                        (js/console.log (str id " disconnect"))
                                        (swap! state assoc
                                               :physically-connected false
                                               :open false)))

                   (js/console.log (str id " initialized")))))))

  (connect [this]
    (when-let [port (:port @state)]
      (js/console.log (str (:id @state) ": opening"))
      (-> (.open port (clj->js (:options @state)))
          (.then (fn []
                   (swap! state assoc :open true)
                   (js/console.log (str (:id @state) ": opened"))
                   (monitor this)))
          (.catch (fn [e]
                    (js/console.log e)
                    (js/window.alert (.-message e)))))))

  (monitor [_]
    (js/console.log "monitor()")
    (let [port (:port @state)]
      (when (and (:open @state) (.-readable port))
        (let [reader (.getReader (.-readable port))]
          (swap! state assoc :_reader reader)
          (-> (js/Promise.
               (fn [resolve]
                 (letfn [(read-loop []
                           (-> (.read reader)
                               (.then (fn [{:keys [value done]}]
                                        (if done
                                          (do
                                            (swap! state assoc :open false)
                                            (resolve))
                                          (do
                                            (let [decoded (.decode decoder value)]
                                              (swap! state update :messages conj decoded))
                                            (when (:open @state)
                                              (read-loop))))))
                               (.catch (fn [error]
                                         (js/console.error "reading error" error)))))]
                   (read-loop))))
              (.finally #(.releaseLock reader)))))))

  (write [_ data]
    (when-let [port (:port @state)]
      (when (.-writable port)
        (let [writer (.getWriter (.-writable port))]
          (-> (.write writer (.encode encoder data))
              (.finally #(.releaseLock writer)))))))

  (close [_]
    (when-let [reader (:_reader @state)]
      (-> (.cancel reader)
          (.then #(.close (:port @state)))))))

;; Create store instance
(defn make-store [] (ConnectionStore. (atom initial-state)))
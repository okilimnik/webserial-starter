(ns webserial-starter.actions
  (:require
   [cemerick.url :refer [url]]
   [nexus.registry :as nxr]
   [promesa.core :as p]
   [webserial-starter.ascii-encoder :refer [decode]]
   [webserial-starter.shortcuts :refer [key-combo]]
   [webserial-starter.utils :refer [decoder encoder get-device-id]]))

(nxr/register-system->state! deref)

(defn dissoc-in [m path]
  (if (= 1 (count path))
    (dissoc m (first path))
    (update-in m (butlast path) dissoc (last path))))

(defn conj-in [m path v]
  (update-in m path conj v))

(defn update-state [state [op & args]]
  (case op
    :assoc-in (apply assoc-in state args)
    :dissoc-in (apply dissoc-in state args)
    :conj-in (apply conj-in state args)))

(nxr/register-placeholder!
 :event.target/value
 (fn [{:replicant/keys [dom-event]}]
   (some-> dom-event .-target .-value)))

(nxr/register-placeholder!
 :event.target/checked
 (fn [{:replicant/keys [dom-event]}]
   (some-> dom-event .-currentTarget .-checked)))

(nxr/register-effect!
 :store/save
 ^:nexus/batch
 (fn [_ store ops]
   (swap! store
          (fn [state]
            (reduce update-state state ops)))))

(nxr/register-effect!
 :toolbar/click
 (fn [{:replicant/keys [dom-event]}]
   (when (= "BUTTON" (.. dom-event -target -tagName))
     (.preventDefault dom-event)
     (let [textarea (.querySelector js/document "#input textarea")]
       (.focus textarea)
       (js/setTimeout #(.execCommand js/document "insertText" false (.. dom-event -target -textContent)) 10)))))

(nxr/register-action!
 :store/assoc-in
 (fn [_ path value]
   [[:store/save :assoc-in path value]]))

(nxr/register-action!
 :store/conj-in
 (fn [_ path value]
   [[:store/save :conj-in path value]]))

(nxr/register-action!
 :baud-rate/change
 (fn [_ value]
   [[:store/assoc-in [:connection :options :baudRate] (js/parseInt value)]]))

(nxr/register-action!
 :buffer-size/change
 (fn [_ value]
   [[:store/assoc-in [:connection :options :bufferSize] (js/parseInt value)]]))

(nxr/register-action!
 :data-bits/change
 (fn [_ value]
   [[:store/assoc-in [:connection :options :dataBits] (js/parseInt value)]]))

(nxr/register-action!
 :flow-control/change
 (fn [_ value]
   [[:store/assoc-in [:connection :options :flowControl] value]]))

(nxr/register-action!
 :parity/change
 (fn [_ value]
   [[:store/assoc-in [:connection :options :parity] value]]))

(nxr/register-action!
 :stop-bits/change
 (fn [_ value]
   [[:store/assoc-in [:connection :options :stopBits] (js/parseInt value)]]))

(nxr/register-action!
 :new-lines/change
 (fn [_ value]
   [[:store/assoc-in [:new-lines?] value]]))

(nxr/register-action!
 :counter/inc
 (fn [state path]
   [[:store/assoc-in path (inc (get-in state path))]]))

(nxr/register-action!
 :counter/dec
 (fn [state path]
   [[:store/assoc-in path (dec (get-in state path))]]))

(nxr/register-action!
 :connect-modal/on-mount
 (fn []
   (let [{:strs [device-id]} (:query (url js/window.location.href))]
     (if device-id
       [[:connection/init device-id]]
       []))))

(nxr/register-action!
 :footer/input-keyup
 (fn [_ e]
   (if (or (= (.-key e) "ArrowUp")
           (= (.-key e) "ArrowDown"))
     []
     [[:store/assoc-in [:wip] (.. e -target -value)]])))

(nxr/register-action!
 :main/console-scroll
 (fn [_ e]
   (let [scroll-point (+ (.. e -target -scrollTop)
                         (.. e -target -clientHeight))
         scrolled-to-bottom? (>= (+ scroll-point 10)
                                 (.. e -target -scrollHeight))]
     [[:store/assoc-in [:scrolled-to-bottom] scrolled-to-bottom?]])))

(nxr/register-effect!
 :dom/prevent-default
 (fn [{:replicant/keys [dom-event]}]
   (.preventDefault dom-event)))

(nxr/register-action!
 :connection/clear-messages
 (fn []
   [[:store/assoc-in [:connection :messages] []]]))

(nxr/register-effect!
 :connection/select-port
 (fn []
   (p/let [^js/SerialPort port (.. js/navigator -serial (requestPort))
           device-id (get-device-id port)]
     (set! js/window.location.href (-> (url js/window.location.href)
                                       (update :query assoc "device-id" device-id)
                                       str)))))

(nxr/register-effect!
 :connection/connect
 (fn [{:keys [state]} store]
   (let [connection (:connection state)]
     (when-let [port (:port connection)]
       (js/console.log (str (:id connection) ": opening"))
       (-> (.open port (clj->js (:options connection)))
           (.then (fn []
                    (nxr/dispatch store nil [[:store/assoc-in [:connection :open] true]
                                             [:connection/monitor]])
                    (js/console.log (str (:id connection) ": opened"))))
           (.catch (fn [e]
                     (js/console.log e)
                     (js/window.alert (.-message e)))))))))

(nxr/register-effect!
 :connection/monitor
 (fn [{:keys [state]} store]
   (js/console.log "monitor()")
   (let [connection (:connection state)
         port (:port connection)]
     (when (and (:open connection) (.-readable port))
       (let [reader (.getReader (.-readable port))]
         (nxr/dispatch store nil [[:store/assoc-in [:connection :_reader] reader]])
         (-> (js/Promise.
              (fn [resolve]
                (letfn [(read-loop []
                          (-> (.read reader)
                              (.then (fn [{:keys [value done]}]
                                       (if done
                                         (do
                                           (nxr/dispatch store nil [[:store/assoc-in [:connection :open] false]])
                                           (resolve))
                                         (do
                                           (let [decoded (.decode decoder value)]
                                             (nxr/dispatch store nil [[:store/conj-in [:connection :messages] decoded]]))
                                           (when (:open state)
                                             (read-loop))))))
                              (.catch (fn [error]
                                        (js/console.error "reading error" error)))))]
                  (read-loop))))
             (.finally #(.releaseLock reader))))))))

(nxr/register-effect!
 :connection/init
 (fn [_ store device-id]
   (p/let [ports (.. js/navigator -serial (getPorts))]
     (let [port (first (filter #(= (get-device-id %) device-id) ports))]
       (if-not port
         (do (set! js/window.location.href (-> (url js/window.location.href)
                                               (update :query dissoc "device-id")
                                               str))
             (p/rejected "Port not found"))
         (do
           (nxr/dispatch store nil [[:store/assoc-in [:connection :id] device-id]
                                    [:store/assoc-in [:connection :port] port]
                                    [:store/assoc-in [:connection :physically-connected] true]])
   
           ;; Add event listeners
           (.addEventListener (.. js/navigator -serial)
                              "connect"
                              (fn [e]
                                (js/console.log (str device-id " device connected") e)
                                (nxr/dispatch store nil [[:store/assoc-in [:connection :port] (.-target e)]
                                                         [:store/assoc-in [:connection :physically-connected] true]])))
   
           (.addEventListener (.. js/navigator -serial)
                              "disconnect"
                              (fn [_]
                                (js/console.log (str device-id " disconnect"))
                                (nxr/dispatch store nil [[:store/assoc-in [:connection :open] false]
                                                         [:store/assoc-in [:connection :physically-connected] false]])))
   
           (js/console.log (str device-id " initialized"))))))))

(nxr/register-effect!
 :connection/send
 (fn [{:keys [state]} _ cmd]
   (let [connection (:connection state)]
     (when-let [port (:port connection)]
       (when (.-writable port)
         (let [writer (.getWriter (.-writable port))]
           (-> (.write writer (.encode encoder cmd))
               (.finally #(.releaseLock writer)))))))))

(nxr/register-effect!
 :connection/close
 (fn [{:keys [state]}]
   (let [connection (:connection state)]
     (when-let [reader (:_reader connection)]
       (-> (.cancel reader)
           (.then #(.close (:port connection))))))))

(nxr/register-action!
 :connection/append
 (fn [_ value]
   [[:store/assoc-in [:connection :append] value]]))

(nxr/register-action!
 :connection/prepend
 (fn [_ value]
   [[:store/assoc-in [:connection :prepend] value]]))

(defn set-input-value! [target value]
  (set! (.-selectionStart target) 0)
  (set! (.-selectionEnd target) (.. target -value -length))
  (.execCommand target "insertText" false value))

(defn interceptor [state e]
  (let [{:keys [history history-index prepend append wip]} state]
    (case (key-combo e)
      :CLEAR
      (do
        (.preventDefault e)
        (nxr/dispatch state nil [[:connection/clear-messages]])
        true)

      :IGNORE_LF
      (do
        (.execCommand js/document "insertText" false "␊")
        true)

      :SEND
      (let [target-value (.. e -target -value)
            cmd (str prepend target-value append)]
        (.preventDefault e)
        (nxr/dispatch state nil (concat
                                 (when-not (= (peek history) target-value)
                                   [[:store/conj-in [:history] target-value]])
                                 [[:counter/inc [:history-index]]
                                  [:store/assoc-in [:wip] ""]
                                  [:connection/send (decode cmd)]]))
        (set-input-value! (.-target e) "")
        true)

      :UP
      (when-not (zero? history-index)
        (nxr/dispatch state nil [[:counter/dec [:history-index]]])
        (set-input-value! (.-target e) (nth history (dec history-index)))
        true)

      :DOWN
      (do
        (nxr/dispatch state nil [[:counter/inc [:history-index]]])
        (if (>= (inc history-index) (count history))
          (do
            (set-input-value! (.-target e) wip)
            (nxr/dispatch state nil [[:store/assoc-in [:history-index] (count history)]])
            true)
          (when (< history-index (count history))
            (set-input-value! (.-target e) (nth history history-index))
            true)))

      nil)))  ;; default case - return nil if no shortcuts matched


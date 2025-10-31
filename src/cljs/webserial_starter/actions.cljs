(ns webserial-starter.actions
  (:require
   [applied-science.js-interop :as j]
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
 :dom/event
 (fn [{:replicant/keys [dom-event]}]
   dom-event))

(nxr/register-placeholder!
 :event/key
 (fn [{:replicant/keys [dom-event]}]
   (j/get dom-event :key)))

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
 (fn [_ store dom-event]
   (when (= "BUTTON" (j/get-in dom-event [:target :tagName]))
     (j/call dom-event :preventDefault)
     (let [textarea (j/call js/document :querySelector "#input textarea")]
       (j/call textarea :focus)
       (js/setTimeout #(nxr/dispatch store nil [[:dom/insert-text js/document (j/get-in dom-event [:target :textContent])]]) 10)))))

(nxr/register-effect!
 :event/prevent-default
 (fn [_ _ dom-event]
   (j/call dom-event :preventDefault)))

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
 :input/change
 (fn [_ value]
   [[:store/assoc-in [:input] value]]))

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
 :input/keyup
 (fn [_ k v]
   (if (or (= k "ArrowUp")
           (= k "ArrowDown"))
     []
     [[:store/assoc-in [:wip] v]])))

(nxr/register-action!
 :ascii-input/on-key-down
 (fn [_ dom-event k]
   (case k
     "Tab" [[:event/prevent-default dom-event]
            [:dom/insert-text js/document \␋]]
     "Enter" [[:event/prevent-default dom-event]
              [:dom/insert-text js/document \␊]]
     [])))

(nxr/register-action!
 :main/console-scroll
 (fn [_ e]
   (let [scroll-point (+ (.. e -target -scrollTop)
                         (.. e -target -clientHeight))
         scrolled-to-bottom? (>= (+ scroll-point 10)
                                 (.. e -target -scrollHeight))]
     [[:store/assoc-in [:scrolled-to-bottom] scrolled-to-bottom?]])))

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
                    (nxr/dispatch store nil [[:store/assoc-in [:connection :open] true]])
                    (nxr/dispatch store nil [[:connection/monitor]])
                    (js/console.log (str (:id connection) ": opened"))))
           (.catch (fn [e]
                     (js/console.log e)
                     (js/window.alert (.-message e)))))))))

(nxr/register-effect!
 :connection/monitor
 (fn [{:keys [state]} store]
   (js/console.log "monitor()")
   (p/let [connection (:connection state)
           port (:port connection)]
     (when (:open connection)
       (when-let [readable (j/get port :readable)]
         (let [reader (j/call readable :getReader)]
           (nxr/dispatch store nil [[:store/assoc-in [:connection :_reader] reader]])
           (-> (p/loop []
                 (when (-> @store :connection :open)
                   (p/let [data (j/call reader :read)]
                     (if (j/get data :done)
                       (nxr/dispatch store nil [[:store/assoc-in [:connection :open] false]])
                       (let [decoded (j/call decoder :decode (j/get data :value))]
                         (nxr/dispatch store nil [[:store/conj-in [:connection :messages] decoded]])
                         (p/recur))))))
               (p/finally #(j/call reader :releaseLock)))))))))

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
       (when-let [writable (j/get port :writable)]
         (let [writer (j/call writable :getWriter)]
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

(nxr/register-action!
 :input/set
 (fn [_ target value]
   (set! (.-selectionStart target) 0)
   (set! (.-selectionEnd target) (.. target -value -length))
   [[:dom/insert-text target value]]))

(nxr/register-effect!
 :dom/insert-text
 (fn [_ _ target value]
   (j/call target :execCommand "insertText" false value)))

(defn interceptor [state dom-event]
  (let [{:keys [history history-index prepend append wip]} state]
    (case (key-combo dom-event)

      :CLEAR [[:event/prevent-default dom-event]
              [:connection/clear-messages]]

      :IGNORE_LF [[:dom/insert-text js/document "␊"]]

      :SEND (let [target-value (j/get-in dom-event [:target :value])
                  cmd (str prepend target-value append)]
              (vec
               (concat
                (when-not (= (peek history) target-value)
                  [[:store/conj-in [:history] target-value]])
                [[:input/set (.-target dom-event) ""]
                 [:event/prevent-default dom-event]
                 [:counter/inc [:history-index]]
                 [:store/assoc-in [:wip] ""]
                 [:connection/send (decode cmd)]])))

      :UP (when-not (zero? history-index)
            [[:input/set (j/get dom-event :target) (nth history (dec history-index))]
             [:counter/dec [:history-index]]])

      :DOWN (if (>= (inc history-index) (count history))
              [[:input/set (j/get dom-event :target) wip]
               [:store/assoc-in [:history-index] (count history)]]
              [[:input/set (j/get dom-event :target) (nth history (inc history-index))]
               [:counter/inc [:history-index]]])

      nil)))  ;; default case - return nil if no shortcuts matched

(nxr/register-action!
 :input/on-key-down
 (fn [state dom-event k]
   (if-let [actions (interceptor state dom-event)]
     actions
     [[:ascii-input/on-key-down dom-event k]])))
(ns webserial-starter.actions
  (:require
   [nexus.registry :as nxr]
   [webserial-starter.ascii-encoder :refer [decode]]
   [webserial-starter.shortcuts :refer [key-combo]]
   [webserial-starter.stores.connection :as conn]))

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

(nxr/register-effect! :store/save
                      ^:nexus/batch
                      (fn [_ store ops]
                        (swap! store
                               (fn [state]
                                 (reduce update-state state ops)))))

(nxr/register-action!
 :store/assoc-in
 (fn [_ path value]
   [[:store/save :assoc-in path value]]))

(nxr/register-action!
 :store/conj-in
 (fn [_ path value]
   [[:store/save :conj-in path value]]))

(nxr/register-action!
 :counter/inc
 (fn [state path]
   [[:store/assoc-in path (inc (get-in state path))]]))

(nxr/register-action!
 :counter/dec
 (fn [state path]
   [[:store/assoc-in path (dec (get-in state path))]]))

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
 :connection/clear-messages
 (fn [{:keys [connection]}]
   (swap! (:state connection) assoc :messages [])))

(nxr/register-effect!
 :connection/send
 (fn [{:keys [connection]} cmd]
   (conn/write connection cmd)))

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


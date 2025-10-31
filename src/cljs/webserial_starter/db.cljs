(ns webserial-starter.db)

(def connection
  {:id nil
   :vendor nil
   :product nil
   :port nil
   :physically-connected false
   :open false
   :_reader nil
   :options {:baudRate 115200
             :bufferSize 255
             :dataBits 8
             :flowControl "none"
             :parity "none"
             :stopBits 1}
   :signals {}
   :messages []
   :input ""
   :prepend ""
   :append \␊})

(defonce store (atom {:wip ""
                      :scrolled-to-bottom true
                      :history []
                      :history-index 0
                      :new-lines? true
                      :connection connection}))
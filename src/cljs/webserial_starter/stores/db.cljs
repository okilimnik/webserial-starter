(ns webserial-starter.stores.db)

(defonce store (atom {:wip ""
                      :scrolled-to-bottom true
                      :history []
                      :history-index 0
                      :prepend {:input-data ""
                                :displayed-input ""}
                      :append {:input-data \n
                               :displayed-input "<x class=\"LF\">␊</x><br>"}}))
(ns webserial-starter.utils)

(defn hex [i]
  (when i
    (-> i
        (.toString 16)
        (.padStart 4 "0")
        (.toLowerCase))))
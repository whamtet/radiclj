(ns radiclj.app.web.resource-cache
    (:require
      [clojure.java.io :as io]
      [radiclj.app.env :refer [dev?]]))

(def hash-resource
  ((if dev? identity memoize)
   (fn [src]
     (->> (.split src "/")
          last
          (str "public/")
          io/resource
          slurp
          hash))))

(defn cache-suffix [src]
  (->> src
       hash-resource
       (str src "?hash=")))

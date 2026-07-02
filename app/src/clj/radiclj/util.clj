(ns radiclj.util)

(defn flatten-all [m] (->> m (tree-seq coll? seq) (remove coll?)))

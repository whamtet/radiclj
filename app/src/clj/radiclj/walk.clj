(ns radiclj.walk
  (:require
    [clojure.walk :as walk]))

(defn- hiccup-vector? [v]
  (and (vector? v) (-> 0 v keyword?)))
(defn postwalk-hiccup [f hiccup]
  (walk/postwalk #(if (hiccup-vector? %) (f %) %) hiccup))

(defn- uniform-vector [[kw attrs & body :as v]]
  (if (map? attrs)
    v
    (vec (list* kw {} attrs body))))
(defn uniformize [hiccup]
  (postwalk-hiccup uniform-vector hiccup))

(defn- extract-id [kw]
  (when-let [id (re-find #"#\w+" kw)]
    [(-> kw (.replace id "") keyword) (.substring id 1)]))

(defn- transfer-id [v]
  (if-let [[tag id] (-> 0 v name extract-id)]
    (-> v
        (assoc 0 tag)
        (assoc-in [1 :id] id))
    v))
(defn transfer-ids [hiccup]
  (postwalk-hiccup transfer-id hiccup))

(defn standardize [hiccup]
  (->> hiccup uniformize transfer-ids (walk/postwalk #(if (seq? %) (vec %) %))))



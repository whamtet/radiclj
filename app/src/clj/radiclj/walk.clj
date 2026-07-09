(ns radiclj.walk)

(defn- hiccup-vector? [v]
  (and (vector? v) (-> v first keyword?)))

(defn- walk-hiccup [f hiccup]
  (cond
    (vector? hiccup)
    (let [[tag attrs & body] (f hiccup) ;; assume normalized
          body (map #(walk-hiccup f %) body)]
      (vec (list* tag attrs body)))
    (seq? hiccup)
    (map #(walk-hiccup f %) hiccup)
    :else hiccup))

(defn- uniform-vector [[kw attrs & body :as v]]
  (if (map? attrs)
    v
    (vec (list* kw {} attrs body))))

(defn- extract-id [kw]
  (when-let [id (re-find #"#\w+" kw)]
    [(-> kw (.replace id "") keyword) (.substring id 1)]))
(defn- transfer-id [v]
  (if-let [[tag id] (-> v first name extract-id)]
    (-> v
        (assoc 0 tag)
        (assoc-in [1 :id] id))
    v))

(defn- pr-str2 [s]
  (if (string? s) s (pr-str s)))
(defn- write-action [m]
  (if (-> m :name (= "action"))
    (update m :value pr-str2)
    m))

(defn standardize [hiccup]
  (walk-hiccup #(-> % uniform-vector transfer-id (update 1 write-action)) hiccup))

(defn- id->path* [path id hiccup]
  (cond
    (and (hiccup-vector? hiccup) (= id (get-in hiccup [1 :id])))
    path
    (or (vector? hiccup) (seq? hiccup))
    (->> hiccup
         (map-indexed
          (fn [i x]
            (id->path* (conj path i) id x)))
         (some identity))))

(defn id->path [id hiccup]
  (id->path* [] (.substring id 1) hiccup))

(defn- assocl [m k v]
  (if (seq? m)
    (concat (take k m) [v] (drop (inc k) m))
    (assoc m k v)))
(defn- getl [m k]
  (if (seq? m)
    (nth m k)
    (get m k)))

(defn assocl-in
  [m [k & ks] v]
  (if ks
    (assocl m k (assocl-in (getl m k) ks v))
    (assocl m k v)))

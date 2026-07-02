(ns radiclj.rt
  (:refer-clojure :exclude [parse-long parse-double parse-boolean])
  (:require
    [clojure.data.json :as json]
    [clojure.string :as string]))

(def parse-date #(if (string? %)
                   (read-string (format "#inst \"%s\"" %))
                   %))
(def parse-trim #(if (string? %)
                   (-> % .trim not-empty)
                   %))
(def parse-long #(if (string? %)
                  (Long/parseLong (.trim %))
                  %))
(def parse-double #(if (string? %)
                    (Double/parseDouble (.trim %))
                    %))
(def parse-longs #(if (string? %)
                   [(parse-long %)]
                   (map parse-long %)))
(def parse-doubles #(if (string? %)
                     [(parse-double %)]
                     (map parse-double %)))

(def parse-long-option #(if (string? %)
                          (when-not (= "null" %)
                           (some-> % .trim not-empty Long/parseLong))
                         %))
(def parse-double-option #(if (string? %)
                            (when-not (= "null" %)
                             (some-> % .trim not-empty Double/parseDouble))
                           %))
(def parse-nullable #(when-not (#{"nil" "null" ""} %) %))

(defn- key-fn [^String s]
  (if (re-find #"^\d+$" s)
    (Long/parseLong s)
    (keyword s)))

(defn- read-str [s] (json/read-str s :key-fn key-fn))
(def parse-json #(if (string? %)
                   (read-str %)
                   %))

(def parse-array #(if (or (nil? %) (vector? %)) % [%]))
(def parse-set #(set (parse-array %)))

(def parse-boolean
  #(case %
     true true
     false false
     (contains? #{"true" "on"} %)))
(def parse-boolean-true
  #(case %
     true true
     false false
     (not= "false" %)))

(def parse-edn #(if (string? %)
                  (read-string %)
                  %))
(def parse-kw #(if (string? %) (keyword %) %))



(defn concat-stack [concat stack]
  (reduce
    (fn [stack x]
      (case x
        ".." (pop stack)
        "." stack
        (conj stack x)))
    stack
    concat))

(defn path [prefix stack p]
  (str
    prefix
    (string/join
      "_"
      (if (.startsWith p "\\")
        (-> p (.split "\\\\") rest)
        (-> p (.split "\\\\") (concat-stack stack))))))

(defn- merge-params [req i x extra]
  (update req :params merge x extra {:index i :i i}))

#_
(defn map-indexed
  "Similar to clojure.core/map-indexed but maintains the component stack correctly e.g. with component

  (defcomponent user [req i first-name last-name] ...)

  (map-indexed user
    [{:first-name \"Fred\" :last-name \"Dagg\"}
     {:first-name \"Sam\" :last-name \"Smith\"}])

  Adds optional fixed params in extra.
  "
  ([f req s] (map-indexed f req s {}))
  ([f req s extra]
   (clojure.core/map-indexed
    (fn [i x]
      (-> req (conj-stack i) (merge-params i x extra) f))
    s)))

(defmacro map-indexedm
  "Zips syms into extra then invokes map-indexed"
  [f req s & syms]
  `(map-indexed
    ~f
    ~req
    ~s
    ~(zipmap (map keyword syms) syms)))

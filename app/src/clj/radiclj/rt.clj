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

(defn map-component [req component component-name data]
  (->> data
       count
       range
       (map
        (fn [i]
          (-> req
              (assoc :mapped? true)
              (assoc-in [:params :i] i)
              (update :stack conj component-name i)
              component)))))

(defmacro map-componentm [component data]
  `(map-component ~'req ~component ~(str component) ~data))

(defn- string-fragment [s]
  (if (re-find #"^\d+$" s)
    (Long/parseLong s)
    (keyword s)))

(defn- chain [[k v]]
  (->
   (mapv string-fragment (-> k name (.split "_")))
   (conj v)))

(defn reconstitute [params]
  (->> params
       (map chain)
       (reduce
        #(assoc-in %1 (pop %2) (peek %2))
        {})))

(defn stack-name [{:keys [stack]} k]
  (string/join "_" (conj stack k)))
(defn stack-hash [req k]
  (str "#" (stack-name req k)))

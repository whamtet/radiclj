(ns radiclj.core
  (:require
    [radiclj.rt :as rt]
    [radiclj.util :as util]))

(defn- conj-stack [req name]
  (update req :stack conj name))

(defn- get-stack [{:keys [stack data]} k]
  (get-in data (conj stack k)))

(def parsers
  {:long `rt/parse-long
   :long-option `rt/parse-long-option
   :double `rt/parse-double
   :double-option `rt/parse-double-option
   :longs `rt/parse-longs
   :doubles `rt/parse-doubles
   :array `rt/parse-array
   :set `rt/parse-set
   :boolean `rt/parse-boolean
   :boolean-true `rt/parse-boolean-true
   :edn `rt/parse-edn
   :keyword `rt/parse-kw
   :nullable `rt/parse-nullable
   :trim `rt/parse-trim
   :json `rt/parse-json
   :date `rt/parse-date})

(defn- sym->f [sym]
  (when-let [meta (meta sym)]
    (some (fn [[k f]]
            (when (meta k)
              f))
          parsers)))

(defn- wrap-parser [sym body]
  (if-let [f (sym->f sym)]
    `(~f ~body)
    body))

(defn- bind-simple [args]
  (mapcat
   (fn [arg]
     [arg (wrap-parser arg `(get-in ~'req [:data ~(str arg)]))])
   args))
(defn- bind-nested [args]
  (mapcat
   (fn [arg]
     [arg (wrap-parser arg `(get-stack ~'req ~(str arg)))])))

(defn- cljs-quote [sym]
  (if false #_env/*compiler* sym `(quote ~sym)))
(defn get-syms [body]
  (->> body
       util/flatten-all
       (filter symbol?)
       distinct
       (mapv cljs-quote)))

(defmacro defcomponent [name binding & body]
  (let [name (vary-meta name assoc :syms (get-syms body) :arglist '[req])
        [simple _ nested] (partition-by symbol? binding)])
  `(defn ~name [~'req]
    (let [~@(bind-simple simple)
          ~@(bind-nested nested)]
      ~@body)))

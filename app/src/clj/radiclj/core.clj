(ns radiclj.core
  (:require
    [radiclj.render :as render]
    [radiclj.rt :as rt]
    [radiclj.tree :as tree]
    [radiclj.util :as util]
    [radiclj.walk :as walk]))

(defn- conj-stack [req name]
  (if (:mapped? req)
    (dissoc req :mapped?)
    (update req :stack conj name)))

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
     [arg (wrap-parser arg `(get-in ~'req [:data ~(keyword arg)]))])
   args))
(defn- bind-nested [args]
  (mapcat
   (fn [arg]
     [arg (wrap-parser arg `(get-stack ~'req ~(keyword arg)))])
   args))

(defn- cljs-quote [sym]
  (if false #_env/*compiler* sym `(quote ~sym)))
(defn get-syms [body]
  (->> body
       util/flatten-all
       (filter symbol?)
       distinct
       (mapv cljs-quote)))

(def optionals
  '{n [n (partial radiclj.rt/stack-name req)]
    h [h (partial radiclj.rt/stack-hash req)]
    id [id (radiclj.rt/stack-name req "")]})

(defmacro defcomponent [name binding & body]
  (let [name (vary-meta name assoc :syms (get-syms body))
        [simple _ nested] (partition-by symbol? binding)]
    `(defn ~name [~'req]
      (let [~@(bind-simple simple)
            ~@(bind-nested nested)
            ~@(->> body util/flatten-all (mapcat optionals) distinct)]
        ~@body))))

(defn- apply-component [req component]
  (->> req
       :params
       rt/reconstitute
       (assoc req :data)
       component
       walk/standardize))
(defn make-handler
  ([root-var]
   (make-handler root-var nil))
  ([root-var source]
   (let [subendpoints (tree/extract-endpoints root-var)
         component @root-var]
     (fn [req]
       (render/html-response
        (if (-> req :request-method (= :get))
          (if source
            (->> req source (assoc req :data) component walk/standardize)
            (-> req component walk/standardize))
          (if-let [{:keys [post target]} (some-> req :params :action read-string)]
            (if-let [f (subendpoints post)]
              (let [updated (apply-component req f)]
                (if target
                  (let [standardized (apply-component req component)]
                    (if-let [path (walk/id->path target standardized)]
                      (walk/assocl-in standardized path updated)
                      updated))
                  updated))
              (apply-component req component))
            (apply-component req component))))))))

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

(defmacro defcomponent [name binding & body]
  (let [name (vary-meta name assoc :syms (get-syms body))
        [simple _ nested] (partition-by symbol? binding)]
    `(defn ~name [~'req]
      (let [~@(bind-simple simple)
            ~@(bind-nested nested)
            ~'n (partial rt/stack-name ~'req)
            ~'h (partial rt/stack-hash ~'req)]
        ~@body))))

(defn make-handler [component component-sym source]
  (let [subendpoints (tree/extract-endpoints component-sym)]
    (fn [req]
      (render/html-response
       (if (-> req :request-method (= :get))
         (->> req source (assoc req :data) component)
         (let [existing-state
               (->> req
                    :params
                    rt/reconstitute
                    (assoc req :data)
                    component)]
           (if-let [{:keys [post target]} (some-> req :params :action read-string)]
             (if-let [f (subendpoints post)]
               (let [standardized (walk/standardize existing-state)
                     updated (f req)]
                 (if-let [path (and target (walk/id->path target standardized))]
                   (walk/unvectorize (assoc-in standardized path updated))
                   (walk/standardize-light updated)))
               (walk/standardize-light existing-state))
             (walk/standardize-light existing-state))))))))

(defmacro make-handlerm [component source]
  `(make-handler ~component '~component ~source))

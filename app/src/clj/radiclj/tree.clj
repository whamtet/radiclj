(ns radiclj.tree)

(defn- mapmerge [f s]
  (apply merge (map f s)))

(defn- extract-endpoints* [ns sym exclusions]
  (when-let [var (ns-resolve ns sym)]
    (let [{:keys [ns syms endpoint]} (meta var)
          exclusions (conj exclusions sym)
          mappings (->> syms
                        (remove exclusions)
                        (mapmerge #(extract-endpoints* ns % exclusions)))]
      (if endpoint
        (assoc mappings (str sym) var)
        mappings))))

(defn extract-endpoints [sym]
  (extract-endpoints* *ns* sym #{}))

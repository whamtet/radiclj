(ns radiclj.config)

(def render-safe? true)

(defn set-render-safe
  "Set false if you wish to include plain html strings inside your hiccup (default true)."
  [s]
  (alter-var-root #'render-safe? (constantly s)))

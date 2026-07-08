(ns radiclj.render
  (:require
    [hiccup.core :as hiccup]
    [hiccup2.core :as hiccup2]
    [radiclj.config :as config]))

(defn html
  "Renders SimpleUI-specific hiccup into html"
  [s]
  (if config/render-safe?
    (->> s hiccup2/html str)
    (->> s hiccup/html)))

(defn html-response [body]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (html body)})

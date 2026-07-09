(ns radiclj.app.web.views.page
  (:require
    [radiclj.app.web.resource-cache :as resource-cache]))

(defn page [css & body]
  (list
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1.0"}]
    [:link {:rel "stylesheet"
            :href (resource-cache/cache-suffix css)}]]
   [:body body]))

(ns radiclj.app.web.views.click-to-load
  (:require
    clojure.string
    [radiclj.app.web.views.page :as page]
    [radiclj.core :refer [defcomponent defupdater]]
    [radiclj.rt :as rt]))

(defn hidden [n v]
  [:input {:type "hidden" :name n :value v}])

(defcomponent tr [:nested name email agent-id]
  [:tr
   (hidden (n "name") name)
   [:td name]
   (hidden (n "email") email)
   [:td email]
   (hidden (n "agent-id") agent-id)
   [:td agent-id]])

(defcomponent page []
  (prn 'data data)
  (page/page
   "/rows.css"
   [:form {:method "POST"}
    [:table
     [:thead
      [:tr [:th "Name"] [:th "Email"] [:th "Status"]]]
     [:tbody
      (rt/map-componentm tr)
      [:tr
       [:td {:colspan 3}
        [:button {:type "submit"} "Load More Agents..."]]]]]
    ]))

(def src "0123456789ABCDEF")
(defn rand-str []
  (clojure.string/join (repeatedly 15 #(rand-nth src))))

(defn- row [i]
  {:name "Agent Smith" :email (format "void%s@null.org" i) :agent-id (rand-str)})

(defn default-data [_]
  {:page
   {:tr
    (mapv row (range 10))}})

(defn- add-rows [rows]
  (->> rows
       count
       (+ 10)
       (range (count rows))
       (map row)
       (concat rows)
       vec))
(defn updater [req]
  (update-in req [:data :page :tr] add-rows))

(ns radiclj.app.web.views.delete-row
  (:require
    clojure.string
    [radiclj.app.web.views.page :as page]
    [radiclj.core :refer [defcomponent defupdater]]
    [radiclj.rt :as rt]))

(defn hidden [n v]
  [:input {:type "hidden" :name n :value v}])

(defcomponent tr [i :nested name email]
  (when name
    [:tr {:id id}
     (hidden (n "name") name)
     [:td name]
     (hidden (n "email") email)
     [:td email]
     [:td "Active"]
     [:td
      [:button {:type "submit" :name "action" :value i}
       "Delete"]]]))

(defcomponent page []
  (page/page
   "/rows.css"
   [:form {:method "POST"}
    [:table
     [:thead
      [:tr [:th "Name"] [:th "Email"] [:th "Status"] [:th]]]
     [:tbody
      (rt/map-componentm tr)]]
    ]))

(defn default-data [_]
  {:page
   {:tr
    [{:name "Joe Smith" :email "joe@smith.org"}
     {:name "Angie MacDowell" :email "angie@macdowell.org"}
     {:name "Fuqua Tarkenton" :email "fuqua@tarkenton.org"}
     {:name "Kim Yee"	:email "kim@yee.org"}]}})

(defn dissoc-i [v i]
  (vec
   (concat
    (take i v)
    (drop (inc i) v))))
(defupdater updater [^:long action]
  (update-in req [:data :page :tr] dissoc-i action))

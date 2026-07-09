(ns radiclj.app.web.views.bulk-update
  (:require
    [radiclj.app.web.views.page :as page]
    [radiclj.core :refer [defcomponent]]
    [radiclj.rt :as rt]))

(defn hidden [n v]
  [:input {:type "hidden" :name n :value v}])

(defcomponent tr [i :stack name email status]
  [:tr ;{:class (when (contains? ids i) action)}
   [:td [:input {:type "checkbox" :name "ids" :value i #_#_:checked (contains? ids i)}]]
   (hidden (n "name") name)
   [:td name]
   (hidden (n "email") email)
   [:td email]
   (hidden (n "status") status)
   [:td status]])

(defcomponent page [^:longs ids]
  (page/page
   "/rows.css"
   [:form {:method "POST"}
    [:table
     [:thead
      [:tr [:th] [:th "Name"] [:th "Email"] [:th "Status"]]]
     [:tbody
      (rt/map-componentm tr)]]
    [:button.mmargin
     {:type "submit" :name "action" :value {:activate? true}}
     "Activate"]
    [:button.mmargin
     {:type "submit" :name "action" :value {:deactivate? true}}
     "Deactivate"]
    ]))

(defn default-data [_]
  {:page
   {:tr
    [{:name "Joe Smith" :email "joe@smith.org" :status "Inactive"}
     {:name "Angie MacDowell" :email "angie@macdowell.org" :status "Inactive"}
     {:name "Fuqua Tarkenton" :email "fuqua@tarkenton.org" :status "Inactive"}
     {:name "Kim Yee"	:email "kim@yee.org"	:status "Inactive"}]}})

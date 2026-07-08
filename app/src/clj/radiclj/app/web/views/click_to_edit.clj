(ns radiclj.app.web.views.click-to-edit
  (:require
    [radiclj.core :refer [defcomponent]]))

(defn- text [n v]
  [:input {:type "text" :name n :value v}])
(defn- emaili [n v]
  [:input {:type "text" :name n :value v}])
(defn- hidden [n v]
  [:input {:type "hidden" :name n :value v}])

(defcomponent ^:endpoint form-edit [first-name last-name email]
  [:form {:id id :hx-put "form-ro" :hx-target "this"}
   [:div
    [:label.mr "First Name"]
    (text "first-name" first-name)]
   [:div.form-group
    [:label.mr "Last Name"]
    (text "last-name" last-name)]
   [:div.form-group
    [:label.mr "Email Address"]
    (emaili "email" email)]
   [:button.margin "Save"]
   [:button.margin {:hx-get "form-ro"} "Cancel"]])

(defcomponent ^:endpoint form-ro [first-name last-name email]
  ;; make sure form-edit is included in endpoints
  form-edit
  [:form {:method "POST"}
   (hidden "first-name" first-name)
   [:div [:label "First Name"] ": " first-name]
   (hidden "last-name" last-name)
   [:div [:label "Last Name"] ": " last-name]
   (hidden "email" email)
   [:div [:label "Email"] ": " email]
   [:button.margin
    {:type "submit" :name "action" :value {:post "form-edit"}}
    "Click To Edit"]])

(defcomponent page []
  (list
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1.0"}]
    [:link {:rel "stylesheet"
            :href "/style.css"}]]
   [:body
    (form-ro req)]))

(defn default-data [_]
  {:first-name "Joe"
   :last-name "Bloggs"
   :email "joe@bloggs.com"})

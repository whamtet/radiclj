(ns radiclj.app.web.views.click-to-edit
  (:require
    [radiclj.app.web.views.page :as page]
    [radiclj.core :refer [defcomponent]]))

(defn- text [n v]
  [:input {:type "text" :name n :value v}])
(defn- emaili [n v]
  [:input {:type "text" :name n :value v}])
(defn- hidden [n v]
  [:input {:type "hidden" :name n :value v}])

(defcomponent ^:endpoint form-edit [first-name last-name email]
  [:form#anchor {:method "POST"}
   [:div
    [:label.mr "First Name"]
    (text "first-name" first-name)]
   [:div.form-group
    [:label.mr "Last Name"]
    (text "last-name" last-name)]
   [:div.form-group
    [:label.mr "Email Address"]
    (emaili "email" email)]
   [:button.margin {:type "submit"} "Save"]
   [:button.margin {:type "submit" :name "action" :value {:cancel? true}} "Cancel"]])

(defcomponent ^:endpoint form-ro [first-name last-name email ^:edn action]
  (let [[first-name last-name email] (when-not (:cancel? action) [first-name last-name email])]
    ;; make sure form-edit is included in endpoints
    form-edit
    [:form#anchor {:method "POST"}
     (hidden "first-name" first-name)
     [:div [:label "First Name"] ": " first-name]
     (hidden "last-name" last-name)
     [:div [:label "Last Name"] ": " last-name]
     (hidden "email" email)
     [:div [:label "Email"] ": " email]
     [:button.margin
      {:type "submit" :name "action" :value {:post "form-edit" :target "#anchor"}}
      "Click To Edit"]]))

(defcomponent page []
  (page/page
   "/click_to_edit.css"
   (form-ro req)))

(defn default-data [_]
  {:first-name "Joe"
   :last-name "Bloggs"
   :email "joe@bloggs.com"})

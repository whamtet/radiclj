(ns radiclj.app.web.routes.ui
  (:require
    [radiclj.app.web.middleware.exception :as exception]
    [radiclj.app.web.middleware.formats :as formats]
    [radiclj.app.web.views.bulk-update :as bulk-update]
    [radiclj.app.web.views.click-to-edit :as click-to-edit]
    [radiclj.core :as radiclj]
    [integrant.core :as ig]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.parameters :as parameters]))

(defn route-data [opts]
  (merge
   opts
   {:muuntaja   formats/instance
    :middleware
    [;; Default middleware for ui
    ;; query-params & form-params
      parameters/parameters-middleware
      ;; encoding response body
      muuntaja/format-response-middleware
      ;; exception handling
      exception/wrap-exception]}))

;; Routes
(defn ui-routes [_opts]
  [
    ["/" (radiclj/make-handler #'bulk-update/page bulk-update/default-data)]
    ["/click-to-edit" (radiclj/make-handler #'click-to-edit/page click-to-edit/default-data)]
    ])

(derive :reitit.routes/ui :reitit/routes)
(defmethod ig/init-key :reitit.routes/ui
  [_ opts]
  ["" (route-data opts) (ui-routes opts)])

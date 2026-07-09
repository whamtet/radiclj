(ns radiclj.app.web.routes.ui
  (:require
    [radiclj.app.web.middleware.exception :as exception]
    [radiclj.app.web.middleware.formats :as formats]
    [radiclj.app.web.views.bulk-update :as bulk-update]
    [radiclj.app.web.views.click-to-edit :as click-to-edit]
    [radiclj.app.web.views.click-to-load :as click-to-load]
    [radiclj.app.web.views.delete-row :as delete-row]
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
    ["/" (radiclj/make-handler
          #'delete-row/page
          delete-row/default-data
          delete-row/updater)]
    ["/click-to-load" (radiclj/make-handler
                       #'click-to-load/page
                       click-to-load/default-data
                       click-to-load/updater)]
    ["/bulk-update" (radiclj/make-handler
                     #'bulk-update/page
                     bulk-update/default-data
                     bulk-update/updater)]
    ["/click-to-edit" (radiclj/make-handler #'click-to-edit/page click-to-edit/default-data)]
    ])

(derive :reitit.routes/ui :reitit/routes)
(defmethod ig/init-key :reitit.routes/ui
  [_ opts]
  ["" (route-data opts) (ui-routes opts)])

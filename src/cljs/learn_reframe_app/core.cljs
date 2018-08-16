(ns learn-reframe-app.core
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [learn-reframe-app.events :as events]
   [learn-reframe-app.routes :as routes]
   [learn-reframe-app.views :as views]
   [learn-reframe-app.config :as config]))



(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.querySelector js/document "#root")))

(defn ^:export init []
  (routes/app-routes)
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))

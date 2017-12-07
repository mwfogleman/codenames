(ns codenames.server
  (:require [codenames.handler :refer [app]]
            [config.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]]
            [immutant.web :refer [run]])
  (:gen-class))

(defn -main [& args]
  (run app))

(ns codenames.server
  (:require
   [codenames.handler :refer [app]]
   [config.core :refer [env]]
   [org.httpkit.server :as httpkit :refer [run-server]])
  (:gen-class))

(defn -main [& args]
  (let [port (or (env :port) 3000)]
    (run-server #'app {:port port :join? false})))

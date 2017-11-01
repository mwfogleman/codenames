(ns codenames.server
  (:require [codenames.handler :refer [app]]
            [config.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

 (defn -main [& args]
   (let [port (Integer/parseInt (or (env :port) "3000"))]
     (run-jetty app {:port port :join? false})))


(defonce dev-server (atom nil))

(defn go-dev-server
  [] (when (not @dev-server)
       (reset! dev-server (-main))))

(defn stop-dev-server []
  (when @dev-server
    (.stop @dev-server)
    (reset! dev-server nil)))

(defn restart-dev-server []
  (stop-dev-server)
  (go-dev-server))

(ns codenames.api
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]))

(defapi game-routes
  (GET "/hello" []
       :query-params [name :- String]
       (ok {:message (str "Hello, " name)})))

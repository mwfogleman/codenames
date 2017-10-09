(ns codenames.handler
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [hiccup.page :refer [include-js include-css html5]]
            [codenames.middleware :refer [wrap-middleware]]
            [config.core :refer [env]]
            [schema.core :as s]))

(def Team (s/enum :red :blue))
(def Identity (s/enum :red :blue :assassin :neutral))
(def Pos (s/enum 0 1 2 3 4))
(def Position [(s/one Pos "a") (s/one Pos "b")])

(def Remaining
  {:red (s/enum 0 1 2 3 4 5 6 7 8 9)
   :blue (s/enum 0 1 2 3 4 5 6 7 8 9)})

(def Word
  {:word s/Str
   :identity Identity
   :revealed? s/Bool
   :position Position})

(def Words
  [(s/one Word 1) (s/one Word 2) (s/one Word 3) (s/one Word 4) (s/one Word 5) (s/one Word 6) (s/one Word 7) (s/one Word 8) (s/one Word 9) (s/one Word 10) (s/one Word 11) (s/one Word 12) (s/one Word 13) (s/one Word 14) (s/one Word 15) (s/one Word 16) (s/one Word 17) (s/one Word 18) (s/one Word 19) (s/one Word 20) (s/one Word 21) (s/one Word 22) (s/one Word 23) (s/one Word 24) (s/one Word 25)])

(def Game
  {:starting-team Team
   :current-team Team
   :remaining Remaining
   :winning-team (s/maybe Team)
   :words Words})

(def mount-target
  [:div#app
   [:h3 "ClojureScript has not been compiled!"]
   [:p "please run "
    [:b "lein figwheel"]
    " in order to start the compiler"]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn loading-page []
  (html5
   (head)
   [:body {:class "body-container"}
    mount-target
    (include-js "/js/app.js")]))


(defroutes routes
  (GET "/" [] (loading-page))
  (GET "/about" [] (loading-page))

  (resources "/")
  (not-found "Not Found"))

(def app (wrap-middleware #'routes))

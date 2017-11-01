(ns codenames.api
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
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

(defn positive-int? [n]
  (and (integer? n) (>= n 0)))

(def Round
  (s/pred positive-int?))

(def Game
  {:starting-team Team
   :current-team Team
   :remaining Remaining
   :winning-team (s/maybe Team)
   :round Round
   :words Words})

(defapi game-routes
  (GET "/hello" []
       :query-params [name :- String]
       (ok {:message (str "Hello, " name)})))

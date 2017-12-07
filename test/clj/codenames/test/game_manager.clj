(ns codenames.test.game-manager
  (:require [codenames.game :as game]
            [codenames.game-manager :as manager]
            [codenames.moves :as m]
            [codenames.queries :as q]
            [codenames.util :refer [in?]]
            [com.rpl.specter :as S]
            [clojure.test :refer :all]))

(defn one-time-setup []
  (def g (atom {})))

(defn one-time-teardown []
  (reset! g {}))

(defn once-fixture [f]
  (one-time-setup)
  (f)
  (one-time-teardown))

;; register as a one-time callback
(use-fixtures :once once-fixture)

(deftest games-is-empty
  (is (empty? @g)))

(deftest we-can-add-games
  (manager/create-game! g "test")
  (is (contains? @g "test")))

(deftest we-can-get-games
  (manager/create-game! g "test")
  (let [k (set (keys (manager/get-game! g "test")))]
    (is (= k #{:starting-team :current-team :remaining :winning-team :round :words}))))

;; (deftest we-can-reset-games
;;   (let [get-assassin      (fn [] (codenames.queries/get-the-assassin (-> @gmanager/games (get "test") :state)))
;;         g                 (manager/get-game! "test")
;;         original-assassin (get-assassin)
;;         _                 (manager/reset-game! "test")
;;         new-assassin      (get-assassin)]
;;     ;; there is an unlikely but possible edge case where the assassin is the same,
;;     ;; and in that edge case this test will fail
;;     (is (not= original-assassin new-assassin))))

;; (deftest we-can-delete-all-games
;;   (manager/delete-all-games!)
;;   (is (empty? g)))


;; (setval (game-path "very stale") {:state (prepare-game)
;;                                   :created-at (t/date-time 1986 10 14 4 3 27 456)} games)

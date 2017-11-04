(ns codenames.test.game-manager
  (:require [codenames.game :as game]
            [codenames.game-manager :as manager]
            [codenames.moves :as m]
            [codenames.queries :as q]
            [codenames.util :refer [in?]]
            [com.rpl.specter :as S]
            [clojure.test :refer :all]))

(deftest games-is-empty
  (is (empty? @manager/games)))

(deftest we-can-add-games
  (manager/create-game! "test")
  (is (manager/game-exists? "test")))

(deftest we-can-get-games
  (let [k (set (keys (manager/get-game "test")))]
    (is (= k #{:starting-team :current-team :remaining :winning-team :round :words}))))

(deftest we-can-reset-games
  (let [get-assassin      (fn [] (codenames.queries/get-the-assassin (-> @manager/games (get "test") :state)))
        g                 (manager/get-game "test")
        original-assassin (get-assassin)
        _                 (manager/reset-game! "test")
        new-assassin      (get-assassin)]
    ;; there is an unlikely but possible edge case where the assassin is the same,
    ;; and in that edge case this test will fail
    (is (not= original-assassin new-assassin))))

(deftest we-can-delete-all-games
  (manager/delete-all-games!)
  (is (empty? @manager/games)))

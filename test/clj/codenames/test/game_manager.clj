(ns codenames.test.game-manager
  (:require [codenames.game :as game]
            [codenames.game-manager :as manager]
            [codenames.moves :as m]
            [codenames.queries :as q]
            [codenames.util :refer [in?]]
            [com.rpl.specter :as S]
            [clojure.test :refer :all]))

(defn setup []
  (def g (atom {})))

(defn teardown []
  (reset! g {}))

(defn each-fixture [f]
  (setup)
  (f)
  (teardown))

(use-fixtures :each once-fixture)

(defn get-test-assassin [] (codenames.queries/get-the-assassin (-> @g (get "test") :state)))

(deftest games-is-empty
  (is (= @g {})))

(deftest we-can-add-games
  (manager/create-game! g "test")
  (is (contains? @g "test")))

(deftest we-can-get-games
  (manager/create-game! g "test")
  (let [k (set (keys (manager/get-game! g "test")))]
    (is (= k #{:starting-team :current-team :remaining :winning-team :round :words}))))

(deftest we-can-reset-games
  (manager/create-game! g "test")
  (let [original-assassin (get-test-assassin)
        _                 (manager/create-game! g "test")
        new-assassin      (get-test-assassin)]
    ;; there is an unlikely but possible edge case where the assassin is the same,
    ;; and in that edge case this test will fail
    (is (not= original-assassin new-assassin))))

;; (deftest we-can-delete-all-games
;;   (manager/delete-all-games!)
;;   (is (empty? g)))


;; (setval (game-path "very stale") {:state (prepare-game)
;;                                   :created-at (t/date-time 1986 10 14 4 3 27 456)} games)

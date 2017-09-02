(ns codenames.test.game
  #?(:clj
     (:require [codenames.game :as game]
               [codenames.moves :as m]
               [codenames.util :refer [in?]]
               [com.rpl.specter :as S]
               [clojure.test :refer :all])
     :cljs
     (:require [codenames.game :as game]
               [codenames.moves :as m]
               [codenames.util :refer [in?]]
               [com.rpl.specter :as S]
               [cljs.test :refer-macros [deftest is testing]])))

(deftest red-and-blue-are-only-teams
  (= (set game/teams)
     #{:red :blue}))

(def a-game (atom (game/prepare-game)))

(deftest game-is-a-map
  (is (map? @a-game)))

(deftest starting-team-is-current-team
  (let [{:keys [starting-team current-team]} @a-game]
    (is (= starting-team current-team))))

(deftest starting-team-is-red-or-blue
  (is (in? game/teams (:starting-team @a-game))))

(deftest current-team-is-red-or-blue
  (is (in? game/teams (:current-team @a-game))))

(deftest red-and-blue-start-with-nine-or-eight
  (let [{:keys [blue-remaining red-remaining]} @a-game
        starting-amounts [blue-remaining red-remaining]]
    (is (or (= starting-amounts [8 9])
            (= starting-amounts [9 8])))))

(deftest starting-round-is-zero
  (is (= (:round @a-game) 0)))

(deftest no-one-has-won
  (is (nil? (:winning-team @a-game))))

(deftest id-is-a-gensym-string
  (let [{:keys [id]} @a-game]
    (is (string? id)))) ;; what is the function that checks if a string matches a regex, e.g. #"\.+\d+"?

(deftest words-tests
  (let [all-words                                  (-> @a-game :words)
        a-word-map                                 (first all-words)
        {:keys [word identity revealed? position]} a-word-map]
    (testing "There are 25 words"
      (is (= (count all-words) 25)))

    (testing "Words are maps"
      (is (every? map? all-words)))

    (testing "Words have specific keys"
      (is (= (set (keys a-word-map))
             #{:word :identity :revealed? :position})))

    (testing "The word is a string"
      (is (string? word)))

    (testing "The identity is a valid identity"
      (is (in? [:red :blue :assassin :neutral] identity)))

    (testing "All words are hidden at the start"
      (is (false? revealed?)))

    (testing "Positions have two numbers"
      (is (and (= (count position) 2)
               (every? number? position))))

    (testing "Positions are less than five (zero-indexed)"
      (is (every? #(<= % 4)
                  position)))))

(deftest starting-alliances-and-revealed-are-sensible
  ;; All words should be hidden to start with, i.e. their revealed? status should be false.
  ;; There should be 25 total, with 9 on one color and 8 on the other, 7 neutrals, and 1 assassin.
  (let [freqs (m/get-freqs a-game)]
    (is (or (= freqs
               {[:blue false] 9, [:red false] 8, [:neutral false] 7, [:assassin false] 1})
            (= freqs
               {[:blue false] 8, [:red false] 9, [:neutral false] 7, [:assassin false] 1})))))

(deftest we-can-reset-games
  (let [get-words     (fn [g] (S/select [S/ATOM :words S/ALL :word] g))
        initial-words (get-words a-game)
        _             (game/new-game! a-game)
        new-words     (get-words a-game)]
    (is (not= initial-words new-words))))



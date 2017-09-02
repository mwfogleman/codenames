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
    (is (string? id))
    (is (some? (re-seq #"\w*\_*\d+" id)))))

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
  (let [freqs             (m/get-freqs a-game)
        revealed-statuses (->> freqs keys (map second))
        total             (->> freqs vals (apply +))
        hidden-civilians  (get freqs [:neutral false])
        hidden-assassins  (get freqs [:assassin false])
        hidden-blue       (get freqs [:blue false])
        hidden-red        (get freqs [:red false])]
    ;; All words should be hidden to start with, i.e. their revealed? status should be false.
    (is (every? false? revealed-statuses))
    ;; There should be 25 total, with 9 on one color and 8 on the other, 7 neutrals, and 1 assassin.
    (is (= total 25))
    (is (or (= hidden-blue 8) (= hidden-blue 9)))
    (is (or (= hidden-red 8) (= hidden-red 9)))
    (is (= hidden-civilians 7))
    (is (= hidden-assassins 1))))

(deftest we-can-reset-games
  (let [get-words     (fn [g] (S/select [S/ATOM :words S/ALL :word] g))
        initial-words (get-words a-game)
        _             (game/new-game! a-game)
        new-words     (get-words a-game)]
    (is (not= initial-words new-words))))

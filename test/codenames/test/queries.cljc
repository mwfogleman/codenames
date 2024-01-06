(ns codenames.test.queries
  #?(:clj
     (:require [com.rpl.specter :as S]
               [codenames.game :as game]
               [codenames.moves :as m]
               [codenames.queries :as q]
               [codenames.util :refer [in?]]
               [clojure.test :refer :all])
     :cljs
     (:require [com.rpl.specter :as S]
               [codenames.game :as game]
               [codenames.moves :as m]
               [codenames.queries :as q]
               [codenames.util :refer [in?]]
               [cljs.test :refer-macros [deftest is use-fixtures]])))

(def a-game (game/prepare-game))

(deftest assassin?-works
  (let [w (q/get-the-assassin a-game)]
    (is (q/assassin? a-game w))))

(deftest get-current-team-works
  (let [current-team (q/get-current-team a-game)]
    (is (in? [:red :blue] current-team))))

(deftest we-can-check-if-words-are-valid
  (let [a-word (-> a-game :words rand-nth :word)]
    (is (q/valid-word? a-game a-word))))

(deftest words-can-be-hidden-or-revealed
  (let [assassin-word (q/get-the-assassin a-game)
        g             (m/reveal! a-game assassin-word)]
    (testing "all words, including the assassin, are hidden to start"
      (is (q/hidden? a-game assassin-word)))
    (testing "let's reveal the assassin."
      (is (q/revealed? g assassin-word)))))

(deftest get-freqs-test
  (let [freqs (q/get-freqs a-game)
        ks    (keys freqs)
        vs    (vals freqs)]
    (testing "the first part of every key is a identity keyword"
      (let [valid-id? (fn [id] (in? [:red :blue :assassin :neutral] id))
            ids       (map first ks)]
        (is (every? keyword? ids))
        (is (every? valid-id? ids))))
    (testing "the second part of every key is a boolean, and to start with they should be false"
      (let [boolean?          (fn [v] (or (= v true) (= v false)))
            revealed-statuses (map second ks)]
        (is (every? boolean? revealed-statuses))
        (is (every? false? revealed-statuses))))
    (testing " the sum of the values should be 25"
      (is (= (apply + vs) 25)))))

(deftest get-cell-returns-a-particular-word-by-position
  (let [a-word (-> a-game :words rand-nth)
        word-pos (:position a-word)
        [pos-1 pos-2] ((juxt first second) word-pos)
        returned-cell (q/get-cell a-game pos-1 pos-2)]
    (is (= a-word
           returned-cell))))

(deftest get-revealed-status-returns-the-revealed-status-of-a-particular-cell
  (is (false? (q/get-revealed-status a-game 0 0))))

(deftest get-id-of-word-works
  (let [a (q/get-the-assassin a-game)]
    (is (= (q/get-id-of-word a-game a)
           :assassin))))

(deftest get-remaining-works
  (let [remaining (q/get-remaining a-game)
        ks        (set (keys remaining))
        vs        (set (vals remaining))]
    (is (= ks
           #{:red :blue}))
    (is (= vs
           #{8 9}))))

(deftest word-id-matches-current-team?-works
  (let [current-team (q/get-current-team a-game)
        f            (if (= current-team :red) q/get-a-red q/get-a-blue)
        w            (f a-game)]
    (is (q/word-id-matches-current-team? a-game w))))

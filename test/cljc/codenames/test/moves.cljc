(ns codenames.test.moves
  #?(:clj
     (:require [com.rpl.specter :as S]
               [codenames.game :as game]
               [codenames.moves :as m]
               [codenames.util :refer [in?]]
               [clojure.test :refer :all])
     :cljs
     (:require [com.rpl.specter :as S]
               [codenames.game :as game]
               [codenames.moves :as m]
               [codenames.util :refer [in?]]
               [cljs.test :refer-macros [deftest is use-fixtures]])))

(def a-game (atom (game/prepare-game)))

;; Use each fixtures to reset a-game before each test, so we can
;; manipulate the state of the game without worrying about affecting
;; the other tests.

;; Clean the lint filter before you do laundry.

;; Blog post about :once and :each fixtures:
;; https://thornydev.blogspot.com/2012/09/before-and-after-logic-in-clojuretest.html

(defn setup []
  (game/new-game! a-game))

(defn each-fixture [f]
  "Resets the a-game variable for each new test."
  (setup)
  (f)
  ;; (teardown)
  )

(use-fixtures :each each-fixture)

(deftest we-can-check-if-words-are-valid
  (let [a-word (-> @a-game :words rand-nth :word)]
    (is (m/valid-word? a-game a-word))))

(deftest opposite-teams
  (testing "we can get the opposite team"
    (is (and
         (= (m/opposite-team :red) :blue)
         (= (m/opposite-team :blue) :red))))
  (testing "trying to get the opposite team only works with keywords"
    (is (thrown? AssertionError (m/opposite-team "red"))))
  (testing "trying to get the opposite team only works with :red or :blue"
    (is (thrown? AssertionError (m/opposite-team :green)))))

(defn grab-a-particular-word [game word]
  (S/select-any [S/ATOM :words S/ALL (S/if-path [:word (S/pred= word)] S/STAY)] game))

(defn get-a-word-of-identity [identity]
  (fn [game]
    (S/select-any [S/ATOM :words S/ALL (S/if-path [:identity (S/pred= identity)] :word)] game)))

(def get-the-assassin (get-a-word-of-identity :assassin))
(def get-a-red (get-a-word-of-identity :red))
(def get-a-blue (get-a-word-of-identity :blue))
(def get-a-neutral (get-a-word-of-identity :neutral))

(deftest words-can-be-hidden-or-revealed
  (let [assassin-word (get-the-assassin a-game)]
    (testing "all words, including the assassin, are hidden to start"
      (is (m/hidden? a-game assassin-word)))
    (testing "let's reveal the assassin."
      (m/reveal! a-game assassin-word)
      (is (m/revealed? a-game assassin-word)))))

(deftest get-freqs
  (let [freqs (m/get-freqs a-game)
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

(defn get-round
  [game]
  (S/select-any [S/ATOM :round] game))

(defn valid-player-team?
  [team]
  (in? [:red :blue] team))

(deftest next-round-works
  (let [initial-round (get-round a-game)
        _             (m/next-round! a-game)
        new-round     (get-round a-game)
        _             (m/next-round! a-game)
        another-round (get-round a-game)]
    (is (= initial-round 0))
    (is (= new-round 1))
    (is (= another-round 2))
    (is (< initial-round new-round another-round))))

(deftest get-current-team-works
  (let [current-team (m/get-current-team a-game)]
    (is (in? [:red :blue] current-team))))

(deftest switch-teams-works
  (let [current-team (m/get-current-team a-game)
        _            (m/switch-teams! a-game)
        new-team     (m/get-current-team a-game)]
    (is (valid-player-team? current-team))
    (is (valid-player-team? new-team))
    (is (not= current-team new-team))))

(deftest next-turn-combines-next-round-and-switch-teams
  (let [initial-round (get-round a-game)
        current-team  (m/get-current-team a-game)
        _             (m/next-turn! a-game)
        new-round     (get-round a-game)
        new-team      (m/get-current-team a-game)]
    (is (= initial-round 0))
    (is (= new-round 1))
    (is (valid-player-team? current-team))
    (is (valid-player-team? new-team))
    (is (not= current-team new-team))))

(deftest winners
  (testing "there is no winner to start"
    (is (nil? (m/get-winner a-game)))
    (is (false? (m/winner? a-game))))
  (testing "we can make :red the winner"
    (m/set-winner! a-game :red)
    (is (= (m/get-winner a-game)
           :red))
    (is (true? (m/winner? a-game))))
  (testing "win! makes the current-team the winner"
    (let [current-team (m/get-current-team a-game)
          _            (m/win! a-game)]
      (is (= (m/get-winner a-game)
             current-team))
      (is (true? (m/winner? a-game)))))
  (testing "lose! makes the current-team the winner"
    (let [current-team  (m/get-current-team a-game)
          opposite-team (m/opposite-team current-team)
          _             (m/lose! a-game)]
      (is (= (m/get-winner a-game)
             opposite-team))
      (is (true? (m/winner? a-game))))))

(deftest get-cell-returns-a-particular-word-by-position
  (let [a-word (-> @a-game :words rand-nth)
        word-pos (:position a-word)
        [pos-1 pos-2] ((juxt first second) word-pos)
        returned-cell (m/get-cell a-game pos-1 pos-2)]
    (is (= a-word
           returned-cell))))

(deftest get-revealed-status-returns-the-revealed-status-of-a-particular-cell
  (is (false? (m/get-revealed-status a-game 0 0))))

(deftest get-id-of-word-works
  (let [a (get-the-assassin a-game)]
    (is (= (m/get-id-of-word a-game a)
           :assassin))))

(deftest get-remaining-works
  (let [remaining (m/get-remaining a-game)
        ks        (set (keys remaining))
        vs        (set (vals remaining))]
    (is (= ks
           #{:red :blue}))
    (is (= vs
           #{8 9}))))

(defn get-a-red [game]
  (S/select-any [S/ATOM :words S/ALL (S/if-path [:identity (S/pred= :red)] :word)] game))

(deftest update-remaining-works
  (let [initial-remaining     (m/get-remaining a-game)
        initial-red-remaining (:red initial-remaining)
        initial-hidden-total  (->> initial-remaining vals (apply +))
        ;; reveal a red, rather than a neutral or the assassin. blue would do just as well.
        a-red                 (get-a-red a-game)
        _                     (m/reveal! a-game a-red)
        _                     (m/update-remaining! a-game)
        new-remaining         (m/get-remaining a-game)
        new-red-remaining     (:red new-remaining)
        new-hidden-total      (->> new-remaining vals (apply +))]
    (testing "all cards are hidden to start, but there are 9 on one team (e.g., :blue) and 8 on the
    other (e.g., :red), for a total of 17"
      (is (= initial-hidden-total
             17)))
    (testing "if we reveal a word, the keys for :blue and :red will be different"
      (is (not= initial-remaining new-remaining))
      (is (> initial-red-remaining new-red-remaining))
      (is (= initial-red-remaining (inc new-red-remaining))))
    (testing "there should now be 16 hidden on each team"
      (is (= new-hidden-total
             16)))))

;; move!

(deftest move-can-throw-assertions
  (testing "assertions are thrown for invalid words, such as SCREWDRIVER" 
    (is (thrown? AssertionError (m/move! a-game "SCREWDRIVER"))))
  (testing "assertions are thrown for revealed words"
    (let [a-word (-> @a-game :words rand-nth :word)
          _      (m/reveal! a-game a-word)]
      (is (thrown? AssertionError (m/move! a-game a-word)))))
  (testing "assertions are thrown if there is already a winner"
    (let [a-word (-> @a-game :words rand-nth :word)
          _      (m/win! a-game)]
      (is (thrown? AssertionError (m/move! a-game a-word))))))

(deftest move-reveals-the-selected-word
  (let [a-neutral               (get-a-neutral a-game)
        initial-revealed-status (->> a-neutral (grab-a-particular-word a-game) :revealed?)
        _                       (m/move! a-game a-neutral)
        new-revealed-status     (->> a-neutral (grab-a-particular-word a-game) :revealed?)]
    (is (false? initial-revealed-status))
    (is (not= initial-revealed-status new-revealed-status))
    (is (true? new-revealed-status))))

(deftest move-updates-remaining-words
  (let [initial-remaining (m/get-remaining a-game)
        a-red             (get-a-red a-game)
        _                 (m/move! a-game a-red)
        new-remaining     (m/get-remaining a-game)]
    (is (not= initial-remaining new-remaining))
    (is (= (:red initial-remaining)
           (inc (:red new-remaining))))))

(deftest picking-the-assassin-makes-you-lose
  (let [initial-winner (m/get-winner a-game)
        current-team   (m/get-current-team a-game)
        assassin-word  (get-the-assassin a-game)
        _              (m/move! a-game assassin-word)
        winner         (m/get-winner a-game)]
    (is (nil? initial-winner))
    (is (= winner
           (m/opposite-team current-team)))))

(deftest if-you-pick-someone-on-your-team-you-can-keep-moving
  (let [initial-round   (get-round a-game)
        initial-team    (m/get-current-team a-game)
        good-guess-word (if (= :blue initial-team) (get-a-blue a-game) (get-a-red a-game))
        _               (m/move! a-game good-guess-word)
        new-round       (get-round a-game)
        new-team        (m/get-current-team a-game)]
    (is (= initial-round new-round))
    (is (= initial-team new-team))))

(deftest picking-neutral-causes-it-to-be-the-other-teams-turn
  (let [a-neutral            (get-a-neutral a-game)
        initial-round        (get-round a-game)
        initial-current-team (m/get-current-team a-game)    
        _                    (m/move! a-game a-neutral)
        new-round            (get-round a-game)
        new-current-team     (m/get-current-team a-game)]
    (is (not= initial-current-team new-current-team))
    (is (= initial-current-team (m/opposite-team new-current-team)))
    (is (not= initial-round new-round))
    (is (< initial-round new-round))
    (is (= new-round (inc initial-round)))))

(deftest if-you-pick-someone-on-the-other-team-its-their-turn
  (let [initial-round  (get-round a-game)
        initial-team   (m/get-current-team a-game)
        bad-guess-word (if (= :blue initial-team) (get-a-red a-game) (get-a-blue a-game))
        _              (m/move! a-game bad-guess-word)
        new-round      (get-round a-game)
        new-team       (m/get-current-team a-game)]
    (is (not= initial-team new-team))
    (is (= initial-team (m/opposite-team new-team)))
    (is (not= initial-round new-round))
    (is (< initial-round new-round))
    (is (= new-round (inc initial-round)))))

(def one-blue-remaining-start-value
  {:starting-team :red,
   :remaining {:blue 1 :red 9}
   :current-team :red,
   :words
   '({:word "POST", :identity :red, :revealed? false, :position [2 0]}
     {:word "POISON", :identity :red, :revealed? false, :position [3 0]}
     {:word "PISTOL", :identity :red, :revealed? false, :position [2 2]}
     {:word "INDIA", :identity :red, :revealed? false, :position [0 1]}
     {:word "POLICE", :identity :red, :revealed? false, :position [1 3]}
     {:word "LINK", :identity :red, :revealed? false, :position [4 0]}
     {:word "SPY", :identity :red, :revealed? false, :position [0 2]}
     {:word "DAY", :identity :red, :revealed? false, :position [4 2]}
     {:word "TRUNK", :identity :red, :revealed? false, :position [1 4]}
     {:word "PAN", :identity :blue, :revealed? true, :position [2 1]}
     {:word "MILLIONAIRE", :identity :blue, :revealed? true, :position [2 3]}
     {:word "BALL", :identity :blue, :revealed? true, :position [1 2]}
     {:word "KING", :identity :blue, :revealed? true, :position [4 4]}
     {:word "MOUNT", :identity :blue, :revealed? true, :position [3 3]}
     {:word "LIMOUSINE", :identity :blue, :revealed? true, :position [3 2]}
     {:word "OCTOPUS", :identity :blue, :revealed? true, :position [0 0]}
     {:word "SPINE", :identity :blue, :revealed? false, :position [2 4]} ;; !!
     {:word "STADIUM", :identity :neutral, :revealed? false, :position [3 1]}
     {:word "CRASH", :identity :neutral, :revealed? false, :position [1 1]}
     {:word "PIRATE", :identity :neutral, :revealed? false, :position [0 3]}
     {:word "GLASS", :identity :neutral, :revealed? false, :position [1 0]}
     {:word "SWING", :identity :neutral, :revealed? false, :position [3 4]}
     {:word "KIWI", :identity :neutral, :revealed? false, :position [4 1]}
     {:word "SHAKESPEARE", :identity :neutral, :revealed? false, :position [4 3]}
     {:word "LONDON", :identity :assassin, :revealed? false, :position [0 4]}),
   :round 0,
   :id "G__47791",
   :winning-team nil})

(def one-blue-remaining (atom one-blue-remaining-start-value))

(def one-red-remaining-start-value
  {:starting-team :blue,
   :remaining {:blue 9 :red 1}
   :current-team :red,
   :words
   '({:word "WAVE", :identity :blue, :revealed? false, :position [0 1]}
     {:word "WEB", :identity :blue, :revealed? false, :position [1 4]}
     {:word "RABBIT", :identity :blue, :revealed? false, :position [3 2]}
     {:word "CAPITAL", :identity :blue, :revealed? false, :position [0 3]}
     {:word "SEAL", :identity :blue, :revealed? false, :position [0 0]}
     {:word "STAR", :identity :blue, :revealed? false, :position [1 2]}
     {:word "SOLDIER", :identity :blue, :revealed? false, :position [4 0]}
     {:word "ROME", :identity :blue, :revealed? false, :position [2 3]}
     {:word "DEATH", :identity :blue, :revealed? false, :position [2 2]}
     {:word "BERLIN", :identity :red, :revealed? true, :position [3 1]}
     {:word "LOCK", :identity :red, :revealed? true, :position [2 4]}
     {:word "AMBULANCE", :identity :red, :revealed? true, :position [4 2]}
     {:word "BAR", :identity :red, :revealed? true, :position [3 0]}
     {:word "LIMOUSINE", :identity :red, :revealed? true, :position [4 3]}
     {:word "FISH", :identity :red, :revealed? true, :position [1 1]}
     {:word "NEEDLE", :identity :red, :revealed? true, :position [4 1]}
     {:word "STRIKE", :identity :red, :revealed? false, :position [4 4]} ;; !!
     {:word "SPRING", :identity :neutral, :revealed? false, :position [2 0]}
     {:word "THIEF", :identity :neutral, :revealed? false, :position [1 3]}
     {:word "HOSPITAL", :identity :neutral, :revealed? false, :position [0 2]}
     {:word "EAGLE", :identity :neutral, :revealed? false, :position [3 4]}
     {:word "BOARD", :identity :neutral, :revealed? false, :position [3 3]}
     {:word "TEMPLE", :identity :neutral, :revealed? false, :position [2 1]}
     {:word "PLOT", :identity :neutral, :revealed? false, :position [0 4]}
     {:word "HAWK", :identity :assassin, :revealed? false, :position [1 0]}),
   :round 0,
   :id "G__47795",
   :winning-team nil})

(def one-red-remaining (atom one-red-remaining-start-value))

(deftest move-can-make-you-win-or-lose
  (testing "if there are no blue-remaining after your move, blue wins! (if you're blue, you win. if you're red, you lose.)"
    (let [initial-remaining (m/get-remaining one-blue-remaining)
          initial-winner    (m/winner? one-blue-remaining)
          current-team      (m/get-current-team one-blue-remaining)
          _                 (m/move! one-blue-remaining "SPINE")
          new-remaining     (m/get-remaining one-blue-remaining)
          new-winner        (m/winner? one-blue-remaining)
          winner            (m/get-winner one-blue-remaining)
          _                 (reset! one-blue-remaining one-blue-remaining-start-value)]
      (is (= (:blue initial-remaining) 1))
      (is (false? initial-winner))
      (is (= current-team :red))
      (is (true? new-winner))
      (is (= (:blue new-remaining) 0))
      (is (not= current-team winner))
      (is (= winner :blue))))

  (testing "if there are no red-remaining after your move, red wins! (if you're red, you win. if you're blue, you lose.)"
    (let [initial-remaining (m/get-remaining one-red-remaining)
          initial-winner    (m/winner? one-red-remaining)
          current-team      (m/get-current-team one-red-remaining)
          _                 (m/move! one-red-remaining "STRIKE")
          new-remaining     (m/get-remaining one-red-remaining)
          new-winner        (m/winner? one-red-remaining)
          winner            (m/get-winner one-red-remaining)
          _                 (reset! one-red-remaining one-red-remaining-start-value)]
      (is (= (:red initial-remaining) 1))
      (is (false? initial-winner))
      (is (= current-team :red))
      (is (true? new-winner))
      (is (= (:red new-remaining) 0))
      (is (= current-team winner))
      (is (= winner :red)))))

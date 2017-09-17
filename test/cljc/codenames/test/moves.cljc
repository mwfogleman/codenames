(ns codenames.test.moves
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

(deftest opposite-teams
  (testing "we can get the opposite team"
    (is (and
         (= (m/opposite-team :red) :blue)
         (= (m/opposite-team :blue) :red))))
  (testing "trying to get the opposite team only works with keywords"
    (is (thrown? AssertionError (m/opposite-team "red"))))
  (testing "trying to get the opposite team only works with :red or :blue"
    (is (thrown? AssertionError (m/opposite-team :green)))))

(deftest we-can-switch-the-view
  (is (= (m/switch-view! :player) :spymaster))
  (is (= (m/switch-view! :spymaster) :player)))

(defn valid-player-team?
  [team]
  (in? [:red :blue] team))

(deftest next-round-works
  (let [initial-round (:round a-game)
        g             (m/next-round! a-game)
        new-round     (:round g)
        g'            (m/next-round! g)
        another-round (:round g')]
    (is (= initial-round 0))
    (is (= new-round 1))
    (is (= another-round 2))
    (is (< initial-round new-round another-round))))

(deftest switch-teams-works
  (let [current-team (q/get-current-team a-game)
        g            (m/switch-teams! a-game)
        new-team     (q/get-current-team g)]
    (is (valid-player-team? current-team))
    (is (valid-player-team? new-team))
    (is (not= current-team new-team))))

(deftest next-turn-combines-next-round-and-switch-teams
  (let [initial-round (:round a-game)
        current-team  (q/get-current-team a-game)
        g             (m/next-turn! a-game)
        new-round     (:round g)
        new-team      (q/get-current-team g)]
    (is (= initial-round 0))
    (is (= new-round 1))
    (is (valid-player-team? current-team))
    (is (valid-player-team? new-team))
    (is (not= current-team new-team))))

(deftest winners
  (testing "there is no winner to start"
    (is (nil? (q/get-winner a-game)))
    (is (false? (q/winner? a-game))))
  (testing "we can make :red the winner"
    (let [g (m/set-winner! a-game :red)]
      (is (= (q/get-winner g)
             :red))
      (is (true? (q/winner? g)))))
  (testing "win! makes the current-team the winner"
    (let [current-team (q/get-current-team a-game)
          g            (m/win! a-game)]
      (is (= (q/get-winner g)
             current-team))
      (is (true? (q/winner? g)))))
  (testing "lose! makes the current-team the winner"
    (let [current-team  (q/get-current-team a-game)
          opposite-team (m/opposite-team current-team)
          g             (m/lose! a-game)]
      (is (= (q/get-winner g)
             opposite-team))
      (is (true? (q/winner? g))))))

(deftest update-remaining-works
  (let [initial-remaining     (q/get-remaining a-game)
        initial-red-remaining (:red initial-remaining)
        initial-hidden-total  (->> initial-remaining vals (apply +))
        ;; reveal a red, rather than a neutral or the assassin. blue would do just as well.
        a-red                 (q/get-a-red a-game)
        g                     (m/reveal! a-game a-red)
        g'                    (m/update-remaining! g)
        new-remaining         (q/get-remaining g')
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
    (let [a-word (-> a-game :words rand-nth :word)
          g      (m/reveal! a-game a-word)]
      (is (thrown? AssertionError (m/move! g a-word)))))
  (testing "assertions are thrown if there is already a winner"
    (let [a-word (-> a-game :words rand-nth :word)
          g      (m/win! a-game)]
      (is (thrown? AssertionError (m/move! g a-word))))))

(deftest move-reveals-the-selected-word
  (let [a-neutral               (q/get-a-neutral a-game)
        initial-revealed-status (->> a-neutral (q/get-a-particular-word a-game) :revealed?)
        g                       (m/move! a-game a-neutral)
        new-revealed-status     (->> a-neutral (q/get-a-particular-word g) :revealed?)]
    (is (false? initial-revealed-status))
    (is (not= initial-revealed-status new-revealed-status))
    (is (true? new-revealed-status))))

(deftest move-updates-remaining-words
  (let [initial-remaining (q/get-remaining a-game)
        a-red             (q/get-a-red a-game)
        g                 (m/move! a-game a-red)
        new-remaining     (q/get-remaining g)]
    (is (not= initial-remaining new-remaining))
    (is (= (:red initial-remaining)
           (inc (:red new-remaining))))))

(deftest picking-the-assassin-makes-you-lose
  (let [initial-winner (q/get-winner a-game)
        current-team   (q/get-current-team a-game)
        assassin-word  (q/get-the-assassin a-game)
        g              (m/move! a-game assassin-word)
        winner         (q/get-winner g)]
    (is (nil? initial-winner))
    (is (= winner
           (m/opposite-team current-team)))))

(deftest if-you-pick-someone-on-your-team-you-can-keep-moving
  (let [initial-round   (:round a-game)
        initial-team    (q/get-current-team a-game)
        good-guess-word (if (= :blue initial-team) (q/get-a-blue a-game) (q/get-a-red a-game))
        g               (m/move! a-game good-guess-word)
        new-round       (:round g)
        new-team        (q/get-current-team g)]
    (is (= initial-round new-round))
    (is (= initial-team new-team))))

(deftest picking-neutral-causes-it-to-be-the-other-teams-turn
  (let [a-neutral            (q/get-a-neutral a-game)
        initial-round        (:round a-game)
        initial-current-team (q/get-current-team a-game)    
        g                    (m/move! a-game a-neutral)
        new-round            (:round g)
        new-current-team     (q/get-current-team g)]
    (is (not= initial-current-team new-current-team))
    (is (= initial-current-team (m/opposite-team new-current-team)))
    (is (not= initial-round new-round))
    (is (< initial-round new-round))
    (is (= new-round (inc initial-round)))))

(deftest if-you-pick-someone-on-the-other-team-its-their-turn
  (let [initial-round  (:round a-game)
        initial-team   (q/get-current-team a-game)
        bad-guess-word (if (= :blue initial-team) (q/get-a-red a-game) (q/get-a-blue a-game))
        g              (m/move! a-game bad-guess-word)
        new-round      (:round g)
        new-team       (q/get-current-team g)]
    (is (not= initial-team new-team))
    (is (= initial-team (m/opposite-team new-team)))
    (is (not= initial-round new-round))
    (is (< initial-round new-round))
    (is (= new-round (inc initial-round)))))

(def one-blue-remaining
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

(def one-red-remaining
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

(deftest move-can-make-you-win-or-lose
  (testing "if there are no blue-remaining after your move, blue wins! (if you're blue, you win. if you're red, you lose.)"
    (let [initial-remaining (q/get-remaining one-blue-remaining)
          initial-winner    (q/winner? one-blue-remaining)
          current-team      (q/get-current-team one-blue-remaining)
          g                 (m/move! one-blue-remaining "SPINE")
          new-remaining     (q/get-remaining g)
          new-winner        (q/winner? g)
          winner            (q/get-winner g)]
      (is (= (:blue initial-remaining) 1))
      (is (false? initial-winner))
      (is (= current-team :red))
      (is (true? new-winner))
      (is (= (:blue new-remaining) 0))
      (is (not= current-team winner))
      (is (= winner :blue))))

  (testing "if there are no red-remaining after your move, red wins! (if you're red, you win. if you're blue, you lose.)"
    (let [initial-remaining (q/get-remaining one-red-remaining)
          initial-winner    (q/winner? one-red-remaining)
          current-team      (q/get-current-team one-red-remaining)
          g                 (m/move! one-red-remaining "STRIKE")
          new-remaining     (q/get-remaining g)
          new-winner        (q/winner? g)
          winner            (q/get-winner g)]
      (is (= (:red initial-remaining) 1))
      (is (false? initial-winner))
      (is (= current-team :red))
      (is (true? new-winner))
      (is (= (:red new-remaining) 0))
      (is (= current-team winner))
      (is (= winner :red)))))

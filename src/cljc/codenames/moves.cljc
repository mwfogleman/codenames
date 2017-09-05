(ns codenames.moves
  (:require [codenames.util :refer [in?]]
            [com.rpl.specter :as S]))

(defn word-filterer [w {:keys [word]}]
  (= word w))

(defn valid-word? [game word]
  (let [words (S/select [:words S/ALL :word] game)]
    (in? words word)))

(defn revealed? [game word]
  (S/select-any [:words (S/filterer #(word-filterer word %)) S/ALL :revealed?] game))

(def hidden? (complement revealed?))

(defn get-freqs [game]
  "Checks how many people are on which team, and how many people are revealed in a game. Running get-freqs on an initial game state should return something like the following:
{[:blue false] 9, [:red false] 8, [:neutral false] 7, [:assassin false] 1}"
  (let [words (S/select [:words S/ALL] game)
        get-attributes (juxt :identity :revealed?)]
    (->> words
         (map get-attributes)
         (frequencies))))

(defn reveal! [game word]
  (S/setval [:words (S/filterer #(word-filterer word %)) S/ALL :revealed?]
            true game))

(defn next-round! [game]
  (S/transform [:round] inc game))

(defn get-current-team
  [game]
  (S/select-any [:current-team] game))

(defn opposite-team [team]
  {:pre [(keyword? team)
         (or (= team :red)
             (= team :blue))]}
  (if (= team :red)
    :blue
    :red))

(defn switch-teams! [game]
  (S/transform [:current-team] opposite-team game))

(defn next-turn! [game]
  (-> game next-round! switch-teams!))

(defn set-winner!
  [game winner]
  (S/setval [:winning-team] winner game))

(defn win!
  "Makes the current team win the game."
  [game]
  (let [winner (get-current-team game)]
    (set-winner! game winner)))

(defn lose!
  "Makes the current team lose the game."
  [game]
  (let [loser (get-current-team game)
        winner (opposite-team loser)]
    (set-winner! game winner)))

(def get-winner :winning-team)

(defn winner?
  "If a GAME has a winner, return true. If not, return false."
  [game]
  (->> game
       get-winner
       some?))

(defn cell-filterer
  [target {:keys [position]}]
  (= target position))

(defn get-cell
  [game x y]
  (S/select-any [:words (S/filterer #(cell-filterer [x y] %)) S/ALL] game))

(def get-current-team :current-team)

(defn get-revealed-status
  [game x y]
  (:revealed? (get-cell game x y)))

(defn get-id-of-word [game word]
  (S/select-any [:words (S/filterer #(word-filterer word %)) S/ALL :identity] game))

(def get-remaining :remaining)

(defn update-remaining!
  [game]
  (let [frqs           (get-freqs game)
        blue-remaining (get frqs [:blue false] 0)
        red-remaining  (get frqs [:red false] 0)]
    (->> (S/setval [:remaining :blue] blue-remaining game)
         (S/setval [:remaining :red] red-remaining))))

(defn move! [game word]
  {:pre [(valid-word? game word)
         (hidden? game word)
         (= false (winner? game))]}
  (let [g                  (-> game (reveal! word) (update-remaining!))
        current-team       (get-current-team g)
        id                 (get-id-of-word g word)
        match-result       (= id current-team) ;; Register whether they picked someone on their team, or on the other team.
        {:keys [blue red]} (get-remaining g)]
    (cond (= id :assassin) (lose! g)
          (and (> blue 0) (> red 0)) ;; Check if there are remaining hidden cards for either team.
          ;; If they picked someone on their team, they can keep moving
          ;; If they picked someone from the other team, switch to make it the other team's turn.
          (if (true? match-result)
            g
            (next-turn! g))
          :else
          (if (true? match-result)
            ;; If the card picked was theirs, win!
            (win! g)
            ;; Otherwise, lose!
            (lose! g)))))

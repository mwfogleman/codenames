(ns codenames.moves
  (:require [codenames.game :as game]
            [com.rpl.specter :as S]))

(defn in?
  "True if the collection contains the element."
  [collection element]
  (some #(= element %) collection))

(defn word-filterer [w {:keys [word]}]
  (= word w))

(defn valid-word? [game word]
  (let [words (S/select [S/ATOM :words S/ALL :word] game)]
    (in? words word)))

(defn revealed? [game word]
  (S/select-any [S/ATOM :words (S/filterer #(word-filterer word %)) S/ALL :revealed?] game))

(def hidden? (complement revealed?))

(defn get-freqs [game]
  (let [words (S/select [S/ATOM :words S/ALL] game)
        get-attributes (juxt :identity :revealed?)]
    (->> words
         (map get-attributes)
         (frequencies))))

(defn reveal! [game word]
  (S/setval [S/ATOM :words (S/filterer #(word-filterer word %)) S/ALL :revealed?]
            true game))

(defn next-round! [game]
  (S/transform [S/ATOM :round] inc game))

(defn opposite-team [team]
  (if (= team :red)
    :blue
    :red))

(defn switch-teams! [game]
  (S/transform [S/ATOM :current-team] opposite-team game))

(defn next-turn! [game]
  (next-round! game)
  (switch-teams! game))

(defn win!
  "Makes the current team win the game."
  [game]
  (let [winner (S/select-any [S/ATOM :current-team] game)]
    (S/setval [S/ATOM :winning-team] winner game)))

(defn lose!
  "Makes the current team lose the game."
  [game]
  (let [loser  (S/select-any [S/ATOM :current-team] game)
        winner (opposite-team loser)]
    (S/setval [S/ATOM :winning-team] winner game)))

(defn winner?
  "If a GAME has a winner, return true. If not, return false."
  [game]
  (->> game
       (S/select-any [S/ATOM :winning-team])
       (some?)))

(defn get-current-team
  [game]
  (S/select-any [S/ATOM :current-team] game))

(defn- get-id-of-word [game word]
  (S/select-any [S/ATOM :words (S/filterer #(word-filterer word %)) S/ALL :identity] game))

(defn get-remaining
  [game]
  (S/select-any [S/ATOM (S/submap [:blue-remaining :red-remaining])] game))

(defn update-remaining!
  [game]
  (let [frqs           (get-freqs game)
        blue-remaining (get frqs [:blue false])
        red-remaining  (get frqs [:red false])]
    (S/setval [S/ATOM :blue-remaining] blue-remaining game)
    (S/setval [S/ATOM :red-remaining]  red-remaining game)))

(defn move! [game word]
  {:pre [(valid-word? game word)
         (hidden? game word)
         (= false (winner? game))]}
  (reveal! game word)
  (update-remaining! game)
  (let [current-team                           (get-current-team game)
        id                                     (get-id-of-word game word)
        match-result                           (= id current-team) ;; Register whether they picked someone on their team, or on the other team.
        {:keys [blue-remaining red-remaining]} (get-remaining game)]
    (cond (= id :assassin) (lose! game)
          (and (> blue-remaining 0) (> red-remaining 0)) ;; Check if there are remaining hidden cards for either team.
          ;; If they picked someone on their team, they can keep moving
          ;; If they picked someone from the other team, switch to make it the other team's turn.
          (if (true? match-result)
            game
            (next-turn! game))
          :else
          (if (true? match-result)
            ;; If the card picked was theirs, win!
            (win! game)
            ;; Otherwise, lose!
            (lose! game)))))

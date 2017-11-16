(ns codenames.moves
  (:require [codenames.queries :as q]
            [com.rpl.specter :as S]))

(defn switch-view! [view]
  (if (= view :player)
    :spymaster
    :player))

(defn reveal! [game word]
  (S/setval [:words (S/filterer #(q/word-filterer word %)) S/ALL :revealed?]
            true game))

(defn next-round! [game]
  (S/transform [:round] inc game))

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
  (let [winner (:current-team game)]
    (set-winner! game winner)))

(defn lose!
  "Makes the current team lose the game."
  [game]
  (let [loser (:current-team game)
        winner (opposite-team loser)]
    (set-winner! game winner)))

(defn update-remaining!
  [game]
  (let [frqs           (q/get-freqs game)
        blue-remaining (get frqs [:blue false] 0)
        red-remaining  (get frqs [:red false] 0)]
    (->> (S/setval [:remaining :blue] blue-remaining game)
         (S/setval [:remaining :red] red-remaining))))

(defn move! [game word]
  {:pre [(q/valid-word? game word)
         (q/hidden? game word)
         (= false (q/winner? game))]}
  (let [g                  (-> game (reveal! word) (update-remaining!))
        assassin?          (q/assassin? g word)
        match?             (q/word-id-matches-current-team? g word)
        {:keys [blue red]} (:remaining g)]
    (cond assassin? (lose! g)
          ;; Check if there are remaining hidden cards for either team.
          ;; If they picked someone on their team, they can keep moving
          ;; If they picked someone from the other team, switch to make it the other team's turn.
          (and (> blue 0) (> red 0)) (if match? g (next-turn! g))
          ;; If the card picked was theirs, win!
          ;; Otherwise, lose!
          :else (if match? (win! g) (lose! g)))))

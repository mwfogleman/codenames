(ns codenames.moves
  (:require [codenames.queries :as q]
            [com.rpl.specter :as S]))

(defn switch-view! [game]
  (let [view (:view game)]
    (if (= view :player)
      (assoc game :view :spymaster)
      (assoc game :view :player))))

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
  (let [winner (q/get-current-team game)]
    (set-winner! game winner)))

(defn lose!
  "Makes the current team lose the game."
  [game]
  (let [loser (q/get-current-team game)
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
        current-team       (q/get-current-team g)
        id                 (q/get-id-of-word g word)
        match-result       (= id current-team) ;; Register whether they picked someone on their team, or on the other team.
        {:keys [blue red]} (q/get-remaining g)]
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

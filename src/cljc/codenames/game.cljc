(ns codenames.game
  (:require [codenames.dictionary :as dict]))

(def teams [:red :blue])

(defn set-alliances
  "In each game, there should be: 1 :assassin, 9 of the starting team (e.g., :red), 8 of the next team (e.g., :blue), and 7 civilians (:neutral). Return a sequence with those amounts of the keywords, as well as a map that says who the starting team is."
  []
  (let [[fst snd] (shuffle teams)
        remaining-map (hash-map fst 9 snd 8)
        m {:starting-team fst
           :current-team fst
           :remaining remaining-map}]
    (cons m (reduce concat [(repeat 9 fst)
                            (repeat 8 snd)
                            (repeat 7 :neutral)
                            [:assassin]]))))

(defn get-words
  []
  (->> dict/dictionary
       shuffle
       (take 25)))

(defn prepare-game
  "Creates a new game of CODENAMES." ;; to be called with atom or r/atom
  []
  (let [[alliance-map & alliances] (set-alliances)
        metadata-init {:winning-team nil
                       :round 0}
        metadata (merge alliance-map metadata-init)
        coords (shuffle (for [x (range 5) y (range 5)] (vector x y)))
        mapper (fn [[id coord wd]] {:word wd
                                   :identity id
                                   :revealed? false
                                   :position coord})]

    (->> (get-words)
         (interleave alliances coords)
         (partition 3)
         (map mapper)
         (hash-map :words)
         (merge metadata))))

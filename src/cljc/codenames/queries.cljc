(ns codenames.queries
  (:require [codenames.util :refer [in?]]
            [com.rpl.specter :as S]))

(defn get-a-particular-word [game word]
  (S/select-any [:words S/ALL (S/if-path [:word (S/pred= word)] S/STAY)] game))

(defn get-a-word-of-identity [identity]
  (fn [game]
    (S/select-any [:words S/ALL (S/if-path [:identity (S/pred= identity)] :word)] game)))

(def get-the-assassin (get-a-word-of-identity :assassin))

(def get-a-red (get-a-word-of-identity :red))

(def get-a-blue (get-a-word-of-identity :blue))

(def get-a-neutral (get-a-word-of-identity :neutral))

(defn assassin? [game word]
  (= (:identity (get-a-particular-word game word)) :assassin))

(defn valid-word? [game word]
  (let [words (S/select [:words S/ALL :word] game)]
    (in? words word)))

(defn word-filterer [w {:keys [word]}]
  (= word w))

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

(defn cell-filterer
  [target {:keys [position]}]
  (= target position))

(defn get-cell
  [game x y]
  (S/select-any [:words (S/filterer #(cell-filterer [x y] %)) S/ALL] game))

(defn get-revealed-status
  [game x y]
  (:revealed? (get-cell game x y)))

(defn get-id-of-word [game word]
  (S/select-any [:words (S/filterer #(word-filterer word %)) S/ALL :identity] game))

(defn word-id-matches-current-team? [game word]
  (let [word-id      (get-id-of-word game word)
        current-team (:current-team game)]
    (= word-id current-team)))

(defn winner?
  "If a GAME has a winner, return true. If not, return false."
  [game]
  (->> game
       :winning-team
       some?))

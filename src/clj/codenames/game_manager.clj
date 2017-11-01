(ns codenames.game-manager
  (:require [codenames.game :refer [prepare-game]]))

(def games (atom {}))

(defn game-exists? [id]
  (contains? @games id))

(defn create-game! [id]
  (swap! games assoc id (prepare-game)))

(defn get-game [id]
  (when-not (game-exists? id)
    (create-game! id))
  (get @games id))

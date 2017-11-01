(ns codenames.game-manager
  (:require [codenames.game :refer [prepare-game]]
            [com.rpl.specter :refer :all]
            [clj-time.core :as t]
            [schema.core :as s]))

(def games (atom {}))

(defn game-exists? [id]
  (contains? @games id))

(defn create-game! [id]
  (setval [ATOM (keypath id)] {:game (prepare-game)
                               :created-at (t/now)} games))

(defn get-game [id]
  (when-not (game-exists? id)
    (create-game! id))
  (select-any [ATOM (keypath id) :game] games))

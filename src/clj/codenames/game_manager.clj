(ns codenames.game-manager
  (:require [clj-time.core :as t]
            [codenames.game :refer [prepare-game]]
            [com.rpl.specter :refer :all]))

(def games (atom {}))

;; Paths

(defn game-path [id] (path ATOM (keypath id)))

;; Query Games

(defn get-game-state [id]
  (select-any [(game-path id) :state] games))

(defn get-game-creation-time [id]
  (select-any [(game-path id) :created-at] games))

(defn get-stale-games []
  (filter #(t/after? (t/yesterday) (get-game-creation-time %)) (get-game-keys)))

;; Create and Update Games

(defn create-game! [id]
  (setval (game-path id) {:state (prepare-game)
                          :created-at (t/now)} games))

(def reset-game! create-game!)

(defn get-game! [id]
  (when-not (contains? @games id)
    (create-game! id))
  (get-game-state id))

(defn delete-game! [id]
  (setval (game-path id) NONE games))

(defn delete-all-games! []
  (map delete-game! (keys @games)))

(defn delete-stale-games! []
  (map delete-game! (get-stale-games)))

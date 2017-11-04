(ns codenames.game-manager
  (:require [clj-time.core :as t]
            [codenames.game :refer [prepare-game]]
            [com.rpl.specter :refer :all]))

(def games (atom {}))

;; Query Games

(defn game-exists? [id]
  (contains? @games id))

(defn get-game-keys []
  (-> @games keys))

(defn get-game-state [id]
  (select-any [ATOM (keypath id) :state] games))

(defn get-game-creation-time [id]
  (select-any [ATOM (keypath id) :created-at] games))

(defn stale-game? [id]
  (let [t (get-game-creation-time id)]
    (t/after? (t/yesterday) t)))

(defn get-stale-games []
  (filter stale-game? (get-game-keys)))

;; Create and Update Games

(defn create-game! [id]
  (setval [ATOM (keypath id)] {:state (prepare-game)
                               :created-at (t/now)} games))

(def reset-game! create-game!)

(defn get-game! [id]
  (when-not (game-exists? id)
    (create-game! id))
  (get-game-state id))

(defn delete-game! [id]
  (setval [ATOM (keypath id)] NONE games))

(defn delete-all-games! []
  (map delete-game! (get-game-keys)))

(defn delete-stale-games! []
  (map delete-game! (get-stale-games)))

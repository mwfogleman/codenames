(ns codenames.game-manager
  (:require [clj-time.core :as t]
            [codenames.game :refer [prepare-game]]
            [com.rpl.specter :refer :all]
            [immutant.scheduling :as scheduling]))

(def games (atom {}))

;; Paths

(defn game-path [id] (path ATOM (keypath id)))

;; Query Games

(defn get-game-state [m id]
  (select-any [(game-path id) :state] m))

(defn get-game-creation-time [m id]
  (select-any [(game-path id) :created-at] m))

(defn get-stale-games [m]
  (filter #(t/after? (t/yesterday) (get-game-creation-time m %)) (-> @m keys)))

;; Create and Update Games

(defn create-game! [m id]
  (setval (game-path id) {:state (prepare-game)
                          :created-at (t/now)} m))

(defn get-game! [m id]
  (when-not (contains? @m id)
    (create-game! m id))
  (get-game-state m id))

(defn delete-game! [m id]
  (setval (game-path id) NONE m))

(defn delete-all-games! [m]
  (map #(delete-game! m %) (keys @m)))

(defn delete-stale-games! [m]
  (map #(delete-game! m %) (get-stale-games m)))

(defn job []
  (when-not (empty? @games)
    (delete-stale-games! games)))

(scheduling/schedule job :every :day)

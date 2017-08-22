(ns codenames.dictionary
  (:require [clojure.string :as str]))

(defn slurper [file]
  (-> file slurp str/split-lines))

(def default-dictionary-location
  "The original CODENAMES dictionary."
  "resources/dictionary.txt")

(def dictionary (slurper default-dictionary-location))

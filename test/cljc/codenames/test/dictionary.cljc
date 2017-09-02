(ns codenames.test.dictionary
  #?(:clj
     (:require [codenames.dictionary :as dict :refer [dictionary]]
               [codenames.util :refer [in?]]
               [clojure.test :refer :all])
     :cljs
     (:require [codenames.dictionary :as dict :refer [dictionary]]
               [codenames.util :refer [in?]]
               [cljs.test :refer-macros [deftest is]])))

(deftest dictionary-is-not-empty
  (is (some? dictionary)))

(deftest africa-comes-first
  (is (-> dictionary first (= "AFRICA"))))

(deftest witches-are-present
  (is (in? dictionary "WITCH")))


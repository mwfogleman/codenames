(ns codenames.test.util
  #?(:clj
     (:require [codenames.util :refer :all]
               [clojure.test :refer :all])
     :cljs
     (:require [codenames.util :refer [in?]]
               [cljs.test :refer-macros [deftest is]])))

(deftest test-in
  (is (in? [0 19 2 3 1 22] 1))
  (is (in? [:href :style :p :a :span] :style))
  (is (false? (in? [:href :style :p :a :span] :on-click)))
  (is (in? [{:a 1 :b 2} {:a 2 :b 3} {:a 4 :b 5}] {:b 3 :a 2})))

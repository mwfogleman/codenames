(ns codenames.test.game-manager
  (:require [codenames.game :as game]
            [codenames.game-manager :as manager]
            [codenames.moves :as m]
            [codenames.queries :as q]
            [codenames.util :refer [in?]]
            [com.rpl.specter :as S]
            [clojure.test :refer :all]))

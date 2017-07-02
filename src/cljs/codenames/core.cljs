(ns codenames.core
  (:require [cljs-time.core :as t]
            [com.rpl.specter :as S]
            [reagent.core :as reagent]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]))

;; Dictionary

(def dictionary ["AFRICA"
                 "AGENT"
                 "AIR"
                 "ALIEN"
                 "ALPS"
                 "AMAZON"
                 "AMBULANCE"
                 "AMERICA"
                 "ANGEL"
                 "ANTARCTICA"
                 "APPLE"
                 "ARM"
                 "ATLANTIS"
                 "AUSTRALIA"
                 "AZTEC"
                 "BACK"
                 "BALL"
                 "BAND"
                 "BANK"
                 "BAR"
                 "BARK"
                 "BAT"
                 "BATTERY"
                 "BEACH"
                 "BEAR"
                 "BEAT"
                 "BED"
                 "BEIJING"
                 "BELL"
                 "BELT"
                 "BERLIN"
                 "BERMUDA"
                 "BERRY"
                 "BILL"
                 "BLOCK"
                 "BOARD"
                 "BOLT"
                 "BOMB"
                 "BOND"
                 "BOOM"
                 "BOOT"
                 "BOTTLE"
                 "BOW"
                 "BOX"
                 "BRIDGE"
                 "BRUSH"
                 "BUCK"
                 "BUFFALO"
                 "BUG"
                 "BUGLE"
                 "BUTTON"
                 "CALF"
                 "CANADA"
                 "CAP"
                 "CAPITAL"
                 "CAR"
                 "CARD"
                 "CARROT"
                 "CASINO"
                 "CAST"
                 "CAT"
                 "CELL"
                 "CENTAUR"
                 "CENTER"
                 "CHAIR"
                 "CHANGE"
                 "CHARGE"
                 "CHECK"
                 "CHEST"
                 "CHICK"
                 "CHINA"
                 "CHOCOLATE"
                 "CHURCH"
                 "CIRCLE"
                 "CLIFF"
                 "CLOAK"
                 "CLUB"
                 "CODE"
                 "COLD"
                 "COMIC"
                 "COMPOUND"
                 "CONCERT"
                 "CONDUCTOR"
                 "CONTRACT"
                 "COOK"
                 "COPPER"
                 "COTTON"
                 "COURT"
                 "COVER"
                 "CRANE"
                 "CRASH"
                 "CRICKET"
                 "CROSS"
                 "CROWN"
                 "CYCLE"
                 "CZECH"
                 "DANCE"
                 "DATE"
                 "DAY"
                 "DEATH"
                 "DECK"
                 "DEGREE"
                 "DIAMOND"
                 "DICE"
                 "DINOSAUR"
                 "DISEASE"
                 "DOCTOR"
                 "DOG"
                 "DRAFT"
                 "DRAGON"
                 "DRESS"
                 "DRILL"
                 "DROP"
                 "DUCK"
                 "DWARF"
                 "EAGLE"
                 "EGYPT"
                 "EMBASSY"
                 "ENGINE"
                 "ENGLAND"
                 "EUROPE"
                 "EYE"
                 "FACE"
                 "FAIR"
                 "FALL"
                 "FAN"
                 "FENCE"
                 "FIELD"
                 "FIGHTER"
                 "FIGURE"
                 "FILE"
                 "FILM"
                 "FIRE"
                 "FISH"
                 "FLUTE"
                 "FLY"
                 "FOOT"
                 "FORCE"
                 "FOREST"
                 "FORK"
                 "FRANCE"
                 "GAME"
                 "GAS"
                 "GENIUS"
                 "GERMANY"
                 "GHOST"
                 "GIANT"
                 "GLASS"
                 "GLOVE"
                 "GOLD"
                 "GRACE"
                 "GRASS"
                 "GREECE"
                 "GREEN"
                 "GROUND"
                 "HAM"
                 "HAND"
                 "HAWK"
                 "HEAD"
                 "HEART"
                 "HELICOPTER"
                 "HIMALAYAS"
                 "HOLE"
                 "HOLLYWOOD"
                 "HONEY"
                 "HOOD"
                 "HOOK"
                 "HORN"
                 "HORSE"
                 "HORSESHOE"
                 "HOSPITAL"
                 "HOTEL"
                 "ICE"
                 "ICE CREAM"
                 "INDIA"
                 "IRON"
                 "IVORY"
                 "JACK"
                 "JAM"
                 "JET"
                 "JUPITER"
                 "KANGAROO"
                 "KETCHUP"
                 "KEY"
                 "KID"
                 "KING"
                 "KIWI"
                 "KNIFE"
                 "KNIGHT"
                 "LAB"
                 "LAP"
                 "LASER"
                 "LAWYER"
                 "LEAD"
                 "LEMON"
                 "LEPRECHAUN"
                 "LIFE"
                 "LIGHT"
                 "LIMOUSINE"
                 "LINE"
                 "LINK"
                 "LION"
                 "LITTER"
                 "LOCH NESS"
                 "LOCK"
                 "LOG"
                 "LONDON"
                 "LUCK"
                 "MAIL"
                 "MAMMOTH"
                 "MAPLE"
                 "MARBLE"
                 "MARCH"
                 "MASS"
                 "MATCH"
                 "MERCURY"
                 "MEXICO"
                 "MICROSCOPE"
                 "MILLIONAIRE"
                 "MINE"
                 "MINT"
                 "MISSILE"
                 "MODEL"
                 "MOLE"
                 "MOON"
                 "MOSCOW"
                 "MOUNT"
                 "MOUSE"
                 "MOUTH"
                 "MUG"
                 "NAIL"
                 "NEEDLE"
                 "NET"
                 "NEW YORK"
                 "NIGHT"
                 "NINJA"
                 "NOTE"
                 "NOVEL"
                 "NURSE"
                 "NUT"
                 "OCTOPUS"
                 "OIL"
                 "OLIVE"
                 "OLYMPUS"
                 "OPERA"
                 "ORANGE"
                 "ORGAN"
                 "PALM"
                 "PAN"
                 "PANTS"
                 "PAPER"
                 "PARACHUTE"
                 "PARK"
                 "PART"
                 "PASS"
                 "PASTE"
                 "PENGUIN"
                 "PHOENIX"
                 "PIANO"
                 "PIE"
                 "PILOT"
                 "PIN"
                 "PIPE"
                 "PIRATE"
                 "PISTOL"
                 "PIT"
                 "PITCH"
                 "PLANE"
                 "PLASTIC"
                 "PLATE"
                 "PLATYPUS"
                 "PLAY"
                 "PLOT"
                 "POINT"
                 "POISON"
                 "POLE"
                 "POLICE"
                 "POOL"
                 "PORT"
                 "POST"
                 "POUND"
                 "PRESS"
                 "PRINCESS"
                 "PUMPKIN"
                 "PUPIL"
                 "PYRAMID"
                 "QUEEN"
                 "RABBIT"
                 "RACKET"
                 "RAY"
                 "REVOLUTION"
                 "RING"
                 "ROBIN"
                 "ROBOT"
                 "ROCK"
                 "ROME"
                 "ROOT"
                 "ROSE"
                 "ROULETTE"
                 "ROUND"
                 "ROW"
                 "RULER"
                 "SATELLITE"
                 "SATURN"
                 "SCALE"
                 "SCHOOL"
                 "SCIENTIST"
                 "SCORPION"
                 "SCREEN"
                 "SCUBA DIVER"
                 "SEAL"
                 "SERVER"
                 "SHADOW"
                 "SHAKESPEARE"
                 "SHARK"
                 "SHIP"
                 "SHOE"
                 "SHOP"
                 "SHOT"
                 "SINK"
                 "SKYSCRAPER"
                 "SLIP"
                 "SLUG"
                 "SMUGGLER"
                 "SNOW"
                 "SNOWMAN"
                 "SOCK"
                 "SOLDIER"
                 "SOUL"
                 "SOUND"
                 "SPACE"
                 "SPELL"
                 "SPIDER"
                 "SPIKE"
                 "SPINE"
                 "SPOT"
                 "SPRING"
                 "SPY"
                 "SQUARE"
                 "STADIUM"
                 "STAFF"
                 "STAR"
                 "STATE"
                 "STICK"
                 "STOCK"
                 "STRAW"
                 "STREAM"
                 "STRIKE"
                 "STRING"
                 "SUB"
                 "SUIT"
                 "SUPERHERO"
                 "SWING"
                 "SWITCH"
                 "TABLE"
                 "TABLET"
                 "TAG"
                 "TAIL"
                 "TAP"
                 "TEACHER"
                 "TELESCOPE"
                 "TEMPLE"
                 "THEATER"
                 "THIEF"
                 "THUMB"
                 "TICK"
                 "TIE"
                 "TIME"
                 "TOKYO"
                 "TOOTH"
                 "TORCH"
                 "TOWER"
                 "TRACK"
                 "TRAIN"
                 "TRIANGLE"
                 "TRIP"
                 "TRUNK"
                 "TUBE"
                 "TURKEY"
                 "UNDERTAKER"
                 "UNICORN"
                 "VACUUM"
                 "VAN"
                 "VET"
                 "WAKE"
                 "WALL"
                 "WAR"
                 "WASHER"
                 "WASHINGTON"
                 "WATCH"
                 "WATER"
                 "WAVE"
                 "WEB"
                 "WELL"
                 "WHALE"
                 "WHIP"
                 "WIND"
                 "WITCH"
                 "WORM"
                 "YARD"])

;; Game Generation

(def teams [:red :blue])

(defn set-alliances
  "In each game, there should be: 1 :assassin, 9 of the starting team (e.g., :red), 8 of the next team (e.g., :blue), and 7 civilians (:neutral). Return a sequence with those amounts of the keywords, as well as a map that says who the starting team is."
  []
  (let [[fst snd] (shuffle teams)
        keywordizer (fn [team] (-> team (name) (str "-remaining") (keyword)))
        remaining-map (hash-map (keywordizer fst) 9 (keywordizer snd) 8)
        m (merge remaining-map {:starting-team fst
                                :current-team fst
                                :view :player})]
    (cons m (reduce concat [(repeat 9 fst)
                            (repeat 8 snd)
                            (repeat 7 :neutral)
                            [:assassin]]))))

(defn get-words [] (->> dictionary shuffle (take 25)))

(defn prepare-game
  "Creates a new game of CODENAMES."
  []
  (let [[alliance-map & alliances] (set-alliances)
        metadata-init {:winning-team nil
                       :id (str (gensym))
                       :created-at (t/now)
                       :round 0}
        metadata (merge alliance-map metadata-init)
        coords (shuffle (for [x (range 5) y (range 5)] (vector x y)))
        mapper (fn [[id coord wd]] {:word wd
                                   :identity id
                                   :revealed? false
                                   :position coord})]

    (->> (get-words)
         (interleave alliances coords)
         (partition 3)
         (map mapper)
         (hash-map :words)
         (merge metadata))))

(defonce game (reagent/atom (prepare-game)))

(defn new-game! [game]
  (reset! game (prepare-game)))

;; Game Play

(defn in?
  "True if the collection contains the element."
  [collection element]
  (some #(= element %) collection))

(defn word-filterer [w {:keys [word]}]
  (= word w))

(defn valid-word? [game word]
  (let [words (S/select [S/ATOM :words S/ALL :word] game)]
    (in? words word)))

(defn revealed? [game word]
  (S/select-any [S/ATOM :words (S/filterer #(word-filterer word %)) S/ALL :revealed?] game))

(def hidden? (complement revealed?))

(defn get-freqs [game]
  (let [words (S/select [S/ATOM :words S/ALL] game)
        get-attributes (juxt :identity :revealed?)]
    (->> words
         (map get-attributes)
         (frequencies))))

(defn reveal! [game word]
  (S/setval [S/ATOM :words (S/filterer #(word-filterer word %)) S/ALL :revealed?]
            true game))

(defn next-round! [game]
  (S/transform [S/ATOM :round] inc game))

(defn opposite-team [team]
  (if (= team :red)
    :blue
    :red))

(defn switch-teams! [game]
  (S/transform [S/ATOM :current-team] opposite-team game))

(defn next-turn! [game]
  (next-round! game)
  (switch-teams! game))

(defn win!
  "Makes the current team win the game."
  [game]
  (let [winner (S/select-any [S/ATOM :current-team] game)]
    (S/setval [S/ATOM :winning-team] winner game)))

(defn lose!
  "Makes the current team lose the game."
  [game]
  (let [loser  (S/select-any [S/ATOM :current-team] game)
        winner (opposite-team loser)]
    (S/setval [S/ATOM :winning-team] winner game)))

(defn winner?
  "If a GAME has a winner, return true. If not, return false."
  [game]
  (->> game
       (S/select-any [S/ATOM :winning-team])
       (some?)))

(defn cell-filterer
  [target {:keys [position]}]
  (= target position))

(defn get-cell
  [game x y]
  (S/select-any [:words (S/filterer #(cell-filterer [x y] %)) S/ALL] @game))

(defn get-current-team
  [game]
  (:current-team @game))

(defn get-revealed-status
  [game x y]
  (:revealed? (get-cell game x y)))

(defn get-view
  [game]
  (:view @game))

(defn get-winner
  [game]
  (:winning-team @game))

(defn- get-id-of-word [game word]
  (S/select-any [S/ATOM :words (S/filterer #(word-filterer word %)) S/ALL :identity] game))

(defn get-remaining
  [game]
  (S/select-any [S/ATOM (S/submap [:blue-remaining :red-remaining])] game))

(defn update-remaining!
  [game]
  (let [frqs           (get-freqs game)
        blue-remaining (get frqs [:blue false])
        red-remaining  (get frqs [:red false])]
    (S/setval [S/ATOM :blue-remaining] blue-remaining game)
    (S/setval [S/ATOM :red-remaining]  red-remaining game)))

(defn move! [game word]
  {:pre [(valid-word? game word)
         (hidden? game word)
         (= false (winner? game))]}
  (reveal! game word)
  (update-remaining! game)
  (let [current-team                           (get-current-team game)
        id                                     (get-id-of-word game word)
        match-result                           (= id current-team) ;; Register whether they picked someone on their team, or on the other team.
        {:keys [blue-remaining red-remaining]} (get-remaining game)]
    (cond (= id :assassin) (lose! game)
          (and (> blue-remaining 0) (> red-remaining 0)) ;; Check if there are remaining hidden cards for either team.
          ;; If they picked someone on their team, they can keep moving
          ;; If they picked someone from the other team, switch to make it the other team's turn.
          (if (true? match-result)
            game
            (next-turn! game))
          :else
          (if (true? match-result)
            ;; If the card picked was theirs, win!
            (win! game)
            ;; Otherwise, lose!
            (lose! game)))))

;; -------------------------
;; Views

(defn colorize [word identity]
  (case identity
    :blue     [:div {:style {:color "blue"}}
               word]
    :red      [:div {:style {:color "red"}}
               word]
    :assassin [:div {:style {:color "grey"}}
               word]
    :neutral  [:div {:style {:color "yellow"}}
               word]))

(defn cell [game x y]
  (let [{:keys [word identity revealed?]} (get-cell game x y)
        winner                            (get-winner game)]
    (fn []
      (if winner
        [:span
         [colorize word identity]]
        (if (true? revealed?)
          [:span {:style {:width 30
                          :height 30}}
           [colorize word identity]]
          [:button {:on-click #(move! game word)
                    :style {:width 100
                            :height 100}}
           [:div {:style {:color "black"}}
            word]])))))

(defn grid [game]
  [:table
   (for [y (range 5)]
     [:tr
      (for [x (range 5)]
        [:td {:style {:width      100
                      :height     100
                      :text-align :center}}
         [cell game x y]])])])

(defn main-panel [game]
  (let [turn   (get-current-team game)
        winner (get-winner game)]
    (fn []
      [:div
       (if winner
         [:div
          (clojure.string/capitalize (name winner)) " is the winner."]
         [:div
          "It's " (name turn) "'s turn."])
       [:center
        [:p
         [grid game]]]
       [:p
        game]])))

(defn home-page []
  [:div [:h2 "Welcome to Codenames"]
   [:div [:a {:href "/game"} "Play a game!"]]])

(defn game-page []
  (let [game (reagent/atom (prepare-game))]
    [:div [:h2 "Codenames"]
     [main-panel game]]))

;; -------------------------
;; Routes

(def page (reagent/atom #'game-page))

(defn current-page []
  [:div [@page]])

(secretary/defroute "/" []
  (reset! page #'game-page))

(secretary/defroute "/game" []
  (reset! page #'game-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))

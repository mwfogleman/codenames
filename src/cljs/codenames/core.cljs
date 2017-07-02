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

(defn new-game! []
  (reset! game (prepare-game)))

;; Game Play

(defn in?
  "True if the collection contains the element."
  [collection element]
  (some #(= element %) collection))

(defn opposite-team [team]
  (if (= team :red)
    :blue
    :red))

(defn winner?
  "If a game has a winner, return true. If not, return false."
  []
  (some? (:winning-team @game)))

;; DELETED FUNCTIONS
;; word-filterer
;; valid-word?
;; revealed?
;; hidden?
;; get-freqs
;; reveal!
;; next-round!

(defn next-round! []
  ;; (S/transform [S/ATOM :round] inc game)
  (swap! game update :round inc))

;; opposite-team
;; switch-teams!
;; next-turn!
;; win!
;; lose!
;; winner?
;; cell-filterer
;; get-cell
;; get-current-team
;; get-revealed-status
;; get-view
;; get-winner
;; get-id-of-word
;; get-remaining
;; update-remaining!
;; move!
;; colorize
;; cell
;; grid
;; main-panel

;; -------------------------
;; Views

(defn home-page []
  [:div [:h2 "Codenames"]])

;; -------------------------
;; Routes

(def page (reagent/atom #'home-page))

(defn current-page []
  [:div [@page]])

(secretary/defroute "/" []
  (reset! page #'home-page))

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

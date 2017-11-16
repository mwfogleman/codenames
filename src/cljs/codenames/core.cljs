(ns codenames.core
  (:require [accountant.core :as accountant]
            [clojure.string :as str]
            [codenames.game :as g]
            [codenames.moves :as m]
            [codenames.queries :as q]
            [reagent.core :as reagent :refer [atom]]
            [secretary.core :as secretary :include-macros true]))

;; -------------------------
;; Views

(defn title-bar []
  [:div [:h2 "Codenames"]])

(defn colorize-team [team]
  (let [pr (if (= team :blue)
             [:span.blue]
             [:span.red])]
    (->> team name (conj pr))))

(defn game-status-bar [game]
  (let [g      @game
        turn   (:current-team g)
        winner (:winning-team g)]
    [:div#game-status
     (if winner
       [:span "The " (colorize-team winner) " team wins!"]
       [:span "It's the " (colorize-team turn) " team's turn."])]))

(defn remaining [game]
  (fn []
    (let [g      @game
          red    (-> g :remaining :red)
          r-span [:span.red red]
          blue   (-> g :remaining :blue)
          b-span [:span.blue blue]]
      [:span#remaining
       (if (> red blue)
         [:span
          r-span " - " b-span]
         [:span
          b-span " - " r-span])])))

(defn next-turn-button [game]
  (fn []
    (let [g    @game
          turn (:current-team g)]
      [:button#next-turn.game {:on-click #(swap! game m/next-turn!)}
       "End the " (name turn) " team's turn."])))

(defn turn-status-bar [game view]
  (fn []
    (let [g @game
          v @view
          w (q/winner? g)]
      (if (and (= v :player) (false? w))
        [:div
         [remaining game]
         [next-turn-button game]]
        [remaining game]))))

(defn view-toggle [view]
  (fn []
    (let [v @view]
      [:button#view.game {:on-click #(swap! view m/switch-view!)}
       (if (= v :player)
         "Spymaster"
         "Player")])))

(defn reset-button [game]
  (fn []
    (let [g @game]
      [:button#reset.game {:on-click #(reset! game (g/prepare-game))}
       "Next Game"])))

(defn gameplay-bar [game view]
  (fn []
    (let [g        @game
          v        @view
          in-play? (nil? (:winning-team g))]
      [:div#gameplay-bar
       (when in-play? [view-toggle view])
       [reset-button game]])))

(defn colorize [word identity]
  (case identity
    :blue     [:div.blue word]
    :red      [:div.red word]
    :assassin [:div.assassin word]
    :neutral  [:div.neutral word]))

(defn cell [game view x y]
  (fn []
    (let [g                                 @game
          v                                 @view
          {:keys [word identity revealed?]} (q/get-cell g x y)
          winner                            (:winning-team g)
          w                                 [:span.word.revealed [colorize word identity]]]
      (if (or (= v :spymaster) winner (true? revealed?))
        w
        [:button.word
         {:on-click #(swap! game m/move! word)}
         word]))))

(defn grid [game view]
  [:table
   (for [y (range 5)]
     [:tr
      (for [x (range 5)]
        [:td
         [cell game view x y]])])])

(defn main-panel [game view]
  (fn []
    (let [g      @game
          v      @view
          turn   (:current-team g)
          winner (:winning-team g)]
      [:div
       [game-status-bar game]
       [turn-status-bar game view]
       [:center
        [:p
         [grid game view]
         [gameplay-bar game view]]]])))

(defn home-page []
  [:div [:h2 "Welcome to Codenames"]
   [:div [:a {:href "/game"} "Play a game!"]]])

(def game (atom (g/prepare-game)))
(def view (atom :player))

(defn game-page []
  (fn []
    [:div [title-bar]
     [main-panel game view]]))

;; -------------------------
;; Routes

(def page (atom #'game-page))

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

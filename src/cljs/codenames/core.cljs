(ns codenames.core
  (:require [accountant.core :as accountant]
            [clojure.string :as str]
            [codenames.game :as g]
            [codenames.moves :as m]
            [reagent.core :as reagent :refer [atom]]
            [secretary.core :as secretary :include-macros true]))

;; -------------------------
;; Views

(defn status-bar [game]
  (let [g      @game
        turn   (m/get-current-team g)
        winner (m/get-winner g)]
    [:div
     (if winner
       [:div (->> winner name str/capitalize) " is the winner."]
       [:div "It's " (name turn) "'s turn."])]))

(defn view-toggle [game]
  (fn []
    (let [g @game]
      [:button {:on-click #(swap! game m/switch-view!)}
       "Toggle view."])))

(defn change-turn-button [game]
  (fn []
    (let [g @game
          turn   (m/get-current-team g)]
      [:button {:on-click #(swap! game m/next-turn!)}
       "End " (name turn) "'s turn."])))

(defn reset-button [game]
  (fn []
    (let [g @game]
      [:button {:on-click #(reset! game (g/prepare-game))}
       "Start a new game!"])))

(defn remaining-display [game]
  (fn []
    (let [g    @game
          red  (-> g :remaining :red)
          blue (-> g :remaining :blue)]
      [:div.remaining
       [:span.red red] " - " [:span.blue blue]])))

(defn colorize [word identity]
  (case identity
    :blue     [:div.blue word]
    :red      [:div.red word]
    :assassin [:div.assassin word]
    :neutral  [:div.neutral word]))

(defn cell [game x y]
  (fn []
    (let [g                                 @game
          v                                 (:view g)
          {:keys [word identity revealed?]} (m/get-cell g x y)
          winner                            (m/get-winner g)
          w [:span.word [colorize word identity]]]
      (if (= v :player)
        (if winner
          w
          (if (true? revealed?)
            w
            [:button.unrevealed {:on-click #(swap! game m/move! word)}
             word]))
        [:span.word
         w]))))

(defn grid [game]
  [:table
   (for [y (range 5)]
     [:tr
      (for [x (range 5)]
        [:td.cell
         [cell game x y]])])])

(defn main-panel [game]
  (fn []
    (let [g      @game
          turn   (m/get-current-team g)
          winner (m/get-winner g)]
      [:div
       [status-bar game]
       (if winner [:div [reset-button game]]
           [:div
            [view-toggle game]
            [remaining-display game]
            [change-turn-button game]
            [reset-button game]])
       [:center
        [:p
         [grid game]]]])))

(defn home-page []
  [:div [:h2 "Welcome to Codenames"]
   [:div [:a {:href "/game"} "Play a game!"]]])

(defn game-page []
  (fn []
    (let [game (atom (g/prepare-game))]
      [:div [:h2 "Codenames"]
       [main-panel game]])))

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

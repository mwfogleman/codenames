(ns codenames.core
  (:require [codenames.game :as g]
            [codenames.moves :as m]
            [reagent.core :as reagent :refer [atom]]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]))

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
  (fn []
    (let [g                                 @game
          {:keys [word identity revealed?]} (m/get-cell g x y)
          winner                            (m/get-winner g)]
      (if winner
        [:span
         [colorize word identity]]
        (if (true? revealed?)
          [:span {:style {:width 30
                          :height 30}}
           [colorize word identity]]
          [:button {:on-click #(->> (m/move! g word)
                                    (reset! game))
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
  (fn []
    (let [g      @game
          turn   (m/get-current-team g)
          winner (m/get-winner g)]
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
        g]])))

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

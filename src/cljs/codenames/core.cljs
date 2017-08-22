(ns codenames.core
  (:require [reagent.core :as reagent :refer [atom]]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]))

(def initial-game {:starting-team :red,
                   :blue-remaining 8,
                   :current-team :red,
                   :words
                   '({:word "IVORY", :identity :red, :revealed? false, :position [3 1]}
                     {:word "QUEEN", :identity :red, :revealed? false, :position [3 0]}
                     {:word "HEART", :identity :red, :revealed? false, :position [0 2]}
                     {:word "KETCHUP", :identity :red, :revealed? false, :position [2 0]}
                     {:word "SPOT", :identity :red, :revealed? false, :position [0 4]}
                     {:word "COMIC", :identity :red, :revealed? false, :position [4 3]}
                     {:word "FIGURE", :identity :red, :revealed? false, :position [4 0]}
                     {:word "MARCH", :identity :red, :revealed? false, :position [2 3]}
                     {:word "THUMB", :identity :red, :revealed? false, :position [3 3]}
                     {:word "PIANO", :identity :blue, :revealed? false, :position [0 0]}
                     {:word "STATE", :identity :blue, :revealed? false, :position [1 0]}
                     {:word "SPELL", :identity :blue, :revealed? false, :position [2 2]}
                     {:word "BUFFALO", :identity :blue, :revealed? false, :position [1 1]}
                     {:word "TOKYO", :identity :blue, :revealed? false, :position [4 2]}
                     {:word "DRAGON", :identity :blue, :revealed? false, :position [3 4]}
                     {:word "MOSCOW", :identity :blue, :revealed? false, :position [0 1]}
                     {:word "PENGUIN", :identity :blue, :revealed? false, :position [0 3]}
                     {:word "SATELLITE", :identity :neutral, :revealed? false, :position [1 2]}
                     {:word "DEATH", :identity :neutral, :revealed? false, :position [1 3]}
                     {:word "CHICK", :identity :neutral, :revealed? false, :position [4 1]}
                     {:word "RABBIT", :identity :neutral, :revealed? false, :position [4 4]}
                     {:word "COPPER", :identity :neutral, :revealed? false, :position [2 1]}
                     {:word "POOL", :identity :neutral, :revealed? false, :position [2 4]}
                     {:word "ORGAN", :identity :neutral, :revealed? false, :position [1 4]}
                     {:word "LEPRECHAUN", :identity :assassin, :revealed? false, :position [3 2]}),
                   :red-remaining 9,
                   :round 0,
                   :id "G__54236",
                   :winning-team nil})

(def game (-> initial-game atom))

(defn reset-game! [] (reset! game initial-game))

;; -------------------------
;; Views

(defn colorize [word identity]
  (let [m @game]
    (case identity
      :blue     [:div {:style {:color "blue"}}
                 word]
      :red      [:div {:style {:color "red"}}
                 word]
      :assassin [:div {:style {:color "grey"}}
                 word]
      :neutral  [:div {:style {:color "yellow"}}
                 word])))

(defn cell [x y]
  (let [m @game
        {:keys [word identity revealed?]} (get-cell x y)
        winner                            (get-winner)]
    (fn []
      (if winner
        [:span
         [colorize word identity]]
        (if (true? revealed?)
          [:span {:style {:width 30
                          :height 30}}
           [colorize word identity]]
          [:button {:on-click #(move! word)
                    :style {:width 100
                            :height 100}}
           [:div {:style {:color "black"}}
            word]])))))

(defn grid []
  (let [m @game]
    [:table
     (for [y (range 5)]
       [:tr
        (for [x (range 5)]
          [:td {:style {:width      100
                        :height     100
                        :text-align :center}}
           [cell x y]])])]))

(defn main-panel []
  (let [m @game
        turn   (get-current-team)
        winner (get-winner)]
    (fn []
      [:div
       (if winner
         [:div
          (clojure.string/capitalize (name winner)) " is the winner."]
         [:div
          "It's " (name turn) "'s turn."])
       [:center
        [:p
         [grid]]]])))

(defn test-button []
  [:div
   [:input.btn {:type "button" :value "Next Round!"
                :on-click #(next-round!)}]])

(defn inspector []
  (let [m @game]
    [:div
     (for [[k v] m]
       [:div (str (->> k name clojure.string/capitalize) ": " v)])]))

(defn home-page []
  [:div [:h2 "Codenames"]
   ;; [main-panel]
   ;; [inspector]
   [:div [:a {:href "/about"} "go to about page"]]])

(defn about-page []
  [:div [:h2 "About codenames"]
   [:div [:a {:href "/"} "go to the home page"]]])

;; -------------------------
;; Routes

(def page (atom #'home-page))

(defn current-page []
  [:div [@page]])

(secretary/defroute "/" []
  (reset! page #'home-page))

(secretary/defroute "/about" []
  (reset! page #'about-page))

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

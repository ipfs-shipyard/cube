(ns ui.pages.home
  (:require [cljs.pprint :refer [pprint]]
            [ui.components.button :as button]
            [reagent.core :refer [atom]]
            ))

(def show-insides? (atom false))

(defn toggle-insides []
  (if @show-insides?
    (reset! show-insides? false)
    (reset! show-insides? true)))

(defn render []
  [:div
   [:span.f2.aqua "Welcome to Cube"]
   [:p "Reliable, easy to use, easily managed pinning service"]
   [:p "All the fun is happening under the 'Pins' and 'Instances' tabs"]
   (button/button {:text (if @show-insides?
                           "Ew, don't show the inside anymore!!!"
                           "Wanna see my insides?")
                   :onClick toggle-insides})
   (when @show-insides?
     [:div
      [:div.mt3.b "Current Application State"]
      [:pre (with-out-str (pprint @re-frame.db/app-db))]])])

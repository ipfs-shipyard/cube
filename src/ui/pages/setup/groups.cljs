(ns ui.pages.setup.groups
  (:require [re-frame.core :as rf]
            [ui.components.button :as button]))

(defn render []
  [:div.page.groups.ph3
   [:h1.f1 "Groups"]
   [:div.f4
    [:small.orange "(Notice: in the prototype we dont have groups yet)"]
    [:p "Create all the groups you want"]
    [:p "Groups can have different permissions so you can give access to exactly what you want"]]
   [:div
    [:input {:type "text"
             :placeholder "Enter group name"}]
    [:input {:type "button"
             :value "Add"}]]
   [:div
    [:h3 "Current groups"]
    [:ul
     (for [group @(rf/subscribe [:groups])]
       [:li group])]]
   [#(button/button {:subpage "users"
                     :text "Next"})]])

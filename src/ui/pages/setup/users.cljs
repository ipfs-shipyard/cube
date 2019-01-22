(ns ui.pages.setup.users
  (:require [re-frame.core :refer [dispatch]]
            [ui.components.button :as button]))

(defn render []
  [:div.page.users.ph3
   [:h1.f1 "Users"]
   [:small.orange "(Notice: in the prototype we dont have users yet)"]
   [:div.f4
    [:p "Create some users"]
    [:p "Users belong to Groups and therefore gets the same permission as the group itself"]]
   (button/button {:subpage "done"
                     :onClick #(dispatch [:set-setup-completed true])
                     :text "Finish Setup"})])

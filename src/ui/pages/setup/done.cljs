(ns ui.pages.setup.done
  (:require [re-frame.core :as re-frame]))

(defn render []
  [:div
   [:p "Finally done! Now you can enjoy everything with Cube"]
   [:p "First up, lets create some instances for you to host your pins on.
       Go ahead and click on the 'Instances' link above"] ])

(ns ui.pages.setup
  (:require [reagent.core :refer [atom]]
            [clojure.pprint :refer [pprint]]
            [re-frame.core :refer [dispatch]]))

(def password (atom ""))

(def password-description "Setup Password as provided from the Cube Application
                          Window or from the terminal output.")

(defn handle-keyup [ev]
  (when (= (.-keyCode ev) 13)
                        (let [url (str "/setup/" @password "/welcome")]
                          (dispatch [:go-to-page url])
                          (dispatch [:set-setup-password @password]))))

(defn handle-onchange [ev]
  (reset! password (-> ev
                       (.-target)
                       (.-value))))

(defn render [params]
  [:div "We'd love to help you setup Cube. For this, you need to have a Setup
        Password."
   [:p "You can find this password with the window from Cube or if you
        started Cube from a terminal, it's in the output after you started
        it."]
   [:p "Either paste in the password below and press enter, or click on
       the 'Open Setup Page' button in the Cube Application Window"]
   [:div
    [:label.f6.b.db.m2 {:for "password"} "Setup Password"]
    [:input.input-reset.ba.b--black-20.pa2.mb2.db.w-50 {:onKeyUp handle-keyup
                                                        :onChange handle-onchange
                                                        :type "password"
                                                        :id "password"}]
     [:small.f6.lh-copy.black-60.db.mb2 password-description]]])

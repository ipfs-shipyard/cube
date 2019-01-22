(ns ui.pages.setup.welcome
  (:require [ui.components.button :as button]
            [re-frame.core :refer [subscribe dispatch]]))

(defn wrong-password []
  [:div
   [:h3.red "Incorrect Setup Password"]
   [:p "It seems the password you provided, was not correct. Please make sure 
        you're using the right one."]])

(defn welcome []
  [:div.f4
   [:p "Cube is a application that helps you manage data stored on a IPFS cluster"]
   [:p "Cube will help you manage your pins in a more human way and allow you to give other people access to pin/upload data to your cluster"]
   [:p "This setup wizard will ask a series of questions to do the first-time setup of Cube"]
   (button/button {:subpage "hosting"
                   :text "Get Started"})])

(defn loading []
  [:div "Loading..."])

(defn render []
  (let [correct? @(subscribe [:setup-password-correct?])
        checked? @(subscribe [:setup-password-checked?])]
    [:div.page.welcome.ph3
     [:h1.f1 "Welcome to Cube"]
     [:div (if (not checked?)
             [loading]
             (if correct?
               [welcome]
               [wrong-password]))]]))

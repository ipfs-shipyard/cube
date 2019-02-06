(ns ui.pages.login
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [ui.login :as login]
            [ui.components.text-input :refer [text-input]]
            [ui.components.button :refer [button]]
            ))

(defn get-value [ev]
  (-> ev .-target .-value))

(def username (r/atom ""))
(def password (r/atom ""))

(defn do-login [username password]
  (dispatch [:do-login username password]))

(defn render []
  [:div.tc
   [:h3.f3 "Login"]
   [:div.mv3 "You need to login before you can use Cube"]
   [:form {:on-submit #(do (.preventDefault %)
                           (do-login @username @password))}
    [:div.mv3
     (text-input {:id "username"
                  :label "Username"
                  :class "w-20"
                  :style {:margin "0px auto"}
                  :type "text"
                  :value @username
                  :onChange #(reset! username (get-value %))})]
    [:div.mv3
     (text-input {:id "password"
                  :label "Password"
                  :class "w-20"
                  :style {:margin "0px auto"}
                  :type "password"
                  :value @password
                  :onChange #(reset! password (get-value %))})]
    (text-input {:id "login"
                 :style {:margin "0px auto"}
                 :class "w-10 bg-aqua white b"
                 :type "submit"
                 :value "Login"
                 :onChange #(do-login @username @password)})
    (let [error-msg @(subscribe [:login/error])]
      [:div.red (when error-msg error-msg)])]])

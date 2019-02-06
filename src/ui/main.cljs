(ns ^:figwheel-always ui.main
  ;; core > external > local
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [subscribe dispatch-sync dispatch]]
            [cljs.pprint :refer [pprint]]
            [ui.state :as state]
            [ui.router :as router]
            [ui.http :as http]
            [ui.login :as login]
            [ui.navigation :as navigation]
            [ui.websocket :as websocket]
            [ui.devtools :as devtools]))

(devtools/init!)

;; app doing routing
(defn app []
  "The default application view"
  [:div
   [navigation/navbar]
   [:div.ma3.gray-box.pa3
    (router/matching-page @(subscribe [:active-page]))]
   ;; Debug mode, shows the state on every page
   ;; [:pre (with-out-str (cljs.pprint/pprint @re-frame.db/app-db.))]
   ])

;; aaaaaand a render function! Does what it says on the tin
(defn render! []
  (reagent/render [app]
                  (js/document.getElementById "app")))

;; This function is called once dom is ready in `resources/public/index.html`
(defn ^:export run
  []
  ;; All of these dispatches are syncronous as we want to make sure the
  ;; handlers of the events gets fully completed before we move on to the
  ;; next one and finally render for the first time
  ;; set the initial db
  (dispatch-sync [:initialize])
  ;; navigate to the page we're at according to url
  (dispatch-sync [:go-to-page (-> js/window .-location .-pathname)])
  ;; check if we're logged in
  (dispatch-sync [:check-logged-in])
  ;; setup event listeners for updating state when page changes
  (router/dispatch-new-pages! js/window)
  ;; And render!
  (render!))

;; This gets called by figwheel when we change a file
(defn on-js-reload []
  (println "Rendering again")
  (render!))

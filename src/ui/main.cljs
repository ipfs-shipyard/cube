(ns ^:figwheel-always ui.main
  ;; core > external > local
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [subscribe dispatch-sync dispatch]]
            [cljs.pprint :refer [pprint]]
            [ui.state :as state]
            [ui.router :as router]
            [ui.setup :as setup]
            [ui.http :as http]
            [ui.navigation :as navigation]
            [ui.devtools :as devtools]))

(devtools/init!)

;; Eventually move into it's own file
;; TODO if connection dies, need to retry
(defn create-ws [url]
  (js/WebSocket. url))

(defn handle-ws-msg [msg]
  (dispatch [:set-remote-db (js->clj (.parse js/JSON (.-data msg)) :keywordize-keys true)]))

(defn setup-ws! [url]
  (let [ws (create-ws url)]
    (set! (.-onmessage ws) handle-ws-msg)))

(defn get-ws-url []
    (str "ws://" (-> (.-location js/window) .-host) "/api/db/ws"))

;; app doing routing
(defn app []
  "The default application view"
  [:div
   [navigation/navbar]
   [:div.ph4
    (router/matching-page @(subscribe [:active-page]))]])

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
  ;; check if setup has been finished already
  (dispatch-sync [:check-setup-completed])
  ;; setup event listeners for updating state when page changes
  (router/dispatch-new-pages! js/window)
  ;; start listen for ws messages
  (setup-ws! (get-ws-url))
  ;; And render!
  (render!))

;; This gets called by figwheel when we change a file
(defn on-js-reload []
  (println "Rendering again")
  (render!))

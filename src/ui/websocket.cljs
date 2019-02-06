(ns ui.websocket
  (:require [re-frame.core :refer [reg-sub dispatch reg-event-db reg-fx reg-event-fx subscribe]]
            [day8.re-frame.tracing :refer-macros [fn-traced]]))

(defn get-ws-url []
    (str "ws://" (-> (.-location js/window) .-host) "/api/db/ws"))

(defn create-ws [url]
  (js/WebSocket. url))

(defn close-ws [socket]
  (.close socket))

(defn handle-ws-msg [msg]
  (dispatch [:set-remote-db (js->clj (.parse js/JSON (.-data msg)) :keywordize-keys true)]))

(reg-event-db
  :set-websocket
  (fn-traced [db [_ websocket]]
             (assoc db :websocket websocket)))

(reg-sub
  :websocket
  (fn-traced [db _]
             (:websocket db)))

(reg-fx
  :websocket-disconnect
  (fn-traced [v]
             ((:on-connect v) (close-ws (:socket v)))))

(reg-fx
  :websocket-connect
  (fn-traced [v]
             ((:on-connect v) (create-ws (:url v)))))

(reg-event-fx
  :ws-disconnect
  (fn-traced [_ _]
             {:websocket-disconnect {:socket @(subscribe [:websocket])
                                     :on-connect #(do (dispatch [:set-websocket nil])
                                                      (dispatch [:set-remote-db {}]))}}))
(reg-event-fx
  :ws-connect
  (fn-traced [_ _]
             {:websocket-connect {:url (get-ws-url)
                                  :on-connect #(do (dispatch [:set-websocket %])
                                                   (set! (.-onmessage %) handle-ws-msg))}}))

(ns ui.login
  (:require [re-frame.core :refer [dispatch reg-fx reg-event-fx reg-event-db subscribe reg-sub]]
            [reagent.cookies :as cookies]
            [day8.re-frame.tracing :refer-macros [fn-traced]]))

(def cookie-name "login-token")

(reg-sub
  :login/error
  (fn-traced [db _]
             (-> db
                 :login
                 :error)))

(reg-sub
  :login/token
  (fn-traced [db _]
             (-> db
                 :login
                 :token)))

(reg-sub
  :login/profile
  (fn-traced [db _]
             (-> db
                 :login
                 :profile)))

(reg-sub
  :login/permissions
  (fn-traced [db _]
             (-> db
                 :login
                 :profile
                 :permissions)))

(reg-event-db
  :set-login-error
  (fn-traced [db [_ msg]]
             (assoc-in db [:login :error] msg)))

(reg-event-db
  :set-profile
  (fn-traced [db [_ profile]]
             (assoc-in db [:login :profile] profile)))

(reg-fx
  :get-cookie
  (fn-traced [v]
             (let [f (:callback v)]
               (f (cookies/get (:name v))))))

(reg-fx
  :delete-cookie
  (fn-traced [v] (cookies/remove! (:name v))))


(reg-event-fx
  :logout
  (fn-traced [cofx _]
             {:db (dissoc (:db cofx) :login)
              :delete-cookie {:name cookie-name}
              :dispatch-n [ ;; disconnect ws when logout happen
                           ;; make sure it's connected again on login
                           [:ws-disconnect]
                           [:go-to-page "/login"]]}))

(reg-fx
  :set-cookie
  (fn-traced [v] (cookies/set! (:name v) (:value v))))

(reg-event-fx
  :set-login-token
  (fn-traced [cofx [_ token]]
             (do (cljs.pprint/pprint token)
                 {:db (assoc-in (:db cofx) [:login :token] token)
                  :set-cookie {:name cookie-name
                               :value token}})))

(reg-event-fx
  :check-logged-in
  (fn-traced [_ _]
             {:get-cookie {:name cookie-name
                           :callback #(if (nil? %)
                                        (dispatch [:go-to-page "/login"])
                                        (do (dispatch [:set-login-token %])
                                            (dispatch [:get-profile-data])
                                            (dispatch [:ws-connect]))
                                        )}}))
(defn take-token-from-res [res]
  (.then (.json res) #(do (println "received res")
                          (dispatch [:set-login-token (get (js->clj %) "token")])
                          (dispatch [:get-profile-data])
                          (dispatch [:go-to-page "/home"])
                          (dispatch [:ws-connect]))))

(defn res->set-profile [res]
  (.then (.json res) #(do (println "my profile:")
                          (dispatch [:set-profile (js->clj % :keywordize-keys true)]))))

(reg-event-fx
  :get-profile-data
  (fn-traced [cofx _]
             {:http {:method "GET"
                     :url "/api/profile"
                     :headers {:Content-Type "application/json"
                               :Authorization (str "Token " @(subscribe [:login/token]))}
                     :on-success #(res->set-profile %)}}))

(reg-event-fx
  :do-login
  (fn-traced [cofx [_ username password]]
             {:http {:method "POST"
                     :url "/api/login"
                     :body {:username username
                            :password password}
                     :headers {:Content-Type "application/json"}
                     :on-success #(if (= 401 (.-status %))
                                    (dispatch [:set-login-error "Wrong username/password"])
                                    (take-token-from-res %))}
              :dispatch [:set-login-error nil]
              :dispatch-later [{:ms 2000 :dispatch [:set-login-error nil]}]}))

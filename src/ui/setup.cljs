(ns ui.setup
  (:require [clojure.string :as string]
            [bidi.bidi :as bidi]
            [re-frame.core :refer [dispatch reg-fx reg-event-fx subscribe]]
            [ui.router :as router]
            ;; TODO ruins dead code elimation
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            ))

(defn get-pw-from-route
  [active-page]
  (->
    (bidi/match-route router/url-map active-page)
    (:route-params)
    (:password)))

;; :check-setup-completed is the "entrypoint" event that kicks off bunch of
;; more events depending on how it goes. Here is a brief outline of what
;; happens.
;; Graph starts at Loading and finishes at any of the other boxes
;;       +-------+
;;       |Loading|
;;       +-------+
;; 
;;           |
;;           |
;;           v
;; 
;; :check-setup-completed
;; 
;;           +
;;           |
;;           |
;;           v
;; 
;; :set-setup-completed +---------> set state
;; 
;;           +
;;           |
;;           v
;;                              :completed? true +---------------+
;; :redirect-to-setup-if-needed +--------------> |Setup Completed|
;;                                               +---------------+
;; +                     +
;; | If not on a         |
;; | setup page          |
;; v                     v
;; 
;; +---------------+  :set-setup-password +----> set state
;; |Show Setup Help|
;; +---------------+     +
;;                       |
;;                       v
;; 
;;                  :check-setup-password
;; 
;;                            +
;;                            |
;;                            v
;; 
;;                         :http-status
;; 
;;                          +         +
;;                          |         |
;;                          v         v
;; 
;; :set-setup-password-correct?      :set-setup-password-checked +-> set state
;; 
;; +                        +
;; | true                   | false
;; v                        v
;; 
;; +----------+          +--------------+
;; |Show Setup|          |Wrong Password|
;; +----------+          +--------------+

(reg-event-fx
  :check-setup-completed
  (fn-traced [_ _]
             {:http-status {:method "GET"
                     :url "/api/setup/completed"
                     :on-success #(dispatch
                                   (condp = %
                                    204 [:set-setup-completed false]
                                    200 [:set-setup-completed true]))}}))

(reg-event-fx
  :set-setup-completed
  (fn-traced [cofx [_ completed?]]
             {:db (assoc-in (:db cofx) [:setup :completed?] completed?)
              ;; TODO don't subscribe! Pass in active-page
              :redirect-to-setup-if-needed {:active-page @(subscribe [:active-page])
                                            :completed? completed?}}))

(reg-fx
  :redirect-to-setup-if-needed
  (fn-traced [{completed? :completed?
               active-page :active-page}]
             (when (not completed?)
               (do
                 (let [password (get-pw-from-route active-page)]
                   (when password
                     (dispatch [:set-setup-password password])))
                 (when (not (string/includes? active-page "setup"))
                   (dispatch [:go-to-page "/setup"]))))))

(reg-event-fx
  :set-setup-password
  (fn-traced [cofx [_ password]]
             {:db (assoc-in (:db cofx) [:setup :password] password)
              :dispatch [:check-setup-password password]}))

(reg-event-fx
  :check-setup-password
  (fn-traced [_ [_ password]]
             {:http-status {:method "GET"
                     :url (str "/api/setup/" password)
                     :on-success #(do
                                    (condp = %
                                      401 (dispatch [:set-setup-password-correct? false])
                                      200 (dispatch [:set-setup-password-correct? true]))
                                    (dispatch [:set-setup-password-checked? true]))}}))

(reg-event-fx
  :save-setup-form
  (fn-traced [_ _]
             (let [pw @(subscribe [:setup-password])]
               {:http {:method "POST"
                       :url (str "/api/setup/" pw "/completed")
                       :body @(subscribe [:setup-form])
                       :headers {:Content-Type "application/json"}
                       :on-success #(do (dispatch [:go-to-page (str "/setup/" pw "/done")])
                                        (dispatch [:set-setup-completed true]))}})))

(ns ui.state (:require
            [re-frame.core :refer [reg-event-db reg-sub]]
            ;; TODO ruins dead code elimation
            [day8.re-frame.tracing :refer-macros [fn-traced]]
               ))

;; contains:
;; - map of the initial state
;; - spec for validating the state after every change
;; - helper for validating state

(def initial-state
  {:active-page "/"
   :remote-db {}
   :setup {
           :completed? false
           :password ""
           :password-correct? false
           :password-checked? false
           :form {
                  :chosen-hosting nil
                  :api-token nil
                  :admin-user {:username ""
                               :password ""}
                  :groups ["Administrators"]
                  :users []}}})

;; Events for changing values in the db, basically "setters"
(reg-event-db
  :initialize
  (fn-traced [_ _] initial-state))

(reg-event-db
  :set-remote-db
  (fn-traced [db [_ remote-db]]
             (assoc db :remote-db remote-db)))

(reg-event-db
  :set-active-page
  (fn-traced [db [_ url]]
             (assoc db :active-page url)))

(reg-event-db
  :set-setup-api-token
  (fn-traced [db [_ token]]
             (assoc-in db [:setup :form :api-token] token)))

(reg-event-db
  :set-setup-password-correct?
  (fn-traced [db [_ correct?]]
             (assoc-in db [:setup :password-correct?] correct?)))

(reg-event-db
  :set-setup-password-checked?
  (fn-traced [db [_ checked?]]
             (assoc-in db [:setup :password-checked?] checked?)))

(reg-event-db
  :set-setup-host
  (fn-traced [db [_ host]]
             (assoc-in db [:setup :form :chosen-host] host)))

;; Setup cursor subscription
;; views use these to make it clear they want these as updated values when
;; they change
;; should be setup together with `state` as they only have to change if the
;; `state` shape changes
(reg-sub
  :active-page
  (fn-traced [db _]
             (:active-page db)))

(reg-sub
  :setup
  (fn-traced [db _]
             (:setup db)))

(reg-sub
  :setup-form
  :<- [:setup]
  (fn-traced [setup]
             (:form setup)))

(reg-sub
  :setup-password
  :<- [:setup]
  (fn-traced [setup]
             (:password setup)))

(reg-sub
  :setup-completed?
  :<- [:setup]
  (fn-traced [setup]
             (:completed? setup)))

(reg-sub
  :setup-password-correct?
  :<- [:setup]
  (fn-traced [setup]
             (:password-correct? setup)))

(reg-sub
  :setup-password-checked?
  :<- [:setup]
  (fn-traced [setup]
             (:password-checked? setup)))

(reg-sub
  :groups
  (fn-traced [db _]
             (-> db
                 :setup-form
                 :groups)))

(reg-sub
  :instances/running
  (fn-traced [db _]
             (-> db
                 :remote-db
                 :instances
                 :running)))

(reg-sub
  :instances/wanted
  (fn-traced [db _]
             (-> db
                 :remote-db
                 :instances
                 :wanted)))

(reg-sub
  :pins
  (fn-traced [db _]
             (-> db
                 :remote-db
                 :pins)))

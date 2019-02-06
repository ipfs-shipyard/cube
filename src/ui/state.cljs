(ns ui.state (:require
            [re-frame.core :refer [reg-event-db reg-sub]]
            ;; TODO ruins dead code elimation
            [day8.re-frame.tracing :refer-macros [fn-traced]]))

;; contains:
;; - map of the initial state
;; - spec for validating the state after every change
;; - helper for validating state

(def initial-state
  {:active-page "/"
   :remote-db {}})

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

(reg-sub
  :active-page
  (fn-traced [db _]
             (:active-page db)))

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

;; returns the first element that matches predicate
(defn find-first
         [f coll]
         (first (filter f coll)))

(defn cid-match? [pin cid]
    (= (:cid pin) cid))

(reg-sub
  :pin
  (fn-traced [db [_ cid]]
             (find-first #(cid-match? % cid) (-> db
                                                 :remote-db
                                                 :pins))))

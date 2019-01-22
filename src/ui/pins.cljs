(ns ui.pins
  (:require [re-frame.core :refer [dispatch reg-event-fx]]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            ))

(defn add-pin [pin-hash pin-name]
  (dispatch [:add-pin pin-hash pin-name]))

(defn get-url
  ([pin-hash pin-name] (str "/api/pins/" pin-hash "/" pin-name))
  ([pin-hash] (str "/api/pins/" pin-hash)))

(reg-event-fx
  :add-pin
  (fn-traced [cofx [_ pin-hash pin-name]]
             {:http-status {
                            :method "POST"
                            :url (get-url pin-hash pin-name)
                            :on-success #(println "pinning")}}))

(reg-event-fx
  :delete-pin
  (fn-traced [cofx [_ pin-hash]]
             {:http-status {
                            :method "DELETE"
                            :url (get-url pin-hash)
                            :on-success #(println "deleting")}}))

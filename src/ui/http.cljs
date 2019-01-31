(ns ui.http
  (:require [re-frame.core :refer [reg-fx dispatch]]
             ;; TODO ruins dead code elimination
            [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
             ))

(defn to-json [obj]
  (.stringify js/JSON (clj->js obj)))

(defn http [url method body headers func]
  "Make a HTTP call with url and method, func is called once res has been received"
  (let [opts {:method method
              :headers (clj->js headers)}
        merged-opts (if (nil? body) opts (merge opts {:body (to-json body)}))]
    (-> (js/fetch url (clj->js merged-opts))
        (.catch #(func %))
        (.then #(func %)))))

(defn http-status [url method func]
  "Make a HTTP call with url and method, func is called once res has been received"
  (.then (js/fetch url (clj->js {:method method})) #(func (.-status %))))

(reg-fx
  :http
  (fn-traced [v]
             (http
               (:url v)
               (:method v)
               (:body v)
               (:headers v)
               (:on-success v))))

(reg-fx
  :http-status
  (fn-traced [v]
             (http-status (:url v)
                          (:method v)
                          #((:on-success v) %))))

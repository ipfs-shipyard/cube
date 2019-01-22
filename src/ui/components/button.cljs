(ns ui.components.button
  (:require [re-frame.core :refer [dispatch]]))

(defn go-to-subpage [page]
  (dispatch [:go-to-subpage page]))

(defn add-subpage [page]
  (dispatch [:add-subpage page]))

(defn button [params]
  [:a.f6.link.dim.ba.ph3.pv2.dib.navy {:disabled (:disabled params)
                                       :href (:subpage params)
                                       :onClick #(do (when (:add-subpage params)
                                                       (.preventDefault %)
                                                       (add-subpage (:add-subpage params)))
                                                   (when (:subpage params)
                                                       (.preventDefault %)
                                                       (go-to-subpage (:subpage params)))
                                                   (when (:onClick params)
                                                       ((:onClick params) %)))}
   (:text params)])

(ns ui.pages.pin
  (:require [re-frame.core :refer [subscribe dispatch]]))

(defn peer-map [p]
  [:div.mt3
   [:div.f4
    [:span.b "PeerID: "]
    [:span (:peer-id p)]
    ]
   [:div.f5.mt1
    [:span.b "Status: "]
    [:span (:status p)]
    ]
   (when (:error p)
     [:div.mt1
      [:div.white.bg-red.w-70.pa1
       [:span.b "Error "]
       [:span (:error p)]
       ]])
   [:div.f6.mt1 (str "Last Update: " (:timestamp p))]])

(defn render [params]
  (let [pin @(subscribe [:pin (:cid params)])]
    [:div
     [:a.aqua {:href "#"
               :onClick #(do (.preventDefault %)
                             (dispatch [:go-to-page "/pins"]))} "Back to all pins"]
     [:div.f3.mt2 (str "Details about pin \"" (:name pin) "\"")]
     [:div.f6.mt1
      [:span.b.monospace "CID: "]
      [:span (:cid pin)]]
     [:div.f6.mt1
      [:span.b.monospace "Pinners: "]
      [:span (-> pin :peer-map count)]]
     [:div.f3.mt4.b "Peers"]
     (for [p (:peer-map pin)]
       (peer-map p))]))

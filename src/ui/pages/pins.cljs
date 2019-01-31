(ns ui.pages.pins
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [ui.components.button :as button]
            [ui.components.text-input :as text-input]
            [ui.pins :as pins]
            [clojure.contrib.humanize :refer [filesize]]
            ))

(defn no-pins-message []
  [:p "Seems you have no pins currently"])

(def current-name (r/atom (str "copy of ipld.io from " (.toISOString (js/Date.)))))
(def current-hash (r/atom "QmXb2bKQdgNhC7vaiKQgXFtt7daUZD382L54UTTNXnwQTD"))
(def pin-disabled? (r/atom false))

(defn add-pin-name []
  (text-input/text-input {:id "name"
                          :label "Name"
                          :description "Descriptive name of this Pin"
                          :value @current-name
                          :onChange #(reset! current-name (-> % .-target .-value))
                          }))

(defn add-pin-hash []
  (text-input/text-input {:id "hash"
                          :label "Multihash"
                          :description "The Multihash of this pin"
                          :value @current-hash
                          :onChange #(reset! current-hash (-> % .-target .-value))
                          }))

(defn add-pin []
  [:div
   [:div
    (add-pin-name)]
   [:div.mt3
    (add-pin-hash)]
   [:div.mt1
    (button/button {:text "Pin!"
                    :disabled @pin-disabled?
                    :onClick #(do (pins/add-pin @current-hash @current-name)
                                  (reset! current-name "")
                                  (reset! current-hash "")
                                  (reset! pin-disabled? true)
                                  (.setTimeout
                                    js/window
                                    (fn [] (reset! pin-disabled? false))
                                    500)
                                  )})]])

(defn group-by-status-and-count [info status]
  (count (get (group-by :status (:peer-map info)) status)))

(defn count-pinned [info]
  (group-by-status-and-count info "pinned"))

(defn count-pinning [info]
  (group-by-status-and-count info "pinning"))

(defn count-errors [info]
  (+ (group-by-status-and-count info "pin_error")
     (group-by-status-and-count info "unpin_error")))

(defn get-ipfs-io-link [pin]
  (str "https://ipfs.io/ipfs/" (:cid pin)))

(defn details-link [cid]
  [:a.f5.aqua {:href "#"
               :onClick #(do (.preventDefault %)
                             (rf/dispatch [:add-subpage cid]))} "Details"])

(defn pin-table [pins]
  [:div
   [:div.overflow-auto
    [:table.center
     [:thead
      [:tr.stripe-dark
       [:th.fw6.tl.pa3.bg-white "Hash"]
       [:th.fw6.tl.pa3.bg-white "Name"]
       [:th.fw6.tl.pa3.bg-white "Size"]
       [:th.fw6.tl.pa3.bg-white "Pinning"]
       [:th.fw6.tl.pa3.bg-white "Pinned"]
       [:th.fw6.tl.pa3.bg-white "Error"]
       [:th.fw6.tl.pa3.bg-white ""]
       [:th.fw6.tl.pa3.bg-white ""]
       [:th.fw6.tl.pa3.bg-white ""]
       [:th.fw6.tl.pa3.bg-white ""]
       ]]
     [:tbody.1h-copy
      (for [pin pins]
        [:tr.stripe-dark {:key (:cid pin)}
         [:td.pa3 (:cid pin)]
         [:td.pa3 (:name pin)]
         [:td.pa3 (filesize (:size pin))]
         [:td.pa3 (count-pinning pin)]
         [:td.pa3 (count-pinned pin)]
         (let [err (count-errors pin)]
           [:td.pa3 {:class (when (> err 0) "red")} err])
         [:td.pa3 (details-link (:cid pin))]
         [:td.pa3 [:a.f5.aqua "View in webui"]]
         [:td.pa3 [:a.f5.aqua {:href (get-ipfs-io-link pin)} "View on ipfs.io"]]
         [:td.pa3 [:a.f5.aqua {:href "#"
                               :onClick #(rf/dispatch [:delete-pin (:cid pin)])
                               } "Delete"]]])]]]])

(defn single-pin [info]
  [:div (str (:cid info) " - Pinned on: #" (count-pinned info))])

(defn render []
  [:div
   [:div.w-100.fl
    [:div.w-30.fl.pa3
     [:h3.f3 "Your Pins"]
     [:p "Here you can manage your pins, like editing the
         meta-data, remove some of them to clear space or simply browse
         your pinned content"]]
    [:div.w-30.fl.pa3
       [:div.w-100
        [:h3.f3 "Add new pin"]
        (add-pin)]]
    [:div.w-30.fl.pa3
     [:h3.f3 "Pin manually with ipfs-cluster-ctl"]
     [:p
      [:span "Wanna pin your content manually? You can use "]
      [:code [:a {:href "https://cluster.ipfs.io/download/"} "ipfs-cluster-ctl"]]
      [:span " directly if you want:"]]
     [:pre.f6 "ipfs-cluster-ctl --host /ip4/172.17.0.5/tcp/9094 id"]
     [:p "Try the command above to connect to your running cluster"]]]
   [:div.w-100.fl
    [:div.pa4]
    (let [pins @(rf/subscribe [:pins])]
      (if (= 0 (count pins))
        (no-pins-message)
        (pin-table pins)))]
   [:div.cf]])

(ns ui.pages.instances
  (:require [re-frame.core :refer [reg-event-fx dispatch subscribe]]
            [reagent.core :as r]
            [cljs.pprint :refer [pprint]]
            [ui.components.text-input :refer [text-input]]
            [ui.components.button :refer [button]]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            ))

(reg-event-fx
  :set-wanted-instances
  (fn-traced [cofx [_ n]]
             {:http {:method "POST"
                     :url (str "/api/instances/wanted/" n)
                     :body ""
                     :headers {}
                     :on-success (fn [])}}))

(defn submit-wanted-instances [n]
  (do 
    (println (str "Setting " n " as # of wanted instances"))
    (dispatch [:set-wanted-instances n])))

(defn instance-row [id instance onClick selected-id]
  [:div.monospace.dim {:key id
                       :onClick onClick
                       :class [(when (= id selected-id) "blue")]}
   (str "OK - " id " - " (:type instance))])

(defn stats [wanted running]
  [:div.monospace (str "Wanted: " wanted " Running: " running)])

(defn label [title text]
  [:div
   [:label.f6.b.db.m2 title]
   [:small.f6.lh-copy.black-60.db.mb2 text]])

(defn selected-view [id instance]
  [:div
   (label "go-ipfs container" (:go-ipfs instance))
   (label "ipfs-cluster container" (:ipfs-cluster instance))
   (label "instance type" (:type instance))
   (label "ipfs-cluster api multiaddr" (:cluster-api instance))])

(def value (r/atom 0))
(def selected (r/atom nil))

(defn render []
  (let [wanted @(subscribe [:instances/wanted])
        running (count @(subscribe [:instances/running]))]
    [:div
     [:div.w-100.fl
      [:div.w-30.fl.pa3
       [:h3.f3 "Your Instances"]
       [:p "Here you can manage your instances, like creating new or delete
           the existing ones."]]
      [:div.w-30.fl.pa3
       [:h3.f3 "How many instances to run"]
       [:div.mt3 (stats wanted running)]
       [:div.mt3
        (text-input {:id "wanted"
                     :label "Wanted Instances"
                     :description "This number decides how many instances Cube will try to run"
                     :type "number"
                     :value (if (= 0 @value) running @value)
                     :onEnter #(submit-wanted-instances @value)
                     :onChange #(reset! value (-> % .-target .-value))})]
       (button {:text "Set"
                :onClick #(submit-wanted-instances @value)})]
      [:div.w-30.fl.pa3
       (if @selected
         [:div
          [:h3.f3 (str "Details about " @selected)]
          [:div.mt3 (selected-view @selected (@selected @(subscribe [:instances/running])))]]
         [:div
          [:h3.f3 "Details"]
          [:p "Details about a instance will show here when you select one"]])]
      [:div.w-100.fl
       [:div.pa4]
       [:div.mt3
        [:div.f3 "Running Instances"]
        [:div.mt1 (for [[id i] @(subscribe [:instances/running])]
                    (instance-row id i #(reset! selected id) @selected))]]]]]))

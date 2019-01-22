(ns ui.pages.setup.hosting
  (:require [reagent.core :refer [atom]]
            [re-frame.core :refer [dispatch]]
            [ui.components.button :as button]
            [ui.components.text-input :as text-input]))

(defonce chosen-host (atom nil))
(defonce host-options (atom {:docker-path "unix:///var/run/docker.sock"}))

(defn go-to-next-page []
  ;; TODO currently only happens here as we don't have more steps right now
  (dispatch [:save-setup-form]))

;; TODO should be shared and specced
(def available-hosting [{:name "Solo"
                         :description "Just runs on this local machine"
                         :enabled false}
                        {:name "Docker"
                         :description "Run go-ipfs and ipfs-cluster via docker containers"
                         :enabled true}
                        {:name "AWS"
                         :description "Use Amazon WebServices"
                         :enabled false}
                        {:name "DigitalOcean"
                         :description "Use DigitalOcean"
                         :enabled false}
                        {:name "Custom"
                         :description "Manually start new Cube instances"
                         :enabled false}])

(defn do-credentials []
  (text-input/text-input {:id "token"
                          :label "API Token"
                          :description "API Token generated from DigitalOcean"
                          :onEnter go-to-next-page
                          :onChange #(swap! host-options assoc :token (-> % .-target .-value))
                         }))

(defn docker-credentials []
  (text-input/text-input {:id "path"
                          :label "Docker daemon socket path"
                          :description "The path Cube should be using to connect to the Docker daemon"
                          :value (:docker-path @host-options)
                          :onEnter go-to-next-page
                          :onChange #(swap! host-options assoc :docker-path (-> % .-target .-value))
                         }))

(defn host-credentials [chosen-host]
  (condp = chosen-host
    "DigitalOcean" (do-credentials)
    "Docker" (docker-credentials)))

(defn host-option [host selected-host]
  (let [attrs {:disabled (not (:enabled host))
               :text (:name host) }]
    (button/button (merge attrs (if selected-host
                                  {:subpage (:name host)}
                                  {:add-subpage (:name host)})))))

(defn render [params]
  [:div.page.hosting.ph3
   [:h1.f1 "Hosting Provider"]
   [:div.f4
    [:p "You'll need to select how you want to run Cube."]]
   [:div (for [host available-hosting]
           (host-option host (:host params)))]
   [:div.mt3 (when (:host params) (host-credentials (:host params)))]
   [:div.mt3 (when (and (:host params)
                        (> (count @host-options) 0)) (button/button {:disabled false
                                                                     :onClick go-to-next-page
                                                                     :text "Complete Setup"}))]])

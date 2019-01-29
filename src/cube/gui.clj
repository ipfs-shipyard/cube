(ns cube.gui
  (:gen-class)
  (:require [clojure.pprint :refer [pprint]]
            [clojure.java.browse :refer [browse-url]]
            [com.stuartsierra.component :as c])
  (:use seesaw.core))

(def cube-status-str {:starting "Starting..."
                      :running "Running!"
                      :error "Error!"})

(def setup-password (atom ""))
(def http-port (atom 0))

(defn get-status [k] (str "Status: " (k cube-status-str)))
(defn get-url [] (str "URL: " (str "http://localhost:" @http-port)))
(defn get-password [] @setup-password)

(def status-label (label :text (get-status :starting)))
(def port-label (label :text "URL: N/A"))
(def password-label (label :text "Setup Password:"))
(def password-output (let [out (text :text (get-password)
                           :editable? false
                           :columns 1)]
                         (.setBackground out nil)
                         (.setBorder out nil)
                         out))

(defn cube-url [port]
  (str "http://localhost:" port))

(defn setup-url [port password]
  (str (cube-url port) "/setup/" password "/welcome"))

(defn open-dashboard [port]
  (browse-url (cube-url port)))

(defn open-setup [port password]
  (browse-url (setup-url port password)))

(def shutdown-button (action
                       :handler (fn [e] (System/exit 0))
                       :enabled? false
                       :name "Shutdown and exit"))

(def open-setup-page-button (action
                       :handler (fn [e]
                                  (future (open-setup @http-port @setup-password)))
                       :enabled? false
                       :name "Open Setup Page"))

(def open-dashboard-button (action
                       :handler (fn [e]
                                  (future (open-dashboard @http-port)))
                      :enabled? false
                      :name "Open Cube Dashboard"))

(def panel (vertical-panel
                          :items [
                                  (label :text "Cube Control Panel" :font "ARIAL-BOLD-21")
                                  status-label
                                  port-label
                                  "---"
                                  password-label
                                  password-output
                                  "---"
                                  open-dashboard-button
                                  open-setup-page-button
                                  shutdown-button]))

(defn server-started! [] (do
                            (config! shutdown-button :enabled? true)
                            (config! open-setup-page-button :enabled? true)
                            (config! open-dashboard-button :enabled? true)
                            (config! status-label :text (get-status :running))
                            (config! port-label :text (get-url))
                            (config! password-output :text (get-password))))

(defn set-port! [port]
  (reset! http-port port))

(defn set-password! [password]
  (reset! setup-password password))

(defn start-gui []
  (let [myframe (frame :title "Cube",
                       :content panel
                       :width 350
                       :height 220
                       :resizable? false
                       :on-close :exit)]
    (invoke-later
      (show! myframe))))

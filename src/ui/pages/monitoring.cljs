(ns ui.pages.monitoring
  (:require [re-frame.core :refer [subscribe dispatch]]
            [ui.monitoring :as monitoring]
            [clojure.contrib.humanize :refer [filesize]]))

(defn unix->jsdate [unix-timestamp]
  (js/Date. (* unix-timestamp 1000)))

(def colors {:ok "bg-aqua"
             :warn "bg-yellow"
             :danger "bg-red"})

(defn diskspace-colors [current-val]
  (cond
    (= current-val 0) (:danger colors)
    ;; Less than 10GB
    (< current-val 10000000000) (:warn colors)
    ;; More than 10GB
    (> current-val 10000000000) (:ok colors)
    :else "bg-gray"))

(defn render []
  (let [pinsize @(subscribe [:monitoring/pinsize])
        freespace @(subscribe [:monitoring/freespace])]
    [:div
     [:div.ma1.pa1 (str "Last Update: " (-> pinsize :timestamp unix->jsdate))]
     [:div.tc.w-20.bg-aqua.white.pa2.ma1.fl
      [:div.f3.b "Spaced used by Pins"]
      [:div.monospace.mt2.f4 (-> pinsize :value filesize)]]
     [:div.tc.w-20.white.pa2.ma1.fl {:class (diskspace-colors (-> freespace :value))}
      [:div.f3.b "Free Diskspace"]
      [:div.monospace.mt2.f4 (-> freespace :value filesize)]]
     [:div.tc.w-20.bg-aqua.white.pa2.ma1.fl
      [:div.f3.b "CPU"]
      [:div.monospace.mt2.f4 "25%"]]
     [:div.tc.w-20.bg-red.white.pa2.ma1.fl
      [:div.f3.b "Memory"]
      [:div.monospace.mt2.f4 "64%"]]
     [:div.tc.w-20.bg-aqua.white.pa2.ma1.fl
      [:div.f3.b "Network"]
      [:div.monospace.mt2.f5 "24Mbps / 12Mbps (▲/▼)"]]
     [:div.cf]
     [:div.mt3 "Notice: Currently this only actually updates the 'Spaced used by
               Pins' and 'Free Diskspace' values. Other values are mock values
               and not real."]]))

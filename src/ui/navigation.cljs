(ns ui.navigation
  (:require [re-frame.core :refer [subscribe]]
            [ui.router :as router]))

(def navbar-items {"/home" "Home"
                   "/upload" "Upload"
                   "/pins" "Pins"
                   "/instances" "Instances"
                   "/users" "Users"
                   "/groups" "Groups"
                   "/preferences" "Preferences"})

(def disabled-items ["/upload" "/users" "/groups" "/preferences"])

(defn href-attr [url enabled?]
  {:href (if enabled? url "")
   :key url
   :onClick #(let [ev %]
               (.preventDefault ev)
               (when enabled?
                 (router/go-to-page url)))})

(defn includes? [coll item]
  (boolean (some #{item} coll)))

(defn create-navbar-item [url title matched? enabled?]
  [:a.link.white.f5.dib.mr3
   (merge {:class [(when matched? "underline")]
           :disabled (not enabled?)}
          (href-attr url enabled?))
   title])

(defn navbar []
  (let [setup-completed? @(subscribe [:setup-completed?])]
    [:nav.pa3.pa4-ns.bg-navy {:class (when (not setup-completed?) "o-10 disabled")}
     [:a.link.dim.white.b.f3.dib.mr3.aqua (href-attr "/home" setup-completed?) "Cube"]
     (let [active-page @(subscribe [:active-page])]
       (for [item navbar-items]
         (let [[url title] item]
           (let [matched? (= active-page url)
                 is-enabled? (not (includes? disabled-items url))]
             (create-navbar-item url title matched? is-enabled?)))))]))


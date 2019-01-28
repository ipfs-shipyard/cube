(ns ui.navigation
  (:require [re-frame.core :refer [subscribe]]
            [ui.router :as router]))

(def navbar-items [{:url "/home" :title "Home" :active true}
                   {:url "/upload" :title "Upload" :active false}
                   {:url "/pins" :title "Pins" :active true}
                   {:url "/instances" :title "Instances" :active true}
                   {:url "/users" :title "Users" :active false}
                   {:url "/groups" :title "Groups" :active false}
                   {:url "/monitoring" :title "Monitoring" :active true}
                   {:url "/preferences" :title "Preferences" :active false}
                   {:url "/logout" :title "Logout" :active false}])

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
         (let [url (:url item)
               title (:title item)
               active (:active item)
               matched? (= active-page url)]
           (create-navbar-item url title matched? active))))]))


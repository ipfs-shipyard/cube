(ns ui.navigation
  (:require [re-frame.core :refer [subscribe dispatch]]
            [ui.router :as router]))

;; list of navigation links, :name represents the permission the user needs to
;; have access to to see the link as active
(def navbar-items [{:url "/home" :title "Home" :name "pins"}
                   {:url "/upload" :title "Upload" :name "upload"}
                   {:url "/pins" :title "Pins" :name "pins"}
                   {:url "/instances" :title "Instances" :name "instances"}
                   {:url "/users" :title "Users" :name "users"}
                   {:url "/groups" :title "Groups" :name "groups"}
                   {:url "/monitoring" :title "Monitoring" :name "monitoring"}
                   {:url "/preferences" :title "Preferences" :name "preferences"}])

(defn take-first-elements [items]
  (distinct (reduce (fn [acc [curr1 curr2]] (conj acc curr1)) [] items)))

(defn is-string [item str]
  (= item str))

(defn has-permission [permissions perm]
  (boolean (some #(is-string % perm) (take-first-elements permissions))))

(defn prevent-default-then [ev then]
  (do (.preventDefault ev)
      (then)))

(defn href-attr [url enabled?]
  {:href (if enabled? url "")
   :key url
   :onClick #(prevent-default-then % (fn [] (when enabled?
                                              (router/go-to-page url))))})

(defn create-navbar-item [url title matched? enabled?]
  [:a.link.white.f5.dib.mr3
   (merge {:class [(when matched? "underline")]
           :disabled (not enabled?)}
          (href-attr url enabled?))
   title])

(defn profile-control [profile]
  (if (nil? profile)
    [:div [:a.link.white.f5.dib.mr3 (href-attr "/login" true) "Login"]]
    [:div
       [:a.link.white.f5.dib.mr3 (href-attr "/profile" true)
        [:span.b "User: "]
        [:span (:username profile)]]
       [:a.link.white.f5.dib.mr3
        {:href "#"
         :onClick #(prevent-default-then % (fn []
                                             (dispatch [:logout])))} "Logout"]]))

(defn navbar []
  [:nav.bg-navy
   [:div.w-80.pa3.fl
    [:a.link.dim.white.b.f3.dib.mr3.aqua (href-attr "/home" true) "Cube"]
    (let [active-page @(subscribe [:active-page])
          permissions @(subscribe [:login/permissions])]
      (doall (for [item navbar-items]
               (let [url (:url item)
                     title (:title item)
                     perm-name (:name item)
                     matched? (= active-page url)
                     enabled? (has-permission permissions perm-name)]
                 (create-navbar-item url title matched? enabled?)))))]
   [:div.tr.w-20.fl.pa3 {:style {:height "27px" :line-height "27px"}}
    (profile-control @(subscribe [:login/profile]))]
   [:div.cf]])

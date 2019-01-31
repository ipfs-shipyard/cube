(ns ui.router
  (:require [clojure.string :as string]
            [cljs.pprint :refer [pprint]]

            [re-frame.core :refer [dispatch reg-event-fx subscribe]]
            [bidi.bidi :as bidi]

            ;; TODO find a way of dynamically load these instead
            [ui.pages.home :as home]
            [ui.pages.upload :as upload]
            [ui.pages.pins :as pins]
            [ui.pages.pin :as pin]
            [ui.pages.instances :as instances]
            [ui.pages.users :as users]
            [ui.pages.groups :as groups]
            [ui.pages.monitoring :as monitoring]
            [ui.pages.preferences :as preferences]
            [ui.pages.login :as login]
            ;; TODO ruins dead code elimation
            [day8.re-frame.tracing :refer-macros [fn-traced]]))

(defn go-to-page [url]
  (dispatch [:go-to-page url]))

(defn go-to-subpage [subpage]
  (dispatch [:go-to-subpage subpage]))

(defn redirect-subpage [subpage]
  (dispatch [:go-to-subpage subpage]))

(defn handle-popstate [ev]
  (when (not (nil? (.-state ev)))
    (dispatch [:set-active-page (-> ev .-state .-url)])))

(defn dispatch-new-pages! [ctx]
  "Listens to popstate events on 'ctx' and dispatches :set-active-page with the new url"
  (.addEventListener ctx "popstate" handle-popstate))

(def url-map ["/" {"" home/render
                 "home" home/render
                 "upload" upload/render
                 "pins" pins/render
                 ["pins/" :cid] pin/render
                 "instances" instances/render
                 "users" users/render
                 "groups" groups/render
                 "monitoring" monitoring/render
                 "preferences" preferences/render
                 "login" login/render}])

(defn not-found [urls active-page]
  [:div
   [:p (str "Could not find a page for `" active-page "` in list of urls")]
   [:pre (with-out-str (pprint urls))]])

(defn matching-page [active-page]
  (if-let [route (bidi/match-route url-map active-page)]
    ((:handler route) (:route-params route))
    (not-found url-map active-page)))

(reg-event-fx
  :go-to-page
  (fn-traced [cofx [_ url]]
             {:db (assoc (:db cofx) :active-page url)
              :dispatch [:push-state url]}))

(defn replace-last-url-part
  [full part]
  "Replaces last component of a full URL with part"
  (string/join
    "/"
    (let [splitted (string/split full "/")]
      (concat (butlast splitted) (list part)))))

;; Does the same as :go-to-page, but only changes the last part of the URL
;; Example: on page `/setup/pw/welcome` and dispatch `[:go-to-subpage "users"]`
;; will send user to page `/setup/pw/users`
(reg-event-fx
  :go-to-subpage
  (fn-traced [cofx [_ new-sub-page]]
             ;; TODO should not subscribe! Pass active page in event
             (let [active-page @(subscribe [:active-page])
                   new-url (replace-last-url-part active-page new-sub-page)]
               {:db (assoc (:db cofx) :active-page new-url)
                :dispatch [:push-state new-url]})))

(reg-event-fx
  :add-subpage
  (fn-traced [cofx [_ new-sub-page]]
             ;; TODO should not subscribe! Pass active page in event
             (let [active-page @(subscribe [:active-page])
                   new-url (str active-page "/" new-sub-page)]
               {:db (assoc (:db cofx) :active-page new-url)
                :dispatch [:push-state new-url]})))

(reg-event-fx
  :push-state
  (fn-traced [_ [_ url]]
             (.pushState window.history (clj->js {:url url}) "" url)))

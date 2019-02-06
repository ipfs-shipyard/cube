(ns cube.web
  (:require [com.stuartsierra.component :as c]
            [org.httpkit.server :as httpkit]
            [compojure.core :as compojure]
            [compojure.route :as route]
            [ring.util.response :refer [resource-response]]
            [compojure.coercions :as coercions]
            [clojure.data.json :as json]
            [buddy.auth.backends :as backends]
            [buddy.auth.middleware :refer (wrap-authentication authentication-request)]
            [buddy.sign.jwt :as jwt]
            [clojure.pprint :refer [pprint]]
            [cube.db :as db]
            [cube.auth :as auth]
            [cube.monitoring :as monitoring]
            [cube.instances :as instances]
            [cube.cluster :as cluster]
            [ring.middleware.reload :refer [wrap-reload]]
            [compojure.handler :as handler]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [hiccup.middleware :refer [wrap-base-url]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [crypto.password.bcrypt :as bcrypt]))

;; Move into it's own file
(defn read-perm [k]
  [k :read])

(defn write-perm [k]
  [k :write])

(def instances-read (read-perm :instances))
(def monitoring-read (read-perm :monitoring))
(def pins-read (read-perm :pins))
(def users-read (read-perm :users))
(def groups-read (read-perm :groups))
(def preferences-read (read-perm :preferences))
(def upload-read (read-perm :upload))

(def instances-write (write-perm :instances))
(def monitoring-write (write-perm :monitoring))
(def pins-write (write-perm :pins))
(def users-write (write-perm :users))
(def groups-write (write-perm :groups))
(def preferences-write (write-perm :preferences))
(def upload-write (write-perm :upload))

(def instances-readwrite [instances-read instances-write])
(def monitoring-readwrite [monitoring-read monitoring-write])
(def pins-readwrite [pins-read pins-write])
(def users-readwrite [users-read users-write])
(def groups-readwrite [groups-read groups-write])
(def preferences-readwrite [preferences-read preferences-write])
(def upload-readwrite [upload-read upload-write])

(defn flat-add-vector [acc curr]
  (if (vector? (get curr 0))
    (into acc curr)
    (into acc [curr])))

(defn join-perms [& perms]
  (vec (reduce flat-add-vector [] perms)))

(def permissions {:admin (join-perms instances-readwrite
                                     monitoring-readwrite
                                     pins-readwrite
                                     users-readwrite
                                     groups-readwrite
                                     preferences-readwrite
                                     upload-readwrite)

                  :manager (join-perms instances-read
                                       monitoring-read
                                       pins-readwrite
                                       users-readwrite
                                       preferences-read
                                       upload-readwrite)

                  :devops (join-perms instances-readwrite
                                      monitoring-readwrite
                                      pins-readwrite
                                      preferences-readwrite
                                      upload-readwrite)

                  :pinner (join-perms pins-readwrite
                                      monitoring-read
                                      upload-readwrite)

                  :viewer (join-perms pins-read upload-read)
                  :guest  []})

(def open-channels (atom []))

(defn send-update-over-channel! [ch to-send]
  (httpkit/send! ch (json/write-str to-send)))

(defn filtered-db-state [user permissions db]
  (auth/filter-db permissions user db))

(defn send-update-over-channels! [to-send]
  (doseq [ch @open-channels]
    (let [filtered-db (filtered-db-state (:user ch) permissions {:state (atom to-send)})]
      (send-update-over-channel! (:channel ch) @(:state filtered-db)))))

(defn create-user [username role]
  {:username username
   :password (bcrypt/encrypt username)
   :roles #{role}
   :permissions (role permissions)})

(def users {"admin" (create-user "admin" :admin)
            "manager" (create-user "manager" :manager)
            "devops" (create-user "devops" :devops)
            "pinner" (create-user "pinner" :pinner)
            "viewer" (create-user "viewer" :viewer)
            "guest" (create-user "guest" :guest)})

(def http-unauthorized {:status 401})

(def secret "mysecret")
(def backend (backends/jws {:secret secret}))

(defn ws-handler
  [db]
  (fn [request]
    (let [login-token (get-in request [:cookies "login-token" :value])]
      (if-not (nil? login-token)
        (let [username (:user (jwt/unsign login-token secret))
              user (get-in users [username])]
          (httpkit/with-channel request channel
            (httpkit/on-close channel (fn [status] (println "[web] Closed WS channel " status)))
            (when (httpkit/websocket? channel)
              (do (swap! open-channels conj {:channel channel :user user})
                  ;; listening for changes on the db, send connected users the
                  ;; new version
                  (db/on-change db send-update-over-channels!)
                  (let [filtered-db (filtered-db-state user permissions db)]
                    (send-update-over-channel! channel @(:state filtered-db)))))))
        http-unauthorized))))

(defn login-handler
  [request]
  (let [data (:body request)]
    (if (auth/authenticated? users data)
      (let [user (get-in users [(:username data)])
            token (jwt/sign {:user (:username user)} secret)]
        {:status 200
         :body {:token token}
         :headers {"Content-Type" "application/json"}})
      http-unauthorized)))

(defn auth-handler [success-handler]
  (wrap-authentication
    (fn [request]
      (if (nil? (:identity request))
        http-unauthorized
        (success-handler request)))
    backend))

(defn profile-handler [request] (auth-handler
                                  #(let [user (get-in users [(-> % :identity :user)])]
                                    {:status 200
                                     :body (dissoc user :password)})))

;; Wraps the routes in bunch of middleware
(defn wrap [routes]
  (-> routes
      (handler/site)
      (wrap-keyword-params)
      (wrap-params)
      (wrap-base-url)
      (wrap-json-response)
      (wrap-json-body {:keywords? true :bigdecimals? true})))

;; cluster service to use everywhere
(defn routes [db instances cluster]
  (wrap 
    (compojure/routes
      (route/resources "/")
      (compojure/GET "/" [] 
                     (resource-response "index.html" {:root "public"}))
      (compojure/context "/api" []
                         (compojure/POST "/login" req (login-handler req))
                         (compojure/GET "/profile" req (profile-handler req))
                         ;; TODO Creating and removing pins should be protected
                         ;; Protected and needs `:pins :write` permission
                         (compojure/POST "/pins/:cid/:cid-name" [cid cid-name]
                                         (cluster/pin cluster cid cid-name))
                         (compojure/DELETE "/pins/:cid" [cid]
                                         (cluster/remove-pin cluster cid))
                         (compojure/GET "/db/ws" [] (ws-handler db))
                         (compojure/GET "/instances/wanted" [] (str (instances/get-wanted instances)))
                         (compojure/POST "/instances/wanted/:instance-type/:instance-count"
                                         [instance-type
                                          instance-count :<< coercions/as-int]
                                         (do 
                                           (instances/set-wanted
                                             instances
                                             (keyword instance-type)
                                             instance-count)
                                           {:status 200}))
                         (compojure/GET "/monitoring" [] (json/write-str @cube.monitoring/state)))
      ;; TODO leading to issues in uberjar with `Stream Closed` and what-not
      ;; (route/not-found (resource-response "index.html" {:root "public"}))
      (compojure/GET "/*" []
                     (resource-response "index.html" {:root "public"}))
      (route/not-found {:status 404})
      )))

(defrecord Web [db instances cluster]
  c/Lifecycle
  (start [this]
    (println "[web] Starting")
    (assoc this :server (httpkit/run-server (routes db instances cluster) {:port (:port this)})))
  (stop [this]
    (print "[web] Stopping")
    ((:server this)) ;; stops httpkit server, run-server returns function to stop
    (assoc this :server nil)))

(defn new [port]
  (map->Web {:port port}))

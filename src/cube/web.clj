(ns cube.web
  (:require [com.stuartsierra.component :as c]
            [org.httpkit.server :as httpkit]
            [compojure.core :as compojure]
            [compojure.route :as route]
            [ring.util.response :refer [resource-response]]
            [compojure.coercions :as coercions]
            [clojure.data.json :as json]
            [clojure.pprint :refer [pprint]]
            [cube.db :as db]
            [cube.setup :as setup]
            [cube.instances :as instances]
            [cube.cluster :as cluster]
            [ring.middleware.reload :refer [wrap-reload]]
            [compojure.handler :as handler]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [hiccup.middleware :refer [wrap-base-url]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]))

(def open-channels (atom []))

(defn send-update-over-channel! [ch to-send]
  (httpkit/send! ch (json/write-str to-send)))

(defn send-update-over-channels! [to-send]
  (doseq [ch @open-channels]
    (send-update-over-channel! ch to-send)))

(defn ws-handler [db]
  (fn [request]
    (httpkit/with-channel request channel
      (httpkit/on-close channel (fn [status] (println "[web] Closed WS channel " status)))
      (when (httpkit/websocket? channel)
        (do 
          (println "[web] Opened WS channel")
          (swap! open-channels conj channel)
          (db/on-change db send-update-over-channels!)
          (send-update-over-channel! channel @(:state db))
          )))))

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
                         (compojure/POST "/pins/:cid/:cid-name" [cid cid-name]
                                         (cluster/pin cluster cid cid-name))
                         (compojure/DELETE "/pins/:cid" [cid]
                                         (cluster/remove-pin cluster cid))
                         (compojure/context "/setup" []
                                            (compojure/GET "/completed" []
                                                           (let [completed? (db/access-in
                                                             db
                                                             [:setup :completed?])]
                                                             (if completed?
                                                               {:status 200}
                                                               {:status 204})))
                                            (compojure/GET "/:pass" [pass]
                                                          (if (setup/check-setup-pass db pass)
                                                            {:status 200}
                                                            {:status 401}))
                                            (compojure/POST "/:pass/completed" [pass]
                                                          (if (setup/check-setup-pass db pass)
                                                            (do (setup/set-completed db)
                                                                {:status 200})
                                                            {:status 204})))
                         (compojure/GET "/db/ws" [] (ws-handler db))
                         (compojure/GET "/instances/wanted" [] (str (instances/get-wanted instances)))
                         (compojure/POST "/instances/wanted/:n" [n :<< coercions/as-int]
                                         (do 
                                           (instances/set-wanted instances n)
                                           {:status 200})))
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

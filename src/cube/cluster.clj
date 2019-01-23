(ns cube.cluster (:require [com.stuartsierra.component :as c]
                           [cube.scheduler :as scheduler]
                           [cube.instances :as instances]
                           [cube.db :as db]
                           [clojure.pprint :refer [pprint]]
                           [clj-http.client :as http]))

(defn format-peer-info [peer]
  (-> (if (empty? (:error peer)) (dissoc peer :error) peer)
      (dissoc :peername)
      (assoc :peer-id (:peer peer))
      (dissoc :peer)
      (assoc :status (keyword (:status peer)))
      (dissoc :cid)))

(defn format-res [res]
  (sort-by :cid (vec (map (fn [p] {:cid (:cid p)
                     :peer-map (vec (map format-peer-info (vals (:peer_map p))))}) res))))

(defn get-api-addr [instances]
  (:cluster-api (second (first (instances/get-running instances)))))

(defn get-name-for-pin [api-addr pin]
  (-> (http/get (format "%s/allocations/%s" api-addr (:cid pin)) {:as :json})
      :body
      :name))

(defn set-name-for-pin [api-addr pin]
  (if (nil? (:name pin))
    (assoc pin :name (get-name-for-pin api-addr pin))
    pin))

(defn set-names-for-pins [api-addr pins]
  (map #(set-name-for-pin api-addr %) pins))

(defn update-pins [db instances]
  (let [api-addr (:cluster-api (second (first (instances/get-running instances))))]
    (when api-addr
      (let [pins (format-res (:body (http/get (format "%s/pins" api-addr) {:as :json})))]
        (db/put db :pins (set-names-for-pins api-addr pins))))))

(defn add-new-pin [instances cid cid-name]
  (http/post
    (format "%s/pins/%s?name=%s" (get-api-addr instances) cid cid-name)
    {:as :json}))

(defrecord Cluster [db scheduler instances]
  c/Lifecycle
  (start [this]
    (println "[cluster] Starting...")
    (scheduler/add-task scheduler #(update-pins db instances))
    (-> this
      (assoc :db db)
      (assoc :instances instances)))
  (stop [this]
    (println "[cluster] Stopping...")
    this))

(defn new []
  (map->Cluster {}))

(defn pin [cluster cid cid-name]
  (add-new-pin (:instances cluster) cid cid-name))

(defn remove-pin [cluster cid]
  (http/delete
    (format "%s/pins/%s" (get-api-addr (:instances cluster)) cid)
    {:as :json}))

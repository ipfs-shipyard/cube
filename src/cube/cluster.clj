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

(defn get-proxy-addr [instances]
  (:ipfs-proxy (second (first (instances/get-running instances)))))

(defn get-name-for-pin [api-addr pin]
  (-> (http/get (format "%s/allocations/%s" api-addr (:cid pin)) {:as :json})
      :body
      :name))

(defn get-size-for-pin [proxy-addr pin]
  (-> (http/get (format "%s/api/v0/object/stat/%s" proxy-addr (:cid pin)) {:as :json})
      :body
      :CumulativeSize))

(defn set-name [api-addr pin]
  (if (nil? (:name pin))
    (assoc pin :name (get-name-for-pin api-addr pin))
    pin))

(defn set-size [proxy-addr pin]
  (if (nil? (:size pin))
    (assoc pin :size (get-size-for-pin proxy-addr pin))))

(defn set-for-pins [func addr pins]
  (map #(func addr %) pins))

(defn update-pins [db instances]
  (let [inst (second (first (instances/get-running instances)))
        api-addr (:cluster-api inst)
        proxy-addr (:ipfs-proxy inst)]
    (when api-addr
      (let [pins (format-res (:body (http/get (format "%s/pins" api-addr) {:as :json})))]
        (->> pins
            (set-for-pins set-name api-addr)
            (set-for-pins set-size proxy-addr)
            (db/put db :pins))))))

(defn add-new-pin [instances cid cid-name]
  (http/post
    (format "%s/pins/%s?name=%s" (get-api-addr instances) cid cid-name)
    {:as :json}))

(defn get-freespace-metrics [cluster]
  (:body (http/get
           (format "%s/monitor/metrics/freespace" (get-api-addr (:instances cluster)))
           {:as :json})))

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

(defn get-pins [cluster]
  (db/access (:db cluster) :pins))

(ns cube.providers.docker
  (:require [com.stuartsierra.component :as c]
            [clj-docker-client.core :as docker]
            [clojure.pprint :refer [pprint]]
            [clj-http.client :as http]
            [clojure.data.json :as json]
            [crypto.random :refer [url-part]]
            [cube.db :as db]))

;; Which images to use for the containers
(def images {:go-ipfs "ipfs/go-ipfs:v0.4.18"
             :ipfs-cluster "ipfs/ipfs-cluster:v0.8.0"})

;; Keyword for this particular provider
(def provider-type :docker)

;; Gets the IP from a container ID
(defn get-ip [conn id]
  (-> (docker/inspect conn id)
      :network-settings
      :ip-address))

(defn pull-images [conn]
  (doseq [[_ image] images]
    (docker/pull conn image)))

;; Good'ol http/get but with retries and hardcoded :9094/id suffix
(defn get-retry [max-attempts ip]
  (http/get (str "http://" ip ":9094/id")
              {:socket-timeout 1000
               :conn-timeout 1000
               :as :json
               :retry-handler (fn [ex try-count ctx]
                                (println (str "Ex" ex " Try: " try-count))
                                (Thread/sleep 500)
                                (< try-count max-attempts))}))

;; Gets the ipfs-cluster api endpoint from instance
(defn get-api-multiaddr [conn instance]
  (let [ip (get-ip conn instance)
        res (get-retry 10 ip)
        id (-> res :body :id)]
    (str "http://" ip ":9094")))

(defn get-webui-addr [conn instance]
  (let [ip (get-ip conn instance)
        res (get-retry 10 ip)
        id (-> res :body :id)]
    (str "http://" ip ":9095/webui")))

(defn get-ipfs-proxy [conn instance]
  (let [ip (get-ip conn instance)
        res (get-retry 10 ip)
        id (-> res :body :id)]
    (str "http://" ip ":9095")))

;; Creates a map to map ID => containers for go-ipfs + ipfs-cluster
(defn create-id [conn go-ipfs-id ipfs-cluster-id]
  [(keyword (url-part 8)) {:type provider-type
                           :metadata {:go-ipfs-id go-ipfs-id
                                      :ipfs-cluster-id ipfs-cluster-id}
                           :cluster-api (get-api-multiaddr conn ipfs-cluster-id)
                           :webui (get-webui-addr conn ipfs-cluster-id)
                           :ipfs-proxy (get-ipfs-proxy conn ipfs-cluster-id)}])

;; Saves containers of go-ipfs and ipfs-cluster into the global state
;; Returns the shared ID
(defn save-instance [conn db go-ipfs-id ipfs-cluster-id]
  (let [[k v] (create-id conn go-ipfs-id ipfs-cluster-id) ]
    (db/put-in db [:instances :running k] v)))

(defn get-start-cmd
  "Returns a bootstrap argument if existing cluster is running, otherwise empty
  string"
  [db conn]
  (let [instances (db/access-in db [:instances :running])]
    (if (> (count instances) 0)
      (let [ip (get-ip conn (-> instances first second :metadata :ipfs-cluster-id))
            res (get-retry 10 ip)
            id (-> res :body :id)]
        (str "daemon --bootstrap /ip4/" ip "/tcp/9096/ipfs/" id))
      "")))

(defn create-go-ipfs [conn]
  (docker/run conn (:go-ipfs images) "daemon" {} {} true))

(defn create-ipfs-cluster [conn start-cmd go-ipfs-ip secret]
  (docker/run conn 
              (:ipfs-cluster images)
              start-cmd
              {:IPFS_API (str "/ip4/" go-ipfs-ip "/tcp/5001")
               :CLUSTER_SECRET secret}
              {}
              true))

(defn create [conn db]
  (let [go-ipfs-id (create-go-ipfs conn)
        go-ipfs-ip (get-ip conn go-ipfs-id)
        start-cmd (get-start-cmd db conn)
        secret (db/access-in db [:instances :cluster-secret])
        ipfs-cluster-id (create-ipfs-cluster conn start-cmd go-ipfs-ip secret)]
    (save-instance conn db go-ipfs-id ipfs-cluster-id)))

(defn destroy [conn instance]
  (let [metadata (:metadata instance)
        go-ipfs-id (:go-ipfs-id metadata)
        ipfs-cluster-id (:ipfs-cluster-id metadata)]
    (doseq [id [ipfs-cluster-id go-ipfs-id]]
      (docker/kill conn id)
      (docker/rm conn id))))

(defn initialize [conn]
  (pull-images conn))

(defrecord ProviderDocker []
  c/Lifecycle
  (start [this]
    (let [connection (docker/connect)]
      (initialize connection)
      (assoc this :connection connection)))
  (stop [this]
    (docker/disconnect (:connection this))
    (assoc this :connection nil)))

(defn new [url]
  (map->ProviderDocker {:url url}))


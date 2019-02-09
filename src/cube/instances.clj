(ns cube.instances
  (:require [com.stuartsierra.component :as c]
            [cube.scheduler :as scheduler]
            [clojure.pprint :refer [pprint]]
            [cube.db :as db]
            [crypto.random :refer [hex]]
            [cube.providers.docker :as provider-docker]
            [cube.providers.do :as provider-do]))

(def active-providers [:docker :do])

(def create-map {:docker (fn [db conn] (provider-docker/create conn db))
                 :do (fn [db conn] (provider-do/create db))})

(comment
  (let [db (:db @cube.dev/running-system)]
    (provider-do/create db))
  )

(def destroy-map {:docker (fn [db conn id] (let [m (db/access-in db [:instances :running id])]
                                             (provider-docker/destroy conn m)
                                             (db/remove-in db [:instances :running id])))
                  :do (fn [db conn] (println "delete do instance"))})

(defn get-provider-wanted [wanted provider]
  (provider wanted))

(defn get-provider-running [running provider]
  (vec (filter #(let [[id i] %] (= (:type i) provider)) running)))

(defn check-provider [db docker-conn wanted running provider]
  (let [running (get-provider-running running provider)
        n-wanted (get-provider-wanted wanted provider)
        n-running (count running)
        create-func (provider create-map)
        destroy-func (provider destroy-map)]
    (cond
      (= n-wanted 0) (doseq [[id _] running]
                       (destroy-func db docker-conn id))
      (> n-wanted n-running) (doseq [_ (range (- n-wanted n-running))]
                               (create-func db docker-conn))
      (< n-wanted n-running) (let [to-destroy (- n-running n-wanted)]
                               (doseq [[id _] (take to-destroy running)]
                                 (destroy-func db docker-conn id)))
      (= n-wanted n-running) (println "Balanced"))))

(defn check-instances [db docker-conn]
  (let [wanted (db/access-in db [:instances :wanted])
        running (db/access-in db [:instances :running])]
    (doseq [provider active-providers]
      (check-provider db docker-conn wanted running provider))))

(defrecord Instances [db scheduler docker]
  c/Lifecycle
  (start [this]
    (println "[instances] Starting")
    (when (nil? (db/access db :instances))
      (db/put db :instances {:wanted {}
                             :running {}
                             :cluster-secret (hex 32)}))
    (scheduler/add-task scheduler #(check-instances db (:connection docker)))
    (assoc this :instances {:db db}))
  (stop [this]
    (print "[instances] Stopping")
    (assoc this :instances nil)))

(defn new []
  (map->Instances {}))

(defn set-wanted [instances instance-type instance-count]
  (db/put-in (:db instances) [:instances :wanted instance-type] instance-count))

(defn get-wanted [instances]
  (db/access-in (:db instances) [:instances :wanted]))

(defn get-running [instances]
  (db/access-in (:db instances) [:instances :running]))

(defn get-cluster-api-multiaddrs [instances]
  (db/access-in (:db instances) [:instances :running]))

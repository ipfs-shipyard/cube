(ns cube.instances
  (:require [com.stuartsierra.component :as c]
            [cube.scheduler :as scheduler]
            [clojure.pprint :refer [pprint]]
            [cube.db :as db]
            [crypto.random :refer [hex]]
            ;; TODO currently hardcoding provider to docker
            [cube.providers.docker :as provider-docker]))

(defn get-difference [a b]
  (- a b))

(defn create-instances [db conn amount]
  (doseq [_ (range amount)]
    (provider-docker/create conn db)))

(defn delete-instances [db conn amount]
  (doseq [_ (range amount)]
    (let [[id m] (first (db/access-in db [:instances :running]))]
      (provider-docker/destroy conn m)
      (db/remove-in db [:instances :running id])
      )))

(defn check-instances [db docker-conn]
  (let [wanted (db/access-in db [:instances :wanted])
        current (count (db/access-in db [:instances :running]))]

    (cond
      (= wanted 0) (doseq [[id m] (db/access-in db [:instances :running])]
                     (provider-docker/destroy docker-conn m)
                     (db/remove-in db [:instances :running id]))
      (> wanted current) (let [to-create (- wanted current)]
                           (println (format "Creating %s new instances" to-create))
                           (create-instances db docker-conn to-create))
      (< wanted current) (let [to-remove (- current wanted)]
                             (println (format "Removing %s instances" to-remove))
                             (delete-instances db docker-conn to-remove))
      (= wanted current) (comment "Balanced")))) ;; do nothing


(defrecord Instances [db scheduler docker]
  c/Lifecycle
  (start [this]
    (println "[instances] Starting")
    ;; Debug function to change state each tick
    ;; (scheduler/add-task scheduler #(db/put db :ticks (+ (db/access db :ticks) 1)))
    (when (nil? (db/access db :instances))
      (db/put db :instances {:wanted 0
                             :running {}
                             :cluster-secret (hex 32)}))
    (scheduler/add-task scheduler #(check-instances db (:connection docker)))
    (assoc this :instances {:db db}))
  (stop [this]
    (print "[instances] Stopping")
    (assoc this :instances nil)))

(defn new []
  (map->Instances {}))

(defn set-wanted [instances n]
  (db/put-in (:db instances) [:instances :wanted] n))

(defn get-wanted [instances]
  (db/access-in (:db instances) [:instances :wanted]))

(defn get-running [instances]
  (db/access-in (:db instances) [:instances :running]))

(defn get-cluster-api-multiaddrs [instances]
  (db/access-in (:db instances) [:instances :running]))

(ns cube.system
  (:require [com.stuartsierra.component :as c]
            [cube.scheduler :as scheduler]
            [cube.db :as db]
            [cube.web :as web]
            [cube.instances :as instances]
            [cube.providers.docker :as provider-docker]
            [cube.cluster :as cluster]
            [cube.monitoring :as monitoring]))

(defn create-system [config-options]
  (let [{http-port :http-port
         db-path   :db-path} config-options]
    (c/system-map :db (db/new db-path)
                  :scheduler (scheduler/new {:interval 1})
                  :web (c/using (web/new http-port) [:db :instances :cluster])
                  :instances (c/using (instances/new) [:db :scheduler :docker])
                  :docker (provider-docker/new "unix:///var/run/docker.sock")
                  :cluster (c/using (cluster/new) [:db :scheduler :instances])
                  :monitoring (c/using (monitoring/new) [:db :scheduler :instances :cluster])
                  )))

(ns cube.setup
  (:require [com.stuartsierra.component :as c]
            [clojure.pprint :refer [pprint]]
            [cube.db :as db]
            [crypto.random :as crypto]))

(defn create-password []
  (crypto/url-part 32))

(defrecord Setup [db]
  c/Lifecycle
  (start [this]
    (println "[setup] Starting")
    (when (nil? (db/access db :setup))
      (db/put db :setup {:password (create-password)
                         :completed? false}))
    this)
  (stop [this] this))

(defn new []
  (map->Setup {}))

(defn get-password [db]
  (db/access-in db [:setup :password]))

(defn completed? [db]
  (db/access-in db [:setup :completed?]))

(defn set-completed [db]
  (db/put-in db [:setup :completed?] true))

(defn check-setup-pass
  ([db guess]
   (check-setup-pass db guess true))
  ([db guess body]
   (if (= guess (get-password db))
     body
     false)))

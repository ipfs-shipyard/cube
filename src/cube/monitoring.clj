(ns cube.monitoring
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [com.stuartsierra.component :as c]
            [cube.cluster :as cluster]
            [cube.instances :as instances]
            [cube.db :as db]
            [cube.scheduler :as scheduler]))

(s/def ::timestamp string?)

(s/def ::value int?)

(s/def ::entry (s/keys :req-un [::timestamp
                                ::value]))

(s/def ::history (s/coll-of ::entry))

(s/def ::current ::entry)

(s/def ::pinsize (s/keys :req-un [::current
                                  ::history]))

(s/def ::freespace (s/keys :req-un [::current
                                    ::history]))

(s/def ::state (s/keys :req-un [::pinsize
                                ::freespace]))

(def state (atom {:freespace {:current nil :history []}
                  :pinsize {:current nil :history []}}))


(s/fdef get-current-time
        :ret int?)

(defn get-current-time [] (quot (System/currentTimeMillis) 1000))

(s/fdef add-to-state
        :args (s/cat :entry ::entry)
        :ret nil?)

(defn add-to-state! [entry t]
  (do
    (swap! state assoc-in [t :current] entry)
    (swap! state update-in [t :history] conj entry)))

(s/fdef calculate-total-pin-size
        :args (s/cat :pins :cube/pins)
        :ret (s/and int? pos?))

(defn calculate-total-pin-size [pins]
  (reduce (fn [acc curr] (+ acc (:size curr))) 0 pins))

(s/def :freespace/name string?)
(s/def :freespace/peer string?)
(s/def :freespace/value string?)
(s/def ::freespace-from-cluster (s/keys :req-un [:freespace/name
                                                 :freespace/peer
                                                 :freespace/value]))

(s/fdef calculate-total-freespace
        :args (s/cat :freespaces ::freespace-from-cluster)
        :ret (s/and int?))

(defn calculate-total-freespace [freespaces]
  (reduce (fn [acc curr] (+ acc (Long/parseLong (:value curr)))) 0 freespaces))

(s/fdef create-entry
        :args (s/cat :value ::value)
        :ret ::entry)

(defn create-entry [size]
  {:value size
   :timestamp (get-current-time)})

(defn check-pin-total-size [cluster]
  (-> cluster
      (cluster/get-pins)
      (calculate-total-pin-size)
      (create-entry)
      (add-to-state! :pinsize)))

(defn check-cluster-freespace [cluster]
  (-> cluster
      (cluster/get-freespace-metrics)
      (calculate-total-freespace)
      (create-entry)
      (add-to-state! :freespace)))

(defn check-metrics [instances cluster]
  (when (> (count (instances/get-running instances)) 0)
    (do (check-pin-total-size cluster)
        (check-cluster-freespace cluster))))

(defn set-db-current-value [db]
  (db/put-in db [:monitoring :pinsize] (-> @state :pinsize :current))
  (db/put-in db [:monitoring :freespace] (-> @state :freespace :current)))

(defrecord Monitoring [db scheduler instances cluster]
  c/Lifecycle
  (start [this]
    (println "[monitoring] Starting")
    (scheduler/add-task scheduler #(check-metrics instances cluster))
    (scheduler/add-task scheduler #(set-db-current-value db))
    (-> (create-entry 0) (add-to-state! :freespace))
    (-> (create-entry 0) (add-to-state! :pinsize))
    this)
  (stop [this]
    (println "[monitoring] Stopping")
    this))

(defn new []
  (map->Monitoring {}))

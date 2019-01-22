(ns cube.scheduler
  (:require [com.stuartsierra.component :as c]
            [tea-time.core :as tt]
            [clojure.pprint :refer [pprint]]))

(defn add-task [scheduler f]
  (do (println "[scheduler] Adding a task to the scheduler")
      (swap! (:tasks scheduler) conj f)))

(defn do-interval [tasks]
  (doseq [f @tasks]
    (f)))

(defrecord Scheduler [interval]
  c/Lifecycle
  (start [this]
    (println "[scheduler] Starting")
    (tt/start!)
    (let [tasks (atom [])
          tt-tasks (atom [(tt/every! interval (bound-fn []
                                                (try
                                                  (do-interval tasks)
                                                  (catch Exception ex
                                                    ;; TODO
                                                    ;; Error in scheduler,
                                                    ;; should log it somewhere
                                                    (pprint ex)))

                                                ))])]
      (-> this
          ;; External tasks
          (assoc :tasks tasks)
          ;; Internal, interval tea-time tasks
          (assoc :tt-tasks tt-tasks))))
  (stop [this]
    (println "[scheduler] Stopping")
    (tt/stop!)
    (doseq [task @(:tt-tasks this)]
      (do (println "[scheduler] Cancelling a task")
        (tt/cancel! task)))
    (assoc this :tt-tasks nil)))

(defn new [options]
  ;; :interval => seconds
  (map->Scheduler {:interval (:interval options)}))

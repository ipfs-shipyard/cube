(ns ^:skip-aot cube.dev
  (:require
            [clojure.tools.namespace.repl :refer [set-refresh-dirs refresh]]
            [figwheel-sidecar.repl-api :as fw]
            [cube.system :as system]
            [com.stuartsierra.component :as c]))

(def running-system (atom nil))

(defn start-system! [params]
  (reset! running-system (c/start (system/system params))))

(defn stop-system! []
  (c/stop @running-system)
  (reset! running-system nil))

(defn start
  ([] (start false))
  ([figwheel?] (do (when figwheel? (fw/start-figwheel!))
                  (start-system! {:http-port 3000}))))

(defn stop
  ([] (stop false))
  ([figwheel?] (do (when figwheel? (fw/stop-figwheel!))
                  (stop-system!))))

(set-refresh-dirs "src/cube")

(defn reset []
  (stop-system!)
  (refresh :after 'cube.dev/start))

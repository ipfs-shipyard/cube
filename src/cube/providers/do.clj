(ns cube.providers.do
  (:require [digitalocean.v2.core :as do]
            [clojure.spec.alpha :as s]
            [clojure.pprint :refer [pprint]]
            [cube.providers.provider :as provider]))

;; This is not used at all currently, as the provider is not ready

;; TODO bunch of data straight from the DO API, should be verified with spec
(def sizes [{:vcpus 1,
             :disk 20,
             :slug "512mb",
             :price_monthly 5.0,
             :transfer 1.0,
             :price_hourly 0.007439999841153622,
             :regions ["ams2"
                       "ams3"
                       "blr1"
                       "fra1"
                       "lon1"
                       "nyc1"
                       "nyc2"
                       "nyc3"
                       "sfo1"
                       "sfo2"
                       "sgp1"
                       "tor1"],
             :memory 512,
             :available true}
            {:vcpus 1,
             :disk 25,
             :slug "s-1vcpu-1gb",
             :price_monthly 5.0,
             :transfer 1.0,
             :price_hourly 0.007439999841153622,
             :regions ["ams2"
                       "ams3"
                       "blr1"
                       "fra1"
                       "lon1"
                       "nyc1"
                       "nyc2"
                       "nyc3"
                       "sfo1"
                       "sfo2"
                       "sgp1"
                       "tor1"],
             :memory 1024,
             :available true}
            {:vcpus 1,
             :disk 30,
             :slug "1gb",
             :price_monthly 10.0,
             :transfer 2.0,
             :price_hourly 0.01487999968230724,
             :regions ["ams2"
                       "ams3"
                       "blr1"
                       "fra1"
                       "lon1"
                       "nyc1"
                       "nyc2"
                       "nyc3"
                       "sfo1"
                       "sfo2"
                       "sgp1"
                       "tor1"],
             :memory 1024,
             :available true}
            {:vcpus 1,
             :disk 50,
             :slug "s-1vcpu-2gb",
             :price_monthly 10.0,
             :transfer 2.0,
             :price_hourly 0.01487999968230724,
             :regions ["ams2"
                       "ams3"
                       "blr1"
                       "fra1"
                       "lon1"
                       "nyc1"
                       "nyc2"
                       "nyc3"
                       "sfo1"
                       "sfo2"
                       "sgp1"
                       "tor1"],
             :memory 2048,
             :available true}])

(s/def ::not-empty-string (s/and string? (complement empty?)))
(s/def ::tag-name ::not-empty-string)
(s/def ::tag-name ::not-empty-string)

(def default-tag-name "cube")
(def default-image-id "ubuntu-18-10-x64")
(def default-size "s-2vcpu-2gb")
;; (def default-size "s-2vcpu-2gb") ;; for running ipfs + ipfs-cluster
(def default-region "region")

(defn filter-by-tag [droplets tag]
  (vec (filter #(.contains (:tags %) tag) droplets)))

(defn credentials [token] {:token token})
(defn ls [creds]
  (filter-by-tag (:droplets (do/droplets (:token creds)))
                 default-tag-name ))
(defn create [])
(defn destroy [])
(defn pause [])
(defn start [])
(defn stop [])
(defn exists? [])

(defrecord DigitalOcean []
  provider/Provider)

;; docs
(defn ls-images [creds]
  (do/images (:token creds)))

(defn ls-sizes [creds]
  (do/sizes (:token creds)))

(defn ls-keys [creds]
  (do/ssh-keys (:token creds)))

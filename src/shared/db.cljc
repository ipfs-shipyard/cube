(ns shared.db
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.pprint :refer [pprint]]))

;; This file contains bunch of clojure specs for all values that goes into the
;; DB. If changes are being made that doesn't fit these specs, the change will
;; fail and be rolled back.

;; See more: https://clojure.org/about/spec

;; Utils
(s/def :cube.util/not-empty-string (s/and string? (complement empty?)))

;; Instances
(s/def :cube.instances.docker/go-ipfs-id :cube.util/not-empty-string)
(s/def :cube.instances.docker/ipfs-cluster-id :cube.util/not-empty-string)

(s/def :cube.instances.docker/metadata (s/keys :req-un [:cube.instances.docker/go-ipfs-id
                                                        :cube.instances.docker/ipfs-cluster-id]))

(s/def :cube.instances.do/ssh-key :cube.util/not-empty-string)
(s/def :cube.instances.do/ipv4 :cube.util/not-empty-string)

(s/def :cube.instances.do/metadata (s/keys :req-un [:cube.instances.do/ssh-key
                                                    :cube.instances.do/ipv4]))

(s/def :cube.instances/type #{:docker :do})
(s/def :cube.instances/count (s/and int? (s/or :positive pos? :zero zero?)))

(s/def :cube.instances/cluster-api :cube.util/not-empty-string)

(s/def :cube.instances/metadata (s/or :docker :cube.instances.docker/metadata
                                      :do :cube.instances.do/metadata))

(s/def :cube.instances/instance (s/keys :req-un [:cube.instances/type
                                           :cube.instances/cluster-api
                                           :cube.instances/metadata]))


(s/def :cube.instances/wanted (s/map-of :cube.instances/type :cube.instances/count))

(s/def :cube.instances/cluster-secret :cube.util/not-empty-string)

(s/def :cube.instances/running (s/map-of keyword? :cube.instances/instance))

(s/def :cube/instances (s/keys :req-un [:cube.instances/cluster-secret
                                        :cube.instances/wanted
                                        :cube.instances/running]))

;; Pins
;; TODO needs to have all pinning statuses from ipfs-cluster

(s/def :cube.pin.peer/status #{:pinned
                               :pinning
                               :pin_error
                               :unpin_error})

(s/def :cube.pin.peer/peer-id :cube.util/not-empty-string)
(s/def :cube.pin.peer/timestamp :cube.util/not-empty-string)

(s/def :cube.pin/peer (s/keys :req-un [:cube.pin.peer/peer-id
                                       :cube.pin.peer/status
                                       :cube.pin.peer/timestamp]))

(s/def :cube.pin/peer-map (s/coll-of :cube.pin/peer))

;; TODO Currently only validates it starts with `Qm` and at least 10 characters
;; Should be implemented as specs in multiformats/clj-multihash instead
(defn multihash? [cid]
  (let [chs (take 2 cid)
        [f s] chs]
    (if (= f \Q)
      (if (= s \m)
        (if (> (count cid) 10)
          true
          false)
        false)
      false)))

(s/def :cube/multihash (s/and :cube.util/not-empty-string multihash?))

(s/def :cube.pin/cid (s/with-gen
                       :cube/multihash
                       #(gen/fmap (fn [s] (str "Qm" s)) (gen/string-alphanumeric))))

(s/def :cube.pin/name :cube.util/not-empty-string)

(s/def :cube.pin/size (s/and int? pos?))

(s/def :cube.pins/pin (s/keys :req-un [:cube.pin/cid
                                       :cube.pin/name
                                       :cube.pin/size
                                       :cube.pin/peer-map]))

(s/def :cube/pins (s/coll-of :cube.pins/pin))

(s/def :cube/db (s/keys :opt-un [:cube/instances
                                 :cube/pins]))

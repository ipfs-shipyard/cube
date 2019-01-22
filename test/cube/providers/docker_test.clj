(ns cube.providers.docker-test
  (:require [cube.providers.docker :as provider-docker] :reload-all
            [clojure.pprint :refer [pprint]])
  (:use clojure.test))

(defn test-db [] {:state (atom {:name "barry"
                                :numbers [5]
                                :instances {:running {}}})})

(deftest create-id
  (let [[k v] (provider-docker/create-id "go-ipfs-id" "ipfs-cluster-id")]
    (is (not (nil? k)))
    (is (= :docker (:type v)))
    (is (= "go-ipfs-id" (:go-ipfs v)))
    (is (= "ipfs-cluster-id" (:ipfs-cluster v)))))

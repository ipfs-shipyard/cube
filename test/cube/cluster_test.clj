(ns cube.cluster-test
  (:require [cube.cluster :as cluster]
            [clojure.spec.alpha :as s])
  (:use clojure.test))

(def example-res '({:cid "QmAUeoxg6D9fYnfwXrVmVmkTm4QhTDTjyoE6ZfKtdGFSLm",
                    :peer_map
                    {:QmBbwt9jwWg4q7YMLkG7De2B9MaE7iYZjvV1eWJs4byEvK
                     {:cid "QmAUeoxg6D9fYnfwXrVmVmkTm4QhTDTjyoE6ZfKtdGFSLm",
                      :peer "QmBbwt9jwWg4q7YMLkG7De2B9MaE7iYZjvV1eWJs4byEvK",
                      :peername "21bad271db77",
                      :status "pinned",
                      :timestamp "2019-01-20T15:21:23Z",
                      :error ""},
                     :QmVqRSNLNqFCKRKV1N4g2Wqi3wpdxhrtgiHm8mzsnQa6om
                     {:cid "QmAUeoxg6D9fYnfwXrVmVmkTm4QhTDTjyoE6ZfKtdGFSLm",
                      :peer "QmVqRSNLNqFCKRKV1N4g2Wqi3wpdxhrtgiHm8mzsnQa6om",
                      :peername "b14754ed2375",
                      :status "pinned",
                      :timestamp "2019-01-20T15:21:23Z",
                      :error ""}}}
                   {:cid "QmS4fTD7Rh8NouNhp9uZbawUdBpgHaUTeL3EnxX8Tiiu7H",
                    :peer_map
                    {:QmBbwt9jwWg4q7YMLkG7De2B9MaE7iYZjvV1eWJs4byEvK
                     {:cid "QmS4fTD7Rh8NouNhp9uZbawUdBpgHaUTeL3EnxX8Tiiu7H",
                      :peer "QmBbwt9jwWg4q7YMLkG7De2B9MaE7iYZjvV1eWJs4byEvK",
                      :peername "21bad271db77",
                      :status "pinned",
                      :timestamp "2019-01-20T15:20:28Z",
                      :error ""},
                     :QmVqRSNLNqFCKRKV1N4g2Wqi3wpdxhrtgiHm8mzsnQa6om
                     {:cid "QmS4fTD7Rh8NouNhp9uZbawUdBpgHaUTeL3EnxX8Tiiu7H",
                      :peer "QmVqRSNLNqFCKRKV1N4g2Wqi3wpdxhrtgiHm8mzsnQa6om",
                      :peername "b14754ed2375",
                      :status "pinned",
                      :timestamp "2019-01-20T15:20:28Z",
                      :error ""}}}))

(defn format-test [res-to-use]
  (let [res (cluster/format-res res-to-use)]
    (is (= "QmAUeoxg6D9fYnfwXrVmVmkTm4QhTDTjyoE6ZfKtdGFSLm" (:cid (first res))))
    (is (= 2 (count (:peer-map (first res)))))
    (is (= "QmBbwt9jwWg4q7YMLkG7De2B9MaE7iYZjvV1eWJs4byEvK" (:peer-id (first (:peer-map (first res))))))
    (is (= nil (:error (first (:peer-map (first res))))))
    (is (= :pinned (:status (first (:peer-map (first res))))))))

(deftest format-pins-response
  (format-test example-res))

;; Makes sure that whatever way the response is from ipfs-cluster, we make it
;; into the same order
(deftest format-pins-response-sorts-res
  (format-test (reverse example-res)))

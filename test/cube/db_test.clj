(ns cube.db-test
  (:require [cube.db :as db] :reload-all
            [clojure.pprint :refer [pprint]])
  (:use clojure.test))

(defn test-db [] {:db-path "/tmp/test-cube-db.clj"
                  :state (atom {:name "barry"
                                :numbers [5]
                                :nested {:name "larry"}
                                :instances {:running {}}})})

(deftest access-value
  (is (= "barry" (db/access (test-db) :name)))
  (is (= [5] (db/access (test-db) :numbers))))

(deftest access-in-value
  (is (= "barry" (db/access-in (test-db) [:name])))
  (is (= 5 (db/access-in (test-db) [:numbers 0])))
  (is (= {} (db/access-in (test-db) [:instances :running]))))

(deftest put-value
  (let [new-db (test-db)]
    (db/put new-db :testing false)
    (is (= false (db/access new-db :testing)))))

(deftest put-in-value
  (let [new-db (test-db)]
    (db/put-in new-db [:instances :running :test-id] true)
    (is (= true (db/access-in new-db [:instances :running :test-id])))))

(deftest remove-value
  (let [new-db (test-db)]
    (db/remove new-db :name)
    (is (= nil (db/access new-db :name)))))

(deftest remove-in-value
  (testing "Remove one key"
    (let [new-db (test-db)]
      (db/remove-in new-db [:name])
      (is (= nil (db/access-in new-db [:name])))))
  (testing "Not remove empty maps when removing nested values"
    (let [new-db (test-db)]
      (db/remove-in new-db [:nested :name])
      (is (= {} (db/access-in new-db [:nested])))
      (is (= nil (db/access-in new-db [:nested :name]))))))

(deftest add-to-value
  (let [new-db (test-db)]
    (db/add-to new-db [:numbers] 1)
    (is (= [5 1] (db/access-in new-db [:numbers])))))

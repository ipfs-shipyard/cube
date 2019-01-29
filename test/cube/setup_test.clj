(ns cube.setup-test
  (:require [cube.setup :as setup])
  (:use clojure.test)
  (:import [cube.setup Setup]))

(def test-password "lol")

(defn test-db [] {:db-path "/tmp/test-cube-db.clj"
                  :state (atom {:setup {:completed? false
                                        :password test-password}})})

(defn empty-db [] {:db-path "/tmp/test-cube-db.clj"
                   :state (atom {})})

(deftest get-password
  (is (= test-password (setup/get-password (test-db))))
  (is (not= "hello" (setup/get-password (test-db)))))

(deftest get-completed
  (is (= false (setup/completed? (test-db)))))

(deftest set-completed
  (let [db (test-db)]
      (setup/set-completed db)
      (is (= true (setup/completed? db)))))

(deftest create-password
  (with-out-str ;; TODO find better way of hiding output from showing through
    (is (not= "" (setup/create-password)))))

(deftest check-setup-pass
  (is (= true (setup/check-setup-pass (test-db) test-password)))
  (is (= false (setup/check-setup-pass (test-db) "eh"))))

(deftest check-setup-pass-with-body
  (is (= :success (setup/check-setup-pass (test-db) test-password :success)))
  (is (= false (setup/check-setup-pass (test-db) "eh" :success))))

(deftest setup-system
  (let [system (-> (Setup. (empty-db))
                   (.start))]
      (is (= false (setup/completed? (:db system))))
      (is (not (nil? (setup/get-password (:db system)))))))

(deftest setup-system-load-db
  (let [system (-> (Setup. (test-db))
                   (.start))]
      (is (= false (setup/completed? (:db system))))
      (is (= "lol" (setup/get-password (:db system))))))

(ns cube.auth-test
  (:require [cube.auth :as auth]
            [cube.db :as db]
            [crypto.password.bcrypt :as bcrypt])
  (:use clojure.test))

(defn test-db [] {:db-path "/tmp/test-cube-db.clj"
                  :state (atom {:name "barry"
                                :age 16})})

(defn user [username, password, role]
  {:username username
   :password (bcrypt/encrypt password)
   :roles #{role}})

(def permissions {:guest []
                  :reader [[:name :read]]
                  :writer [[:name :read] [:name :write]]})

(def users {"guest" (user "guest" "guest" :guest)
            "reader" (user "reader" "reader" :reader)
            "writer" (user "writer" "writer" :writer)})

(defn get-user [n]
  (get-in users [n]))

(deftest authorization
  (testing "turns role(s) into permissions"
      (is (= (auth/roles->permissions permissions #{}) []))
      (is (= (auth/roles->permissions permissions #{:guest}) []))
      (is (= (auth/roles->permissions permissions #{:reader}) [[:name :read]]))
      (is (= (auth/roles->permissions permissions #{:writer}) [[:name :read] [:name :write]])))

  (testing "can check permissions against keys and access-type"
      (is (= (auth/allowed? (:guest permissions) :name :read) false))
      (is (= (auth/allowed? (:guest permissions) :name :write) false))
      (is (= (auth/allowed? (:guest permissions) :random :read) false))

      (is (= (auth/allowed? (:reader permissions) :name :read) true))
      (is (= (auth/allowed? (:reader permissions) :name :write) false))
      (is (= (auth/allowed? (:reader permissions) :random :read) false))

      (is (= (auth/allowed? (:writer permissions) :name :read) true))
      (is (= (auth/allowed? (:writer permissions) :name :write) true))
      (is (= (auth/allowed? (:writer permissions) :random :read) false))

      ;; Invalid access-type, needs to be :read or :write
      (is (thrown? Exception (auth/allowed? (:writer permissions) :name :lol))))

  (testing "can check user with permissions against keys and access-type"
      (is (= (auth/authorized? permissions (get-user "guest") :name :read) false))
      (is (= (auth/authorized? permissions (get-user "guest") :name :write) false))
      (is (= (auth/authorized? permissions (get-user "guest") :random :read) false))

      (is (= (auth/authorized? permissions (get-user "reader") :name :read) true))
      (is (= (auth/authorized? permissions (get-user "reader") :name :write) false))
      (is (= (auth/authorized? permissions (get-user "reader") :random :read) false))

      (is (= (auth/authorized? permissions (get-user "writer") :name :read) true))
      (is (= (auth/authorized? permissions (get-user "writer") :name :write) true))
      (is (= (auth/authorized? permissions (get-user "writer") :random :read) false))

      ;; Invalid access-type, needs to be :read or :write
      (is (thrown? Exception (auth/authorized? permissions (get-user "writer") :name :lol)))))

(deftest authentication
  (testing "authenticated?"
    (is (= (auth/authenticated? users (auth/split-auth "")) false))
    (is (= (auth/authenticated? users (auth/split-auth "guest")) false))
    (is (= (auth/authenticated? users (auth/split-auth "guest:g")) false))
    (is (= (auth/authenticated? users (auth/split-auth "guest:guest")) true))))

(deftest web-authentication
  (testing "parsing cookie authentication format username:password"
    (is (= (auth/split-auth nil) {:username "" :password ""}))
    (is (= (auth/split-auth "") {:username "" :password ""}))
    (is (= (auth/split-auth "a") {:username "" :password ""}))
    (is (= (auth/split-auth "a:") {:username "" :password ""}))
    (is (= (auth/split-auth "a:b") {:username "a" :password "b"}))))

(deftest db-interface
  (testing "protects db access function"
    (is (thrown? Exception (auth/protected-access permissions (get-user "guest") (test-db) :name)))
    (is (= (auth/protected-access permissions (get-user "reader") (test-db) :name) "barry"))
    (is (= (auth/protected-access permissions (get-user "writer") (test-db) :name) "barry")))
  (testing "protects db write function"
    (is (thrown? Exception (auth/protected-put permissions (get-user "guest") (test-db) :name "john")))
    (is (thrown? Exception (auth/protected-put permissions (get-user "reader") (test-db) :name "john")))
    (is (= (auth/protected-put permissions (get-user "writer") (test-db) :name "john")))))

(deftest db-filtering
  (testing "protects the entire db based on auth of user"
    (is (= (db/access (auth/filter-db permissions (get-user "guest") (test-db)) :name) nil))
    (is (= (db/access (auth/filter-db permissions (get-user "reader") (test-db)) :name) "barry"))
    (is (= (db/access (auth/filter-db permissions (get-user "writer") (test-db)) :name) "barry"))))

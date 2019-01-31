(ns cube.auth
  (:require [cube.db :as db]
            [clojure.pprint :refer [pprint]]
            [crypto.password.bcrypt :as bcrypt]))

;; This namespace should maybe be two parts. Authentication and Authorization

;; Format for a permission: vector of vectors. Each vector has the keys being
;; the namespaces, and the last one deciding the access-type
;; [:person :name :read] would say that (get-in [:person :name]) is allowed.
;; [:name :write] would say that (assoc-in [:name] value) is allowed.
;; TODO hardcoded users, roles and permissions for now
(def permissions {:viewer [[:name :read]]
                  :pinner [[:name :read] [:name :write]]
                  :guest  []})

;; Currently only takes the first role, as one user currently can only have
;; one role. In the future this might change.
(defn roles->permissions [permissions roles]
  (let [role (first roles)]
    (if (nil? role)
      []
      (role permissions))))

(def access-type-error-msg
  "'%s' is not a valid access-type. You need either :read or :write")

;; Checks a list of permissions against k and access-type
(defn allowed? [permissions k access-type]
  (if (not (or (= access-type :read) (= access-type :write))) ;; wrong access-type
    (throw (Exception. (format access-type-error-msg access-type)))
    (let [permission (first permissions)]
      (if (nil? permission) ;; no permissions left to check
        false
        (let [[role-k role-a] permission]
          (if (and (= role-k k) (= role-a access-type))
            true ;; had the right permission
            (recur (rest permissions) k access-type))))))) ;; not here, continue

;; Calls `allowed?` but easier to use as we can just pass the user directly
(defn authorized? [permissions user k access-type]
  (let [permissions (roles->permissions permissions (:roles user))]
    (allowed? permissions k access-type)))

(def unauthorized-error-msg
  "User '%s' with roles '%s' did not have access to '%s' in the '%s' namespace")

(defn unauthorized-error [user k access-type]
  (throw (Exception. (format unauthorized-error-msg (:username user) (:roles user) access-type k))))

;; Interface functions between a user and a DB to protected the DB against
;; unallowed reads and writes
(defn- protected-read [permissions user db k func-name]
  (if (authorized? permissions user k :read)
    ((ns-resolve 'cube.db (symbol func-name)) db k)
    (unauthorized-error user k :read)))

(defn- protected-write [permissions user db k v func-name]
  (if (authorized? permissions user k :write)
    ((ns-resolve 'cube.db (symbol func-name)) db k v)
    (unauthorized-error user k :write)))

(defn protected-access [permissions user db k]
  (protected-read permissions user db k "access"))

(defn protected-put [permissions user db k v]
  (protected-write permissions user db k v "put"))

(defn permission-keys [permissions]
  (reduce (fn [acc [p _]] (conj acc p)) #{} permissions))

(defn filter-keys-from-state [included-ks state]
  (let [new-state (atom {})]
    (doseq [k included-ks]
      (swap! new-state assoc k (k state)))
    new-state))

;; Function that returns a new DB, but with it's fields only being the
;; ones the user supposedly have :read access to.
;; TODO currently has internal knowledge of DB structure...
(defn filter-db [permissions user db]
  (let [old-state @(:state db)
        ks (permission-keys (roles->permissions permissions (:roles user)))]
    (assoc db :state (filter-keys-from-state ks old-state))))

(defn split-auth [auth-string]
  (if auth-string
    (let [[username password] (clojure.string/split auth-string #":")]
      (if (or (empty? username) (empty? password))
        {:username "" :password ""}
        {:username username
         :password password}))
    {:username ""
     :password ""}))

(defn authenticated? [users user]
  (let [wanted-user (get-in users [(:username user)])]
    (if (nil? wanted-user)
      false ;; didnt find user
      (bcrypt/check (:password user) (:password wanted-user)))))

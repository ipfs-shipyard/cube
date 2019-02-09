(ns cube.providers.do
  (:require [com.stuartsierra.component :as c]
            [clojure.spec.alpha :as s]
            [clj-docker-client.core :as docker]
            [clj-http.client :as http]
            [clojure.string :refer [trim]]
            [clojure.pprint :refer [pprint]]
            [cube.providers.docker :as provider-docker]
            [cube.db :as db]
            [clojure.spec.alpha :as s]
            [clj-ssh.ssh :as ssh]))

;; This is not used at all currently, as the provider is not ready

;; Steps needed for creating a digitalocean host:

;; create ssh-key
;; use auth token from preferences
;; create the instance
;; ssh in
;; use all the docker functions to create the actual cluster

(defonce auth-token "d85f7311cda382899e6dfe7dbae13b3cfb6d66a68a0f5b372d68a02074a6da5b")
(defonce base-api "https://api.digitalocean.com/v2")
(defonce default-ssh-key-name "cube-deploy-key")
(defonce key-store (str (System/getProperty "user.home") "/.cube/"))
(defonce provider-type :do)

(s/def ::private (s/and string? (complement empty?)))
(s/def ::public (s/and string? (complement empty?)))
(s/def ::ssh-key (s/keys :req-un [::private ::public]))

(defn save-ssh-key [private public]
  (do (spit (str key-store "/" default-ssh-key-name) private)
      (spit (str key-store "/" default-ssh-key-name ".pub") public)))

(defn load-ssh-key [path]
  (let [private (slurp path)
        public (slurp (str path ".pub"))]
    {:private (trim private)
     :public (trim public)}))

(defn bytes->ssh-key [private-bytes public-bytes]
  {:private (String. private-bytes)
   :public (String. public-bytes)})

(defn list-ssh-keys! [token]
  (-> (http/get (str base-api "/account/keys")
                   {:socket-timeout 1000
                    :conn-timeout 1000
                    :headers {:Authorization (str "Bearer " token)}
                    :as :json})
      :body
      :ssh_keys))

(defn create-ssh-key! [token key-name public-key]
  (:body (http/post (str base-api "/account/keys")
                   {:socket-timeout 1000
                    :conn-timeout 1000
                    :headers {:Authorization (str "Bearer " token)}
                    :form-params {:name key-name
                                  :public_key public-key}
                    :content-type :json
                    :as :json})))

(defn get-droplet [token id]
  (-> (http/get (str base-api "/droplets/" id)
                   {:headers {:Authorization (str "Bearer " token)}
                    :content-type :json
                    :as :json})
      :body
      :droplet))

(defn create-droplet! [token name region size image tags ssh-keys]
  (-> (http/post (str base-api "/droplets")
                   {:headers {:Authorization (str "Bearer " token)}
                    :form-params {:name name
                                  :region region
                                  :size size
                                  :image image
                                  :tags tags
                                  :ssh_keys ssh-keys}
                    :content-type :json
                    :as :json})
      :body
      :droplet
      :id))

(defn has-ssh-key? [ks key-name]
  (boolean (some #(= (:name %) key-name) ks)))

(def default-region "ams2")
(def default-size "s-2vcpu-2gb")
(def default-image "ubuntu-18-10-x64")
(def default-tags ["cube"])

(defonce test-droplet "82.196.10.174")

(defn find-first
  [f coll]
  (first (filter f coll)))

(defn ssh-key->id [auth-token k]
  (:id (find-first #(= (:public_key %) (:public k)) (list-ssh-keys! auth-token))))

(defn ssh-error [ip cmd err]
  (throw (Throwable. (format "Failed to run cmd '%s' on host '%s'. Received error: '%s'" cmd ip err))))

(defn execute-via-ssh [ip cmd]
  (let [agent (ssh/ssh-agent {:use-system-ssh-agent false})]
    (ssh/add-identity agent {:private-key-path (str key-store default-ssh-key-name)})
    (let [session (ssh/session agent ip {:strict-host-key-checking :no
                                                   :username "root"})]
      (ssh/with-connection session
        (let [result (ssh/ssh session {:cmd cmd})]
          (when (not (= (:exit result) 0))
              (ssh-error ip cmd (:err result)))
          result
          )))))

(defn ssh-install-docker [ip]
  (execute-via-ssh ip "curl https://get.docker.com | bash"))

(defn log [msg]
  (println (str "[digitalocean] " msg)))

(defrecord ProviderDigitalOcean []
  c/Lifecycle
  (start [this]
    (log "Starting...")
    this)
  (stop [this]
    (log "Stopping...")
    this))

(defn new []
  (map->ProviderDigitalOcean {}))


(defn ensure-ssh-key! [token]
  (when (not (has-ssh-key? (list-ssh-keys! token) default-ssh-key-name))
    (let [agent (ssh/ssh-agent {})]
      (let [[private public] (ssh/generate-keypair agent :rsa 1024 "")
            ssh-key (bytes->ssh-key private public)]
        (save-ssh-key (String. private) (String. public))
        (create-ssh-key! token default-ssh-key-name (String. public))))))

(defn create-default-droplet! [token]
  (let [ssh-keys [(ssh-key->id token (load-ssh-key (str key-store default-ssh-key-name)))]]
    (create-droplet!
      token
      "cube-01"
      default-region
      default-size
      default-image
      default-tags
      ssh-keys)))

(defn droplet-ready? [token id]
  (= (:status (get-droplet token id)) "active"))

(defn wait-for-droplet-ready
  ([token id]
   (wait-for-droplet-ready token id 1000 10 0))
  ([token id backoff max-tries current-tries]
   (if (= max-tries current-tries)
     false
     (if (droplet-ready? token id)
       true
       (do (Thread/sleep backoff)
           (wait-for-droplet-ready token id (* backoff 2) max-tries (+ current-tries 1)))))))

(defn droplet-id->ip [token id]
  (:ip_address (first (-> (get-droplet token id)
                          :networks
                          :v4))))

(def docker-override "
[Service]
ExecStart=
ExecStart=/usr/bin/dockerd -H fd:// -H tcp://127.0.0.1:2376")

(def docker-enable-http-api
  (clojure.string/join
    " && "
    ["mkdir /etc/systemd/system/docker.service.d/"
     "cd /etc/systemd/system/docker.service.d"
     (format "echo '%s' >> override.conf" docker-override)
     "systemctl daemon-reload"
     "systemctl restart docker.service"]))

(defn docker-create [conn db]
  (let [go-ipfs-id (provider-docker/create-go-ipfs conn)
        ;; should be the external IP of the digital ocean droplet
        go-ipfs-ip (provider-docker/get-ip conn go-ipfs-id)
        start-cmd (provider-docker/get-start-cmd db conn)
        secret (db/access-in db [:instances :cluster-secret])
        ipfs-cluster-id (provider-docker/create-ipfs-cluster conn start-cmd go-ipfs-ip secret)]
    (provider-docker/save-instance conn db provider-type go-ipfs-id ipfs-cluster-id)))

;; the approach of using local port forwarding and talk with the nodes
;; that way won't work. As we're running one container of each image on each
;; host, we need to bind to the private and public interface, go-ipfs on the
;; public one and ipfs-cluster on the private. The creation of ipfs-cluster
;; config needs to not be in the docker stuff
;; abstract stuff into a general provider

;; cluster should listen of internal network
;; which means we have to proxy the calls
;; ipfs-cluster should be the one extracting the multiaddrs, depending on :type

(defn run-containers [db auth-token ip]
  (let [agent (ssh/ssh-agent {})]
    (ssh/add-identity agent {:private-key-path (str key-store default-ssh-key-name)})
    (let [session (ssh/session agent ip {:strict-host-key-checking :no
                                         :username "root"})]
      (ssh/with-connection session
        (ssh/with-local-port-forward [session 9321 2376]
          (with-open [conn (docker/connect (str "http://localhost:9321"))]
            (provider-docker/initialize conn)
            ;; have to inject the session so we can proxy a docker address,
            ;; not just localhost
            (docker-create conn db)
            ))))))


(defn create [db]
  (do (log "creating new instance")
    (ensure-ssh-key! auth-token)
      (let [instance-id (create-default-droplet! auth-token)]
        ;; should enter the instance into the db here
        ;; also should put the rest in a future to not block everything else
        (log (str "created, got id " instance-id))
        (wait-for-droplet-ready auth-token instance-id)
        (log "waited for it to become ready")
        (Thread/sleep (* 1000 10))
        (log "getting ip")
        (let [ip (droplet-id->ip auth-token instance-id)]
          (log "got ip, installing docker")
          (ssh-install-docker ip)
          (log "enabling docker api")
          (execute-via-ssh ip docker-enable-http-api)
          (log "running docker containers")
          (run-containers db auth-token ip)))))

;; a provision should happen in steps, and if a step is not completed, it
;; should return with a map with enough data so it can be repeated but with
;; only the steps that were failing

;; Steps to create a new droplet with go-ipfs + ipfs-cluster
;; - create and upload ssh key if not already exists
;; - create droplet
;; - wait for it to come online
;; - ssh in and install docker
;; - once install is done, run the `create` from the docker provider

(comment

  (create {})

  (ensure-ssh-key! auth-token)
  (create-default-droplet! auth-token)
  (wait-for-droplet-ready auth-token 132289402)
  (ssh-install-docker (droplet-id->ip auth-token 132289402))
  (execute-via-ssh (droplet-id->ip auth-token 132289402) docker-enable-http-api)

  (let [ssh-keys [(ssh-key->id auth-token (load-ssh-key (str key-store default-ssh-key-name)))]]
    (create-droplet! auth-token "cube-01" default-region default-size default-image default-tags ssh-keys))
  (pprint (ls-regions auth-token))
  (pprint (ls-images auth-token))
  ;; user@lasoa
  (has-ssh-key? (list-ssh-keys! auth-token) default-ssh-key-name)
  (has-ssh-key? (list-ssh-keys! auth-token) "user@lasoa")
  (pprint
    (create-ssh-key!
      auth-token
      default-ssh-key-name
      (:public (load-ssh-key (str key-store "/" default-ssh-key-name)))))
  (let [agent (ssh/ssh-agent {})]
    (let [[private public] (ssh/generate-keypair agent :rsa 1024 "")
          ssh-key (bytes->ssh-key private public)]
      (pprint ssh-key)
      (pprint (s/valid? ::ssh-key ssh-key))
      (save-ssh-key (String. private) (String. public))))
  )

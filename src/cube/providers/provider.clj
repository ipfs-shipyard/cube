(ns cube.providers.provider)

;; Not really used yet!

(defprotocol Provider
  "Provider manages hosts in some location"
  (credentials [token] "Creates a credentials map for authenticating with this provider")
  (create [instance-spec] "Creates a instance based on the passed in specification")
  (destroy [instance-id] "Destroys a instance based on the instance ID")
  (is-ready? [instance-id] "Checks if the instance is currently running"))

;; ;; Experimental API
;;   (s/def ::instance-opts {:name
;;                           :size
;;                           :region
;;                           :image_id })
;; 
;;   (defn create "Creates a new instance" [instance-opts] ^instance)
;;   (defn stop "Stops a instance" [instance])
;;   (defn remove "Removes an instance" [instance])
;; 
;;   ;; connection interface cluster <> instances
;; 
;;   ;; The API should look something like this
;; 
;;   ;; creating credentials
;;   (def creds (create-creds "aws" "username" "password"))
;; 
;;   ;; creating ssh-credentials
;;   (def ssh-creds (create-ssh-creds "username"))
;; 
;;   ;; create a new node
;;   (def node (create-node :default-ec2 node-spec))
;; 
;;   ;; make sure we're running one copy of this node
;;   (def change (converge creds node 1))
;; 
;;   ;; explain what changed
;;   (explain change)
;; 
;;   ;; create a new config named "ipfs-cluster" with `config-spec`
;;   (def config (create-config "ipfs-cluster" config-spec))
;; 
;;   ;; do the provisioning
;;   (def provision-change (provision ssh-creds node config))
;; 
;;   ;; explain what changed
;;   (explain provision-change)

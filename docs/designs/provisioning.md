## Provisioning

Provisioning the good 'ol fashioned way of just running commands willy-nilly
became shitty very quickly, as you have to wait for states before proceeding
to the next one, otherwise it fails and you have to begin from the beginning.

If you have steps that takes very long time, it's annoying to go through them
everytime something fails in the end. It's not very resillient and won't be
able to handle future changes.

Hence, I propose a design for provisioning changes. This will apply to both
the docker and digital ocean provisioners.

The most basic shape in a provision, is a `step`. It contains a command to run.

If the step fails, the provision cancels and reports back with what it has.

For example, when provisioning a Digital Ocean Droplet with go-ipfs and ipfs-cluster,
it could look something like this:

```clojure
(def provision-steps [{:name "Create Droplet"
                       :in [:token]
                       :command #(create-default-droplet! %1)
                       :out [:id]}
                      {:name "Wait for creation"
                       :in [:id]
                       :command #(wait-for-droplet-ready %1)}
                      {:name "Get Droplet IPs"
                       :in [:token :id]
                       :command #(droplet->ip %1 %2)
                       :out [:ip]}
                      {:name "Install Docker"
                       :in [:ip]
                       :command #(ssh-install-docker %1)}
                      {:name "Enable Docker HTTP API"
                       :in [:ip]
                       :command #(execute-via-ssh %1 docker-enable-http-api)}
                      {:name "Run go-ipfs container"
                       :in [:ip]
                       :command #(run-go-ipfs %1)}
                      {:name "Run ipfs-cluster container"
                       :in [:ip]
                       :command #(run-ipfs-cluster %1)}])
```

The steps will run sequentially.

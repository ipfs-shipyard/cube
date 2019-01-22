## Cube Architecture Overview

*Notice: All of this is currently in prototyping stage and can rapidly change
without notice*

Cube will act as a controller for a group of ipfs-cluster backed instances
hosted in various configurations.

In it's most basic shape, Cube manages how many instances of what instance image
runs where. It'll act a bit like CloudFormation but cloud-agnostic.

When starting Cube, Cube will ask a few questions regarding where to host the
cluster, what groups to create with what permissions, what users belong to what
group and what the default settings for a `pin` is.

Once the setup is complete, Cube will ensure that what the user wants, is actually
running in a live production environment in the chosen hosting platform.

When it's up-and-running, Cube will monitor the health of the cluster. This means
make sure it's responsive and check other user-specified attributes (like
min-space available).

While ipfs-cluster will continue with it's own state sharing while Cube is not
running, the monitoring and convergance of local<>remote state will only
happen when Cube itself is running. So it's advisable to deploy Cube somewhere
to run it 24/7.

Cube should also be able to deploy itself in the future.

### Birdview Architecture

```
                                                               +----------------------+
                                                               |      Provider A      |
                                                               |                      |
                                                               | +------------------+ |
                                                               | |                  | |
                                               +-------------> | | Instance         | |
                                               |               | |                  | |
                                               |               | | +--------------+ | |
                                               |               | | |              | | |
                                               |               | | | IPFS+Cluster | | |
                                               |               | | |              | | |
                                               |               | | +--------------+ | |
                                               |               | |                  | |
                                               |               | +------------------+ |
                                               |               |                      |
                                               |               +----------------------+
                      Cube                     |
                                               |
               +----------------+              |               +----------------------+
               |                |              |               |      Provider B      |
               |    Cube+API    |              |               |                      |
               | +------------+ |              |               | +------------------+ |
               | |*Auth       | |              |               | |                  | |
               | |*Persistance| |              +-------------> | | Instance         | |
      +------> | |*Web        | |              |               | |                  | |
      |        | |*Monitoring | |              |               | | +--------------+ | |
      |        | |*Instances  | +--------------+               | | |              | | |
      |        | +------------+ |              |               | | | IPFS+Cluster | | |
      |        |                |              |               | | |              | | |
      |        +----------------+              |               | | +--------------+ | |
      |                                        |               | |                  | |
+-----+-----+                                  |               | +------------------+ |
|           |                                  |               |                      |
|  Browser  |                                  |               | +------------------+ |
|           |                                  |               | |                  | |
+-----------+                                  +-------------> | | Instance         | |
|Cube Web UI|                                                  | |                  | |
+-----------+                                                  | | +--------------+ | |
                                                               | | |              | | |
                                                               | | | IPFS+Cluster | | |
                                                               | | |              | | |
                                                               | | +--------------+ | |
                                                               | |                  | |
                                                               | +------------------+ |
                                                               |                      |
                                                               +----------------------+
```

### Components

#### Basic Shape

A component follows a traditional lifecycle with injectable dependencies to
be flexible in testing and composition.

```
(defprotocol LifeCycle
  "Controls starting/stopping of a component"
  (start [component] "Starts a component passing in the global state")
  (stop [component] "Stops a component"))
```

#### DB (Persistance)

DB handles connection to a local/remote DB to persist the current state
of the running system. It establish the connection on `start` and disconnects
on `stop`.

Currently defaults to a `edn` file in `~/.cube`

#### Instances

Instances handles the creation/destruction of instances that runs ipfs-cluster
nodes. Different strategies for creating/destroying instances will be created,
with the default and most basic one being "Keep X number of nodes running". If
the number of currently running instances is below X, Instances will handle
the creation of the nodes. Another strategy in the future could be to make
sure free harddrive space is always above Y.

##### Providers

Different cloud-providers works differently. As long as included ones implement
the `Instances` protocol, it should be available to be used in Cube.

#### Cluster

Cluster components handles all connections to the running ipfs-cluster nodes.

#### Web

Web is the component that spins up a webserver and uses the other components
to allow the user to control the system via HTTP API endpoints.

It just implements the component LifeCycle protocol to start/stop the server.

### Not Yet Implemented Components

#### Monitoring

Monitoring handles gathering of metrics, persistance via Persistance component
and alerting if needed.

#### Auth

The Auth component handles users and groups. Users have username, passwords and
belong to one group. Groups have names and a list of permissions.

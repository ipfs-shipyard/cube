## Monitoring

Just another component.

### What we want to monitor?

- Disk + Memory + CPU + Bandwidth of Instance
- Sizes of Pins (actually saved, and in general when all pins are done)
- Cluster Health (nodes connected? Link health?)

### What else we want?
- Exposing Prometheus endpoint
- Compose go-ipfs + ipfs-cluster prometheus metrics
- Host metrics

## Other thoughts
- Embed Grafana?
- Storage in memory for now. In the future, need to buffered to disk
- Not together with the rest of application state, only chosen, last values
  go there, to avoid sending too much data to frontend

## Cube - Components (backend)

Cube as an application contains many different components for different purposes
in the system.

- Cluster - Handles communication with the different ipfs-cluster
- DB - Handles the mutable state and persistance
- Instances - Handles communication with different hosting services
- Scheduler - Manages recurring tasks that should be run continously
- Web - Handles web requests to the application, only available locally

Future but not yet implemented components

- Auth - Handles local authentication, protecting various endpoints and
  making sure the right users have access to the right things.
- Monitoring - Component that helps monitoring the status/health of Cube

For an overview of the components and how they work together, checkout
the [Overview document](./overview.md)

## Other Parts

- CLI - Runs the Cube system and opens GUI if needed
- Dev - Makes development easier, exposes functions for the repl
- GUI - Responsible for rendering the main application window
- System - Composition of all the other components

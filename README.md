# flux
Flux is an asynchronous, scalable, multi-tenant, distributed and reliable state machine based orchestrator. Flux intends to cater the needs of both a traditional state machine and also an advanced workflow orchestrator.

Flux provides capabilities like state management, audit, tracing through configurable constructs.

The Flux system design is organized into multiple layers in an attempt to be holistic about building, testing, deploying and operating workflows in a shared environment:

![Layers](https://github.com/flipkart-incubator/flux/raw/master/docs/flux-high-level.png)

* UI - Provides for inspecting workflow execution and administration tasks on deployed instances/templates
* API/Modelling - Provides primitives for modelling State machines and reacting to state transitions, complex DAG-like workflows
* Runtime - Multiple variants from single JVM to distributed and isolated runtimes
* Integration - Support for operating the system i.e. Monitoring, Metrics and RPC mechanisms for running isolated runtimes
* Deployment - Providing mechanisms for cluster discovery, work distribution

Please go through the [wiki](https://github.com/flipkart-incubator/flux/wiki) pages to find out more about flux.

## Getting help
For help regarding usage, or receiving important announcements, subscribe to the Flux users mailing list: http://groups.google.com/group/flux-users
To get involved in the evolution of Flux, subscribe to the Flux interest mailing list: http://groups.google.com/group/flux-interest

## Releases
| Release | Date | Description |
| --- | --- | --- |
| 1.0-beta | March 2016 | Beta release for first customers |


## Changelog
Changelog can be viewed in [CHANGELOG.md](https://github.com/flipkart-incubator/flux/blob/master/CHANGELOG.md) file

## License
Flux is licensed under : The Apache Software License, Version 2.0. Here is a copy of the license (http://www.apache.org/licenses/LICENSE-2.0.txt)

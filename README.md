# Flux
Flux is an asynchronous, scalable, optionally multi-tenant & distributed and reliable state machine based orchestrator. 
Flux can be used to build Workflows and Reactive apps that are stateful. 

Please go through the [wiki](https://github.com/flipkart-incubator/flux/wiki) pages to find out more about flux.

## Releases
| Release | Date | Description |
|:------------|:----------------|:------------|
| Version 1.0.1                    | Feb 2017      |  Bug fixes
| Version 1.0                      | Jan 2017      |  Initial stable release

## Changelog
Changelog can be viewed in [CHANGELOG.md](https://github.com/flipkart-incubator/flux/blob/master/CHANGELOG.md) file

## Why Flux
A number of real-world stateful systems like workflows, event-driven reactive apps can be modeled as state machines. Event sources are many - 
user actions, messages passed between systems and data changes to business entities. Flux provides a programming model and 
client SDK to build event-driven reactive apps. The runtime allows to run multiple state machine instances concurrently.

## Flux Visualization & Monitoring
Flux allows you to monitor real-time execution of all running state machine tasks. Default path for dashboard is http://localhost:9999/admin/dashboard

<img src="https://github.com/flipkart-incubator/flux/raw/master/docs/flux-cluster.png" width="800">

Additionally, you can also inspect individual state machine execution DAGs and audit data:

<img src="https://github.com/flipkart-incubator/flux/raw/master/docs/Flux-serial-workflow.png" width="500">

<img src="https://github.com/flipkart-incubator/flux/blob/master/docs/Flux-fork-join.png" width="500">

![Audit] (https://github.com/flipkart-incubator/flux/raw/master/docs/audit_records.png)

Visit https://www.youtube.com/watch?v=DxyNcntnVzQ&feature=youtu.be to see how Flux state machine instance graph and audit look like.

## Documentation and Examples
Flux examples are under "examples" module. Each example can be run independently. Flux has very few dependencies and the simplest
examples can be run in under 5 minutes.
Documentation is continuously being added to the Flux [wiki](https://github.com/flipkart-incubator/flux/wiki)

## Getting help
For help regarding usage, or receiving important announcements, subscribe to the Flux users mailing list: http://groups.google.com/group/flux-users
To get involved in the evolution of Flux, subscribe to the Flux interest mailing list: http://groups.google.com/group/flux-interest

## License
Flux is licensed under : The Apache Software License, Version 2.0. Here is a copy of the license (http://www.apache.org/licenses/LICENSE-2.0.txt)

## Building with Flux
The Flux system design is organized into multiple layers to support building, testing, deploying and operating workflows in a shared environment:

![Layers](https://github.com/flipkart-incubator/flux/raw/master/docs/flux-high-level.png) 

* UI - Provides for inspecting workflow execution and administration tasks on deployed instances/templates
* API/Modelling - Provides primitives for modelling State machines and reacting to state transitions, complex DAG-like workflows
* Runtime - Multiple variants from single JVM to distributed and isolated runtimes
* Integration - Support for operating the system i.e. Monitoring, Metrics and RPC mechanisms for running isolated runtimes
* Deployment - Providing mechanisms for cluster discovery, work distribution

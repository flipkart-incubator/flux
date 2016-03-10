# Design Considerations

The various sub-systems/components and design considerations for Flux are detailed below.

## Runtime
The flux runtime has to support the following models of deployment:

![Runtimes](https://github.com/flipkart-incubator/flux/raw/master/docs/Flux-runtimes.png)
* Local : Primarily used during development and functional testing. Runs inside an IDE with minimal resource dependencies - say a locally
hosted MySQL database for persisting state and event data.
* Managed : A hosted setup that is distributed and multi-tenanted. Teams may deploy their statemachines/workflows, Tasks & Hooks on this hosted setup.
SLA is managed by the hosted runtime for both : statemachine/workflow execution and user written Tasks and Hooks.
* Isolated : A hosted setup that is distributed and multi-tenanted. Teams may deploy their statemachines/workflows on this hosted setup. SLA is 
split - statemachine/workflow execution is managed by hosted service while Tasks/Hooks execution is provisioned and managed by clients.


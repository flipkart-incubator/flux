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

The runtime can be further split into:

### Flux Runtime

The Flux Runtime should support the following:

* Large number of active statemachine/workflow instances
* High throughput of state transition processing
* Highly available, Distributed in order to support volume and throughput as described above
* Event driven execution model (for satisfying State machine use cases)
* Suitably shielded from user code (Task, Hook, Event). Each of these can adversely impact resource availability(CPU, Persistence store IOPS) of Flux runtime and other tenants on the hosted service.
* Support for checkpointing execution, resume when needed.
* Audit trail of state transitions
* Dynamic state machine definitions
* Fair job scheduling
    * Wall-clock time based - for State transitions that have timeouts defined
    * Event based triggering of State transitions, Task execution
* Re-driver - needed for triggering, executing tasks that have timed out, support back-off strategies and eventually putting on hold



### Task Runtime

The Task Runtime needs to support:

* Pull model (for isolated Runtime deployment model)
* Push/Direct invocation when tasks execute in Local or Managed Runtime
* Provide for persisting Event data - raised as outcome of Task execution, and used to drive further state transitions
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
* Suitably shielded from User code (Task, Hook, Event). Each of these can adversely impact resource availability(CPU, Persistence store IOPS) of Flux runtime and other tenants on the hosted service.
* Support for checkpointing execution, resume when needed.
* Audit trail of state transitions
* Dynamic state machine definitions
* Fair job scheduling
    * Wall-clock time based - for State transitions that have timeouts defined
    * Event based triggering of State transitions, Task execution
* Re-driver - needed for triggering, executing tasks that have timed out, support back-off strategies and eventually putting on hold

**Tech Choices:**
The following technologies will be used by the Flux Runtime:
* [Spring](https://spring.io/) - DI framework for wiring the runtime components together to create a container and also swapping implementations suitable for the deployment : Local vs Managed.
[Trooper](https://github.com/regunathb/Trooper) or an alternate Spring startup environment.
* [Akka](http://akka.io/) actors and Cluster - for implementing the State machine primitives

### Task Runtime

The Task Runtime needs to support:

* Pull model (for isolated Runtime deployment model)
* Push/Direct invocation when tasks execute in Local or Managed Runtime
* Provide for persisting Event data - raised as outcome of Task execution, and used to drive further state transitions

## Persistence

Flux has the following persistence needs:
* State machine/Workflow meta data - Highly structured, Long-lived
* Event data - User defined structure, TTL is in order of hours or a few days
* Audit data - Structured, Long lived

**Tech Choices:**
The following technologies will be used by the Flux Persistence layer:
* MySQL - for State machine/Workflow meta data, Audit data and Event data (in Local runtime)
* HBase - for Event data (in Managed and Isolated runtimes)

## Isolation

Flux runtime should be suitably shielded from User code (Task, Hook, Event) as these can adversely impact resource availability(CPU, Persistence store IOPS) of Flux runtime and other tenants on the hosted service.
In addition, for clients that require isolation from other tenants in a hosted service, Flux should also support committed capacities for resource intensive workloads. 
User code like Task execution is usually CPU intensive and the throughput of execution can be controlled if clients deployed their own compute that can then participate in Flux.

The Isolated runtime supports pull based Task execution where Workers running on client compute can pull work (Task) from Flux managed service. In this mode of operation, the SLA is split between
Flux (for State machine/Workflow orchestration) and clients (For Task, Hook executions). Event data may still be managed centrally by the hosted Flux managed service.

**Tech Choices:**
The following technologies will be used for isolating runtimes:
* [Hystrix](https://github.com/Netflix/Hystrix) (or) a Hystrix wrapper like [Phantom](https://github.com/Flipkart/phantom) - for isolation of Runtime code from User code (Task, Hook, Event) 
* Http based endpoints on Flux Managed service for clients to pull Tasks for execution

## Monitoring

The Flux runtime would support monitoring of:

* Task execution - time, timeouts, failures
* Event payload sizes
* No. of executing State machines/Workflows per tenant
* StateMachine/Workflow visualization

## Productivity Features

The Flux primitives may be used as-is, in code form, and also made available for definition declaratively:

* DSL - for definition State Machine, State and Event definitions, Workflow DAG modelling.


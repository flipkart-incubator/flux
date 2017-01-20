## 1.0 (Jan 19, 2017) - Stable release
- **Changes:**
  - Deployment Unit dynamic load and unload
  - Metrics integration
  - UI advancements
  - Bug fixes
  
## 1.0.4-SNAPSHOT (Dec 09, 2016)
- **Changes:**
  - Local routers instead of Cluster singleton routers to avoid performance bottleneck and single point of failure 
  - Distributed Redriver
  - Fixed connection leaks in client library
  - Fixed request burst behaviour (incase of high qps, the requests used to end up as hystrix threadpool rejections, now they will wait in Akka Actor's mailbox)
  - Local retries implemented through Akka Scheduler and follow exponential backoff strategy
  - @SelectDataSource annotation coupled with @Transactional to specify the datastore for reads/writes
  - Support for tasks with multiple params of same type
  - Http API to find the workflows which are errored
  - few Akka cluster & UI changes

## 1.0.3-SNAPSHOT (Oct 24, 2016)
- **New features:**
  - Support for Deployment Units
  - Global redriver
  - Workflows versioning support
  - Support for Retriable exceptions
  - Http API for unsidelining sidelined/errored tasks

## 1.0-SNAPSHOT (June 16, 2016)
- **New features:**
  - Client SDK for writing workflows with @Workflow, @Task annotations
  - Implicit Concurrent execution of workflow instances, tasks
  - Support for Task execution timeouts and retries
  - Support for Local & Distributed execution across a number of Flux nodes
  - Http API interface for triggering workflows, inspection and monitoring
  - Real-time monitoring of Task execution across state machine instances
  - Visualization for inspecting individual State machine executions
  

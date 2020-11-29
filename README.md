# Gandaru Wallet Application
###### Daruma Project

The _Gandaru Wallet_ app allows users to register wallets with multiple accounts, deposit and withdraw money to and from them and to create transactions among them. 

The goal of this proof of concept is to implement the [Actor Model](https://doc.akka.io/docs/akka/current/typed/guide/actors-motivation.html) through the various tools provided by Lightbend's Akka libraries. In sum, these tools allow software developers to build **[highly reactive](https://www.lightbend.com/blog/reactive-manifesto-20)** applications with ease.

#### Tools Used

### Akka Typed Actors

#### [Akka Typed Persistence](https://doc.akka.io/docs/akka/current/typed/persistence.html)
Akka Persistence enables stateful actors to persist their state so that it can be recovered when an actor 
is either restarted, such as after a JVM crash, by a supervisor or a manual stop-start, or migrated within a cluster. The key concept behind Akka Persistence is that only the events that are persisted by the actor are stored, not the actual state of the actor (though actor state snapshot support is also available). The events are persisted by appending to storage (nothing is ever mutated) which allows for very high transaction rates and efficient replication. A stateful actor is recovered by replaying the stored events to the actor, allowing it to rebuild its state. This can be either the full history of changes or starting from a checkpoint in a snapshot which can dramatically reduce recovery times.

### Akka Cluster
Stateless Apps => the middle tier does not contain any application state, everything is stored in the DB. Every time you get a request, we need to query the database, execute the business logic, etc. This is easy to deploy, scale up and scale down (among other benefits), but it is not the best architecture for distributed systems. The problem is that in highly concurrent scenarios, the need to be constantly querying the database hinders the application. Moreover, although scalability can be achieved, this comes at the cost of need more storage space.

#### Concepts

### Event Sourcing

### CQRS

### Domain Driven Design

### TDD

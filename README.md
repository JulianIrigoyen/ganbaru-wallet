# Gandaru Wallet Application
###### Daruma Project
[![Coverage Status](https://coveralls.io/repos/github/JulianIrigoyen/ganbaru-wallet/badge.svg?branch=master)](https://coveralls.io/github/JulianIrigoyen/ganbaru-wallet?branch=master)
The _Gandaru Wallet_ app allows users to register wallets with multiple accounts, deposit and withdraw money to and from them and to create transactions among them. 

The goal of this proof of concept is to implement the [Actor Model](https://doc.akka.io/docs/akka/current/typed/guide/actors-motivation.html) through the various tools provided by Lightbend's Akka libraries. In sum, these tools allow software developers to build **[highly reactive](https://www.lightbend.com/blog/reactive-manifesto-20)** applications with ease.

#### Tools Used

### Akka Typed Actors

#### [Akka Typed Persistence](https://doc.akka.io/docs/akka/current/typed/persistence.html)
Akka Persistence enables stateful actors to persist their state so that it can be recovered when an actor 
is either restarted, such as after a JVM crash, by a supervisor or a manual stop-start, or migrated within a cluster. The key concept behind Akka Persistence is that only the events that are persisted by the actor are stored, not the actual state of the actor (though actor state snapshot support is also available). The events are persisted by appending to storage (nothing is ever mutated) which allows for very high transaction rates and efficient replication. A stateful actor is recovered by replaying the stored events to the actor, allowing it to rebuild its state. This can be either the full history of changes or starting from a checkpoint in a snapshot which can dramatically reduce recovery times.

### [Akka Cluster Sharding](https://doc.akka.io/docs/akka/current/typed/cluster-sharding.html)
Akka Cluster Sharding sits on top of Akka Cluster and distributes data in shards, and load across members of a cluster without developers needing to keep track of where data actually resides in the cluster. Data is stored in Actors that represent individual entities, identified by a unique key, which closely corresponds to an Aggregate Root in Domain-Driven Design terminology

#### Concepts

### Event Sourcing

### CQRS

### Domain Driven Design

### TDD

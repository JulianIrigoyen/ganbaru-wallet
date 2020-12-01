[![CircleCI](https://circleci.com/gh/circleci/circleci-docs.svg?style=svg)](https://app.circleci.com/pipelines/github/JulianIrigoyen/ganbaru-wallet)
[![Coverage Status](https://coveralls.io/repos/github/JulianIrigoyen/ganbaru-wallet/badge.svg?branch=master)](https://coveralls.io/github/JulianIrigoyen/ganbaru-wallet?branch=master)
# [Ganbaru Wallet Application](https://ganbaru-wallet.herokuapp.com/v1/doc)
###### Daruma Project
The _Ganbaru Wallet_ app allows users to register wallets with multiple accounts, deposit and withdraw money to and from them and to create transactions among them. 

The goal of this proof of concept is to implement the [Actor Model](https://doc.akka.io/docs/akka/current/typed/guide/actors-motivation.html) through the various tools provided by Lightbend's Akka libraries. In sum, these tools allow software developers to build **[highly reactive](https://www.lightbend.com/blog/reactive-manifesto-20)** applications with ease.

#### Tools Used

### Akka Typed Actors
In this context, where modeling was approached using Domain Driven Design, an actor is the minimum computational unit that lives in memory and encapsulates the state an aggregate entity of the domain (ie. a Wallet). By definition, an actor can only:

* Receive and send messages
* Spawn child actors 
* Change its behavior based on its history

Moreover, actors never expose their internal state: the only way of knowing such state is to send the actor a message and observing the response. 
This project implements the evolution of the classic Akka Actor library, Akka Typed. The main development in this newer version concerns the protocol first approach, by which developers have to _explicitly determine which *type*_ of messages each actor can handle and when. 

### Event Sourcing
The fundamental idea behind event sourcing is to allow changes in the state of our application to be captured in an Event object, and that these objects are *_persisted_* following a temporal sequence in which they where *_applied_* during the application's state lifecycle. This implies that all state can be represented by either an event or a sequence of events. State can be _recovered_ by replaying these persisted events. 

Following the typical 3 layer architecture, where we find: 
* Views       -> the UI through users interact with the system. 
* Appliaction -> the famous business logic.
* Persistence -> storage.

_*Events*_ suppose a 4th dimension, as they represent a significant change in state, and should be modeled as *something that has already happened*. 

How do we persist them?

#### [Akka Typed Persistence](https://doc.akka.io/docs/akka/current/typed/persistence.html)
" Akka Persistence enables stateful actors to persist their state so that it can be recovered when an actor 
is either restarted, such as after a JVM crash, by a supervisor or a manual stop-start, or migrated within a cluster. The key concept behind Akka Persistence is that only the events that are persisted by the actor are stored, not the actual state of the actor (though actor state snapshot support is also available). The events are persisted by appending to storage (nothing is ever mutated) which allows for very high transaction rates and efficient replication. A stateful actor is recovered by replaying the stored events to the actor, allowing it to rebuild its state. This can be either the full history of changes or starting from a checkpoint in a snapshot which can dramatically reduce recovery times. "


#### [Akka Cluster Sharding](https://doc.akka.io/docs/akka/current/typed/cluster-sharding.html)
" Akka Cluster Sharding sits on top of Akka Cluster and distributes data in shards, and load across members of a cluster without developers needing to keep track of where data actually resides in the cluster. Data is stored in Actors that represent individual entities, identified by a unique key, which closely corresponds to an Aggregate Root in Domain-Driven Design terminology. "

_Although not heavily enforced in this application (as it is only a PoC), the Gandaru Wallet is set up to be deployed as an Akka Cluster._ 

### TDD & CI/CD
Test Driven Design was part of this project (and should be of any.) *CircleCI* was configured as pipeline to enforce this.

### Coverage
In hand with TDD, coveralls and scoverage tools were configured to keep track of code coverage and to generate coverage reports. 

### OpenApi 3.0.2
[Documentation](https://ganbaru-wallet.herokuapp.com/v1/doc) created with OpenApi spec. Files located in /docs directory and bundled with bundle_documentation.sh.

### Running Locally
To run the Ganbaru Wallet App, clone the repository and execute
```
sbt run
```

The service can be easily tested using the [Postman Collection here](https://gofile.io/d/AFI7RL)

#### TODOS -


- [ ]   Implement  [Read side](https://cqrs.nu/Faq/read-sides#:~:text=What%20is%20a%20read%20side,be%20made%20on%20that%20model.) of the application would be the next logical step. 
- [ ]   NTH -> React UI...

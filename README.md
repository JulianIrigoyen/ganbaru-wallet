# Ganbaru Wallet

Ganbaru Wallet is a REST service for wallets. A wallet holds several accounts. Users deposit, withdraw, and transfer money between accounts. The code is a proof of concept for event sourcing with Akka Typed, written from November 2020 to May 2021.

## Stack

- Scala 2.13, Play 2.8 (HTTP layer)
- Akka 2.6 Typed: actors, Persistence, Cluster Sharding
- MongoDB through `akka-reactivemongo-plugin` (event journal)
- OpenAPI 3 spec in `docs/`
- CircleCI and scoverage for CI and coverage

## How it works

Each wallet is one event-sourced actor (`app/model/wallets/Wallet.scala`).

1. A REST request reaches `WalletRouter` under `/api/wallets`.
2. `WalletResource` sends a command to the wallet actor. Cluster Sharding finds or starts the actor by wallet id.
3. The actor turns the command into events. It stores the events in MongoDB.
4. The actor applies the events to its state. After a restart, it replays the events to rebuild the state.

Commands: create wallet, add account, get wallet, get account, get the account with the largest balance, deposit, withdraw, attempt transaction, roll back transaction, list transactions. `GanbaruServiceSettings` is a second actor. It links wallets to a service client.

The API spec is in `docs/api_v1.yaml`. The script `bundle_documentation.sh` bundles it to `public/docs/api_v1.html` with swagger-cli and redoc-cli.

## Run it

You need JDK 8 or 11, sbt, and a MongoDB instance.

1. Open `conf/wallets-akka.conf`. Set `mongo-uri` to your MongoDB. The committed value has the password replaced with `REDACTED`.
2. Open `conf/application.conf`. Set `play.http.secret.key` to your own value.
3. In `build.sbt`, remove the `credentials` line for `artifactory.linkedstore.com`. It points to a private repository you cannot reach.
4. Run `sbt run`.

Run the tests with `sbt test`.

## Status

Proof of concept, last commit 19 May 2021. The read side (queries over the events) is not built. There is no UI.

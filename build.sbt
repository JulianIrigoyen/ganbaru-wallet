name := "ganbaru-wallet"

version := "0.1"

scalaVersion := "2.13.4"

scalaVersion := "2.13.4"
val akkaVersion = "2.6.10"
val akkaHttpVersion = "10.2.1"
val akkaManagementVersion = "1.0.7"
val akkaRxmongoPlugin = "1.4.0"
val playJsonMapping = "1.1.2"


resolvers += "Typesafe releases" at "https://repo.typesafe.com/typesafe/releases"
resolvers += "Typesafe repository plugin" at "https://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"

resolvers += Resolver.bintrayRepo("null-vector", "releases")
libraryDependencies += "null-vector" %% "akka-reactivemongo-plugin" % akkaRxmongoPlugin
libraryDependencies += "null-vector" %% "play-json-mapping" % playJsonMapping

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.3.0"

// Akka Classic
libraryDependencies += "com.typesafe.akka" %% "akka-persistence" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-persistence-query" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-cluster" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-remote" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-discovery" % akkaVersion
// Akka Typed
libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-cluster-typed" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-cluster-sharding-typed" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-stream-typed" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion

// Akka test kits
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
libraryDependencies += "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test
libraryDependencies += "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test
libraryDependencies += "com.typesafe.akka" %% "akka-persistence-testkit" % akkaVersion % Test
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
libraryDependencies += "org.mockito" % "mockito-core" % "2.23.4" % Test
libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test


enablePlugins(PlayScala)


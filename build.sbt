import org.scoverage.coveralls.Imports.CoverallsKeys.coverallsToken
import sbt.Keys.libraryDependencies
import sbt.{Credentials, Resolver}

name := "services-orders-scala"
version := "2.0-SNAPSHOT"
scalaVersion := "2.13.3"
val akkaVersion = "2.6.10"
val akkaHttpVersion = "10.2.1"
val akkaManagementVersion = "1.0.7"
val akkaRxmongoPlugin = "1.4.0"
val playJsonMapping = "1.1.2"

scalacOptions := Seq(
  "-encoding", "UTF-8", "-target:jvm-1.8", "-deprecation",
  "-feature", "-unchecked",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-Ymacro-global-fresh-names",
  //  "-Ybackend-parallelism 16",
)

resolvers += "Akka Maven Repository" at "https://akka.io/repository"
credentials += Credentials("Artifactory Realm", "artifactory.linkedstore.com", "reader", "X+7Ahj&#F;{?%3LU")

resolvers += "Typesafe releases" at "https://repo.typesafe.com/typesafe/releases"
resolvers += "Typesafe repository plugin" at "https://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"
resolvers += Resolver.bintrayRepo("unisay", "maven")
resolvers += Resolver.bintrayRepo("null-vector", "releases")
resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

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

libraryDependencies += "com.twitter" %% "chill-akka" % "0.9.5"

libraryDependencies += "null-vector" %% "akka-reactivemongo-plugin" % akkaRxmongoPlugin
libraryDependencies += "null-vector" %% "play-json-mapping" % playJsonMapping

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.3.0"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "5.3"

libraryDependencies += guice
libraryDependencies += "net.codingwell" %% "scala-guice" % "4.2.6"

libraryDependencies += "com.lightbend.akka.management" %% "akka-management" % akkaManagementVersion
libraryDependencies += "com.lightbend.akka.management" %% "akka-management-cluster-http" % akkaManagementVersion
libraryDependencies += "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % akkaManagementVersion
//libraryDependencies += "com.newrelic.agent.java" % "newrelic-agent" % newrelicVersion.value
// Testing deps1
// Akka test kits
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
libraryDependencies += "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test
libraryDependencies += "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test
libraryDependencies += "com.typesafe.akka" %% "akka-persistence-testkit" % akkaVersion % Test
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
libraryDependencies += "org.mockito" % "mockito-core" % "2.23.4" % Test
libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test

fork := true

enablePlugins(PlayScala)
coverallsToken := Some("ZUAC7SL9ToirnZHj16F2Idx81MpNQ1VyO")

coverageExcludedPackages := "<empty>;router;.*Arguments.*;.*NewRelicEventDispatcher.*;.*AckCommand.*;.*Credentials.*"

sources in(Compile, doc) := Seq.empty

Test / fork := true
Test / javaOptions += "-XX:MaxRAMPercentage=80.0"
Test / javaOptions += "-XX:+UseG1GC"
Test / testOptions += Tests.Argument("-oD")
lazy val raml2html = TaskKey[Unit]("removeCacheFile", "Deletes a cache file")

raml2html := {
  import sys.process._
  println("Updating html documentation...")
  Seq("sh", "update_html_doc.sh") !
}

//compile in Compile := (compile in Compile).dependsOn(raml2html).value
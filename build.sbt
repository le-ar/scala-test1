name := "my-chat"

version := "1.0"

scalaVersion := s"2.13.13"

resolvers += "Akka library repository".at("https://repo.akka.io/maven")

lazy val akkaVersion = "2.6.20"
lazy val akkaHttpVersion = "10.2.10"

// Run in a separate JVM, to make sure sbt waits until all threads have
// finished before returning.
// If you want to keep the application running while executing other
// sbt tasks, consider https://github.com/spray/sbt-revolver/
fork := true

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "org.typelevel" %% "cats-core" % "2.9.0",

  "org.sangria-graphql" %% "sangria-relay" % "4.0.0",
  "org.sangria-graphql" %% "sangria-spray-json" % "1.0.2",
  "org.sangria-graphql" %% "sangria-slowlog" % "3.0.0",
  "org.sangria-graphql" %% "sangria-circe" % "1.3.2",
  "org.sangria-graphql" %% "sangria-akka-http-core" % "0.0.4",
  "org.sangria-graphql" %% "sangria-akka-http-circe" % "0.0.4",
  "org.sangria-graphql" %% "sangria-marshalling-api" % "1.0.7",
  "org.sangria-graphql" %% "sangria-marshalling-testkit" % "1.0.4" % "test",

  "io.circe" %% "circe-core" % "0.14.1",
  "io.circe" %% "circe-generic" % "0.14.1",
  "io.circe" %% "circe-parser" % "0.14.1",
  "io.circe" %% "circe-generic-extras" % "0.14.1",

  "com.google.inject" % "guice" % "5.1.0",
  "com.google.inject.extensions" % "guice-assistedinject" % "5.1.0",

  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "ch.qos.logback" % "logback-classic" % "1.2.13",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
)

scalacOptions += "-language:experimental.macros"

mainClass in (Compile, run) := Some("com.chat.Main")

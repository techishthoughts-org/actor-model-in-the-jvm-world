import sbt.Keys.libraryDependencies

ThisBuild / name := "actor-model-with-akka" // Set the name of the project

ThisBuild / version := "1.0.0-SNAPSHOT" // Set the version of the project

ThisBuild / scalaVersion := "3.3.4" // Explicitly declare Scala 3

lazy val root = (project in file(".")) // Define the root project
  .settings(
    name := "actor-model-with-akka",
    libraryDependencies ++= Seq(
      // Core Akka dependencies
      "com.typesafe.akka" %% "akka-actor" % "2.6.21" cross CrossVersion.for3Use2_13, // Add Akka actor
      "com.typesafe.akka" %% "akka-slf4j" % "2.6.21" cross CrossVersion.for3Use2_13, // Add SLF4J logging for Akka
      "com.typesafe.akka" %% "akka-testkit" % "2.6.21" % Test cross CrossVersion.for3Use2_13, // Add Akka TestKit for testing

                  // HTTP Server dependencies (using Scala 2.13 compatible versions)
      "com.typesafe.akka" %% "akka-http" % "10.2.10" cross CrossVersion.for3Use2_13, // Add Akka HTTP for REST endpoints
      "com.typesafe.akka" %% "akka-stream" % "2.6.21" cross CrossVersion.for3Use2_13, // Add Akka Streams for HTTP
      "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.10" cross CrossVersion.for3Use2_13, // JSON serialization for HTTP

      // JSON handling
      "io.spray" %% "spray-json" % "1.3.6" cross CrossVersion.for3Use2_13, // JSON serialization

      // Additional utilities
      "com.softwaremill.sttp.client4" %% "core" % "4.0.9", // Add STTP client - upgraded to stable
      "ch.qos.logback" % "logback-classic" % "1.4.12", // Add Logback for logging

      // Testing dependencies
      "org.scalatest" %% "scalatest" % "3.2.19" % Test // Add % Test to limit scope to test compilation
    )
  )

resolvers += "Maven Central" at "https://repo1.maven.org/maven2/"
resolvers += "Sonatype" at "https://oss.sonatype.org/releases/"

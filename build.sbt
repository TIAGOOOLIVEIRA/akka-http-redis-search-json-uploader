lazy val akkaHttpVersion = "10.2.9"
lazy val akkaVersion    = "2.6.19"
lazy val catsVersion    = "3.4.8"
lazy val http4sVersion = "0.18.26"

// Run in a separate JVM, to make sure sbt waits until all threads have
// finished before returning.
// If you want to keep the application running while executing other
// sbt tasks, consider https://github.com/spray/sbt-revolver/
fork := true

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.example",
      scalaVersion    := "2.13.4"
    )),
    name := "akka-http-quickstart-scala",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"                % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json"     % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-actor-typed"         % akkaVersion,
      "com.typesafe.akka" %% "akka-remote"              % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster"             % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-sharding"    % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-tools"       % akkaVersion,
      "com.typesafe.akka" %% "akka-stream"              % akkaVersion,
      "org.typelevel" %% "cats-effect"                  % catsVersion,
      "org.http4s" %% "http4s-dsl"                      % http4sVersion,
      "org.http4s" %% "http4s-blaze-server"             % http4sVersion,
      "org.http4s" %% "http4s-blaze-client"             % http4sVersion,
      "ch.qos.logback"    % "logback-classic"           % "1.2.3",
      "com.lihaoyi" %% "upickle" % "0.9.5",
      "com.lihaoyi" %% "os-lib" % "0.8.0",
      "redis.clients" % "jedis" % "4.2.3",
      "com.google.code.gson" % "gson" % "2.9.0",



      "com.typesafe.akka" %% "akka-http-testkit"        % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"                % "3.1.4"         % Test
    )
  )

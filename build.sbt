// Build and Scala version
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.6.1"

val AkkaVersion = "2.10.0"
val AkkaHttpVersion = "10.7.0"

lazy val sharedResources = (project in file("SharedResources"))
  .settings(
    name := "SharedResources",
    resolvers += "Akka library repository".at("https://repo.akka.io/maven"),
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.14",
      "org.scalatest" %% "scalatest" % "3.2.14" % Test,
      "org.scala-lang.modules" %% "scala-xml" % "2.3.0",
      "org.playframework" %% "play-json" % "3.0.4",
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
      "io.spray" %%  "spray-json" % "1.3.6"
    ),
  )

lazy val basicChess = (project in file("BasicChess"))
  .dependsOn(sharedResources)
  .settings(
    name := "BasicChess",
    resolvers += "Akka library repository".at("https://repo.akka.io/maven"),
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.14",
      "org.scalatest" %% "scalatest" % "3.2.14" % Test,
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
      "io.spray" %%  "spray-json" % "1.3.6"
    ),
  )

lazy val devourChess = (project in file("DevourChess"))
  .dependsOn(sharedResources)
  .settings(
    name := "DevourChess",
    resolvers += "Akka library repository".at("https://repo.akka.io/maven"),
      libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.14",
      "org.scalatest" %% "scalatest" % "3.2.14" % Test,
        "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
        "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
        "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
        "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
        "io.spray" %%  "spray-json" % "1.3.6"
    ),
  )

lazy val realChess = (project in file("RealChess"))
  .dependsOn(sharedResources)
  .settings(
    name := "RealChess",
    resolvers += "Akka library repository".at("https://repo.akka.io/maven"),
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.14",
      "org.scalatest" %% "scalatest" % "3.2.14" % Test,
      "com.lihaoyi" %% "requests" % "0.9.0",
      "com.lihaoyi" %% "upickle" % "4.0.2",
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
      "io.spray" %%  "spray-json" % "1.3.6"
    ),
  )

lazy val controller = (project in file("Controller"))
  .dependsOn(devourChess, realChess, sharedResources)
  .settings(
    name := "Controller",
    resolvers += "Akka library repository".at("https://repo.akka.io/maven"),
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.14",
      "org.scalatest" %% "scalatest" % "3.2.14" % Test,
      "net.codingwell" %% "scala-guice" % "7.0.0",
      "com.google.inject" % "guice" % "5.1.0",
      "org.scala-lang.modules" %% "scala-xml" % "2.3.0",
      "org.playframework" %% "play-json" % "3.0.4",
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
      "io.spray" %%  "spray-json" % "1.3.6"
    ),
  )

lazy val tui = (project in file("TUI"))
  .dependsOn(controller)
  .settings(
    name := "TUI",
    resolvers += "Akka library repository".at("https://repo.akka.io/maven"),
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.14",
      "org.scalatest" %% "scalatest" % "3.2.14" % Test,
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
      "io.spray" %%  "spray-json" % "1.3.6",
    ),
  )

lazy val gui = (project in file("GUI"))
  .dependsOn(controller)
  .settings(
    name := "GUI",
    resolvers += "Akka library repository".at("https://repo.akka.io/maven"),
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.14",
      "org.scalatest" %% "scalatest" % "3.2.14" % Test,
      "org.scalafx" %% "scalafx" % "23.0.1-R34",
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
      "io.spray" %%  "spray-json" % "1.3.6"
    ),
  )

lazy val xml = (project in file("XML"))
  .dependsOn(sharedResources)
  .settings(
    name := "XML",
    resolvers += "Akka library repository".at("https://repo.akka.io/maven"),
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.14",
      "org.scalatest" %% "scalatest" % "3.2.14" % Test,
      "org.scala-lang.modules" %% "scala-xml" % "2.3.0",
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
      "io.spray" %%  "spray-json" % "1.3.6"
    ),
  )

lazy val json = (project in file("JSON"))
  .dependsOn(sharedResources)
  .settings(
    name := "JSON",
    resolvers += "Akka library repository".at("https://repo.akka.io/maven"),
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.14",
      "org.scalatest" %% "scalatest" % "3.2.14" % Test,
      "org.playframework" %% "play-json" % "3.0.4",
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
      "io.spray" %%  "spray-json" % "1.3.6"
    ),
  )

lazy val root = (project in file("."))
  .aggregate(tui, gui, controller, devourChess, basicChess, realChess, sharedResources, xml, json)
  .dependsOn(tui, gui, controller, devourChess, basicChess, realChess, sharedResources, xml, json)
  .settings(
    name := "JP_Morgan_Chess",
    resolvers += "Akka library repository".at("https://repo.akka.io/maven"),
    // Dependencies
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.14",
      "org.scalatest" %% "scalatest" % "3.2.14" % Test,
      "net.codingwell" %% "scala-guice" % "7.0.0",
      "com.google.inject" % "guice" % "5.1.0",
      "com.lihaoyi" %% "requests" % "0.9.0",
      "com.lihaoyi" %% "upickle" % "4.0.2",
      "org.scala-lang.modules" %% "scala-xml" % "2.3.0",
      "org.playframework" %% "play-json" % "3.0.4",
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
        "io.spray" %%  "spray-json" % "1.3.6"
    ),
    coverageExcludedPackages := "<empty>;.*aView.*",
  )

Compile / run / mainClass := Some("Chess")
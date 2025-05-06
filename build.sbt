// Build and Scala version
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.6.1"
ThisBuild / assembly / assemblyJarName := { name.value + ".jar" }

val AkkaVersion = "2.10.0"
val AkkaHttpVersion = "10.7.0"

// at the very top of build.sbt
import sbtassembly.AssemblyPlugin.autoImport._
import sbtassembly.MergeStrategy
import sbtassembly.PathList

ThisBuild / assembly / assemblyMergeStrategy := {
  // drop all module-info under META-INF/versions/…
  case PathList("META-INF", "versions", _*)                              => MergeStrategy.discard
  // drop any standalone module-info
  case "module-info.class"                                              => MergeStrategy.discard

  // drop all JavaFX substrate json resources
  case PathList("META-INF", "substrate", _ @ _*)                        => MergeStrategy.discard

  case PathList("META-INF", "MANIFEST.MF")                              => MergeStrategy.discard


  case "reference.conf"                                                 => MergeStrategy.concat

  // drop all .tasty files
  case PathList(ps @ _*) if ps.last.endsWith(".tasty")                  => MergeStrategy.discard

  // for any other META-INF conflicts, keep the first
  case PathList("META-INF", xs @ _*)                                    => MergeStrategy.first

  // fallback: keep first
  case _                                                                 => MergeStrategy.first
}

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
      "io.spray" %%  "spray-json" % "1.3.6",
      "ch.qos.logback"    %  "logback-classic" % "1.5.11",
    ),
  )
  .enablePlugins(AssemblyPlugin)

lazy val basicChess = (project in file("BasicChess"))
  .dependsOn(sharedResources)
  .settings(
    name := "BasicChess",
    assembly / mainClass := Some("BasicChess.BasicChessServer"),
    resolvers += "Akka library repository".at("https://repo.akka.io/maven"),
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.14",
      "org.scalatest" %% "scalatest" % "3.2.14" % Test,
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
      "io.spray" %%  "spray-json" % "1.3.6",
      "ch.qos.logback"    %  "logback-classic" % "1.5.11",
    ),
  )
  .enablePlugins(AssemblyPlugin)

lazy val devourChess = (project in file("DevourChess"))
  .dependsOn(sharedResources)
  .settings(
    name := "DevourChess",
    assembly / mainClass := Some("DevourChess.ChessServer"),
    resolvers += "Akka library repository".at("https://repo.akka.io/maven"),
      libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.14",
      "org.scalatest" %% "scalatest" % "3.2.14" % Test,
        "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
        "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
        "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
        "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
        "io.spray" %%  "spray-json" % "1.3.6",
        "ch.qos.logback"    %  "logback-classic" % "1.5.11",
    ),
  )
  .enablePlugins(AssemblyPlugin)

lazy val realChess = (project in file("RealChess"))
  .dependsOn(sharedResources)
  .settings(
    name := "RealChess",
    assembly / mainClass := Some("RealChess.ChessServer"),
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
      "io.spray" %%  "spray-json" % "1.3.6",
      "ch.qos.logback"    %  "logback-classic" % "1.5.11",
    ),
  )
  .enablePlugins(AssemblyPlugin)

lazy val controller = (project in file("Controller"))
  .dependsOn(sharedResources)
  .settings(
    name := "Controller",
    assembly / mainClass := Some("Controller.ControllerServer"),
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
      "io.spray" %%  "spray-json" % "1.3.6",
      "ch.qos.logback"    %  "logback-classic" % "1.5.11",
    ),
  )
  .enablePlugins(AssemblyPlugin)

lazy val tui = (project in file("TUI"))
  .dependsOn(sharedResources)
  .settings(
    name := "TUI",
    assembly / mainClass := Some("TUI.TuiServer"),
    resolvers += "Akka library repository".at("https://repo.akka.io/maven"),
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.14",
      "org.scalatest" %% "scalatest" % "3.2.14" % Test,
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
      "io.spray" %%  "spray-json" % "1.3.6",
      "ch.qos.logback"    %  "logback-classic" % "1.5.11",
    ),
  )
  .enablePlugins(AssemblyPlugin)

lazy val gui = (project in file("GUI"))
  .dependsOn(sharedResources)
  .settings(
    name := "GUI",
    assembly / mainClass := Some("GUI.GuiServer"),
    resolvers += "Akka library repository".at("https://repo.akka.io/maven"),
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.14",
      "org.scalatest" %% "scalatest" % "3.2.14" % Test,
      "org.scalafx" %% "scalafx" % "23.0.1-R34",
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
      "io.spray" %%  "spray-json" % "1.3.6",
      "ch.qos.logback"    %  "logback-classic" % "1.5.11",
    ),
  )
  .enablePlugins(AssemblyPlugin)

lazy val xml = (project in file("XML"))
  .dependsOn(sharedResources)
  .settings(
    name := "XML",
    assembly / mainClass := Some("XML.ApiFileServer"),
    resolvers += "Akka library repository".at("https://repo.akka.io/maven"),
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.14",
      "org.scalatest" %% "scalatest" % "3.2.14" % Test,
      "org.scala-lang.modules" %% "scala-xml" % "2.3.0",
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
      "io.spray" %%  "spray-json" % "1.3.6",
      "ch.qos.logback"    %  "logback-classic" % "1.5.11",
    ),
  )
  .enablePlugins(AssemblyPlugin)

lazy val json = (project in file("JSON"))
  .dependsOn(sharedResources)
  .settings(
    name := "JSON",
    assembly / mainClass := Some("JSON.ApiFileServer"),
    resolvers += "Akka library repository".at("https://repo.akka.io/maven"),
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.14",
      "org.scalatest" %% "scalatest" % "3.2.14" % Test,
      "org.playframework" %% "play-json" % "3.0.4",
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
      "io.spray" %%  "spray-json" % "1.3.6",
      "ch.qos.logback"    %  "logback-classic" % "1.5.11",

    ),
  )
  .enablePlugins(AssemblyPlugin)

lazy val api = (project in file("API"))
  .dependsOn(sharedResources)
  .settings(
    name := "API",
    assembly / mainClass := Some("API.ApiServer"),
    resolvers += "Akka library repository".at("https://repo.akka.io/maven"),
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.14",
      "org.scalatest" %% "scalatest" % "3.2.14" % Test,
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
      "io.spray" %%  "spray-json" % "1.3.6",
      "ch.qos.logback"    %  "logback-classic" % "1.5.11",
    ),
  )
  .enablePlugins(AssemblyPlugin)

lazy val root = (project in file("."))
  .aggregate(tui, gui, controller, devourChess, basicChess, realChess, sharedResources, xml, json, api)
  .dependsOn(tui, gui, controller, devourChess, basicChess, realChess, sharedResources, xml, json, api)
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
        "io.spray" %%  "spray-json" % "1.3.6",
      "ch.qos.logback"    %  "logback-classic" % "1.5.11",
    ),
    coverageExcludedPackages := "<empty>;.*aView.*",
  )
  .enablePlugins(AssemblyPlugin)

Compile / run / mainClass := Some("Chess")
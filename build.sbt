// Build and Scala version
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.6.1"

lazy val sharedResources = (project in file("SharedResources"))
  .settings(
    name := "SharedResources",
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.14",
      "org.scalatest" %% "scalatest" % "3.2.14" % Test,
    ),
  )

lazy val basicChess = (project in file("BasicChess"))
  .dependsOn(sharedResources)
  .settings(
    name := "BasicChess",
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.14",
      "org.scalatest" %% "scalatest" % "3.2.14" % Test,
    ),
  )

lazy val devourChess = (project in file("DevourChess"))
  .dependsOn(basicChess, sharedResources)
  .settings(
    name := "DevourChess",
      libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.14",
      "org.scalatest" %% "scalatest" % "3.2.14" % Test,
    ),
  )

lazy val realChess = (project in file("RealChess"))
  .dependsOn(sharedResources, basicChess)
  .settings(
    name := "RealChess",
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.14",
      "org.scalatest" %% "scalatest" % "3.2.14" % Test,
      "com.lihaoyi" %% "requests" % "0.9.0",
      "com.lihaoyi" %% "upickle" % "4.0.2",
    ),
  )

lazy val controller = (project in file("Controller"))
  .dependsOn(devourChess, realChess, sharedResources, basicChess)
  .settings(
    name := "Controller",
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.14",
      "org.scalatest" %% "scalatest" % "3.2.14" % Test,
      "net.codingwell" %% "scala-guice" % "7.0.0",
      "com.google.inject" % "guice" % "5.1.0",
      "org.scala-lang.modules" %% "scala-xml" % "2.3.0",
      "org.playframework" %% "play-json" % "3.0.4"
    ),
  )

lazy val tui = (project in file("TUI"))
  .dependsOn(controller)
  .settings(
    name := "TUI",
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.14",
      "org.scalatest" %% "scalatest" % "3.2.14" % Test,
    ),
  )

lazy val gui = (project in file("GUI"))
  .dependsOn(controller)
  .settings(
    name := "GUI",
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.14",
      "org.scalatest" %% "scalatest" % "3.2.14" % Test,
      "org.scalafx" %% "scalafx" % "23.0.1-R34",
    ),
  )

lazy val root = (project in file("."))
  .aggregate(tui, gui, controller, devourChess, basicChess, realChess, sharedResources)
  .dependsOn(tui, gui, controller, devourChess, basicChess, realChess, sharedResources)
  .settings(
    name := "JP_Morgan_Chess",

    // Dependencies
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.14",
      "org.scalatest" %% "scalatest" % "3.2.14" % Test,
      "net.codingwell" %% "scala-guice" % "7.0.0",
      "com.google.inject" % "guice" % "5.1.0",
      "com.lihaoyi" %% "requests" % "0.9.0",
      "com.lihaoyi" %% "upickle" % "4.0.2",
      "org.scala-lang.modules" %% "scala-xml" % "2.3.0",
      "org.playframework" %% "play-json" % "3.0.4"
    ),
    coverageExcludedPackages := "<empty>;.*aView.*",
  )

Compile / run / mainClass := Some("Chess")
val scala3Version = "3.1.2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "discord-qotd-mod-bot",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test,
    libraryDependencies ++= Seq("org.javacord" % "javacord" % "3.4.0"),
    scalacOptions ++= Seq(
      "-source", "future",
      "-deprecation",
      //"-Yexplicit-nulls"
    )


  )

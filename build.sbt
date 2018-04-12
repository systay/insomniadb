import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.5",
      version := "0.1.0-SNAPSHOT"
    )),
    name := "Hello",
    libraryDependencies ++= Seq(
      scalaTest % Test,
      "org.neo4j" % "openCypher-frontend-1" % "3.4.0-beta01",
      "org.agrona" % "Agrona" % "0.9.2-SNAPSHOT"
    ),
    resolvers += "Local Maven Repository" at "file:///" + Path.userHome + "/.m2/repository"
  )

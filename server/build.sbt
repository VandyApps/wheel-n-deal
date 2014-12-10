
name := "Game Server"

version := "1.0"

scalaVersion := "2.10.3"

scalacOptions := Seq(
  "-unchecked",
  "-deprecation",
  "-encoding", "utf8",
  "-feature",
  "-language:implicitConversions")

resolvers += "Twitter" at "http://maven.twttr.com"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.twitter" %% "finatra" % "1.5.2",
  "com.google.code.gson" %  "gson" % "2.2.4",
  "com.typesafe.akka" %% "akka-actor" % "2.3.7"
)

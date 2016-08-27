import sbt.{Build, Project, ProjectRef, uri}

name := "akka-transbank"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.11" % "2.4.4"

libraryDependencies += "com.typesafe.akka" % "akka-http-core_2.11" % "2.4.4"

libraryDependencies += "com.typesafe.akka" % "akka-http-experimental_2.11" % "2.4.4"

libraryDependencies += "com.lihaoyi" %% "scalatags" % "0.5.4"

libraryDependencies += "org.postgresql" % "postgresql" % "9.4.1208.jre7"

libraryDependencies += "com.typesafe.slick" %% "slick" % "3.1.1"

//libraryDependencies += "com.typesafe.slick" %% "slick-hikaricp" % "3.1.1"

libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.6.4"

libraryDependencies += "com.typesafe.akka" % "akka-testkit_2.11" % "2.4.4"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"

libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.4.4" % "test"

libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % "2.4.4" % "test"



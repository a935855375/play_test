name := "simple chatroom"

version := "2.6.0-SNAPSHOT"

scalaVersion := "2.12.3"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies += guice
libraryDependencies += jdbc
libraryDependencies += ws
libraryDependencies += ehcache
libraryDependencies += jcache
libraryDependencies += "org.jsr107.ri" % "cache-annotations-ri-guice" % "1.0.0"

libraryDependencies += "org.webjars" % "flot" % "0.8.3"
libraryDependencies += "org.webjars" % "bootstrap" % "3.3.6"
libraryDependencies += "org.webjars" % "jquery" % "1.11.1"

libraryDependencies += "com.h2database" % "h2" % "1.4.194"

libraryDependencies += "com.typesafe.play" %% "anorm" % "2.5.3"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.0" % Test

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.41"

libraryDependencies += "com.typesafe.play" %% "play-mailer" % "6.0.1"
libraryDependencies += "com.typesafe.play" %% "play-mailer-guice" % "6.0.1"
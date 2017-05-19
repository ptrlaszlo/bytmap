
name := "bytmap"
scalaVersion := "2.12.2"

val akkaVersion = "2.4.17"
val elastic4sVersion = "5.4.2"
val specs2Version = "3.8.9"

libraryDependencies ++= Seq(
  "com.sksamuel.elastic4s" %% "elastic4s-http-streams" % elastic4sVersion,
  "com.sksamuel.elastic4s" %% "elastic4s-http" % elastic4sVersion,
	"com.typesafe" % "config" % "1.3.1",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
	"com.typesafe.akka" %% "akka-stream" % akkaVersion,
	"com.typesafe.play" %% "play-json" % "2.6.0-M7",
	"org.scalaj" %% "scalaj-http" % "2.3.0",
	"org.jsoup" % "jsoup" % "1.10.2",

	"com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
	"com.sksamuel.elastic4s" %% "elastic4s-testkit" % elastic4sVersion % Test,
  "org.specs2" %% "specs2-core" % specs2Version % Test,
  "org.specs2" %% "specs2-mock" % specs2Version % Test
)

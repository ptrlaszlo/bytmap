
name := "bytmap"
scalaVersion := "2.12.2"

val elastic4sVersion = "5.4.2"

libraryDependencies ++= Seq(
  "com.sksamuel.elastic4s" %% "elastic4s-http-streams" % elastic4sVersion,
  "com.sksamuel.elastic4s" %% "elastic4s-http" % elastic4sVersion,
	"com.typesafe.akka" %% "akka-stream" % "2.4.17",
	"com.typesafe.play" %% "play-json" % "2.6.0-M7",
	"org.scalaj" %% "scalaj-http" % "2.3.0",
	"org.jsoup" % "jsoup" % "1.10.2",
	"com.typesafe" % "config" % "1.3.1"
)
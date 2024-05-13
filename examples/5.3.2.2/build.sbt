name := "intrusion-detection"
version := "1.0"

scalaVersion := "2.12.18"
val sparkVersion = "3.4.0"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-mllib" % sparkVersion,
  "org.apache.logging.log4j" %% "log4j-api-scala" % "12.0",
  "org.apache.logging.log4j" % "log4j-core" % "2.13.0",
)

assemblyJarName := "intrusion-detection_2.12-1.0.jar"
assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}



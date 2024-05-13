name := "spark-examples"
version := "1.0"

scalaVersion := "2.12.18"
val sparkVersion = "3.4.0"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
)

assemblyJarName := "spark-examples_2.12-1.0.jar"
assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
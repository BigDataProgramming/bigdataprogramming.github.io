name := "trajectory-spark"
version := "1.0"

scalaVersion := "2.12.18"
val sparkVersion = "3.4.0"

//mainClass in (Compile, run) := Some("it.unical.dimes.scalab.trajectory.TrajectoryMining")

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-mllib" % sparkVersion,
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.4.0",
  "de.micromata.jak" % "JavaAPIforKml" % "2.2.1",
  "com.spatial4j" % "spatial4j" % "0.5",
  "com.vividsolutions" % "jts" % "1.13",
  "joda-time" % "joda-time" % "2.7"
)

assemblyJarName := "trajectory-spark_2.12-1.0.jar"
assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
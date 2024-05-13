package it.unical.dimes.scalab.trajectory

import org.apache.spark.mllib.fpm.FPGrowth.FreqItemset
import org.apache.spark.mllib.fpm.{FPGrowth, PrefixSpan}
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import java.time.format.DateTimeFormatter
import java.util.Locale
import scala.collection.JavaConverters._
import scala.collection.mutable
import it.unical.dimes.scalab.utils._
import org.apache.spark.mllib.fpm.AssociationRules.Rule

object TrajectoryExtraction {
  val datetime_format: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm:ss a").withLocale(Locale.ENGLISH)
  val romeShape = GeoUtils.getCircle(GeoUtils.getPoint(12.492373, 41.890251), 10000)

  class SingleTrajectory(var poi: String = "", var daytime: String = "") {
    override def hashCode(): Int =
      31 * (
        poi.##
        ) + daytime.##

    override def equals(o: Any): Boolean = {
      if (o == null) return false
      if (getClass ne o.getClass) return false
      val other = o.asInstanceOf[SingleTrajectory]
      if (poi.equals(other.poi) && daytime.equals(other.daytime))
        return true
      false
    }

    override def toString: String = {
      "Single Trajectory for poi %s, timestamp %s".format(poi, daytime)
    }
  }

  class UserTrajectory(var username: String = "", var daytime: String = "") {

    override def hashCode(): Int =
      31 * (
        username.##
        ) + daytime.##

    override def equals(o: Any): Boolean = {
      if (o == null) return false
      if (getClass ne o.getClass) return false
      val other = o.asInstanceOf[UserTrajectory]
      if (username.equals(other.username) && daytime.equals(other.daytime))
        return true
      false
    }

    override def toString: String = {
      "Trajectory %s, day %s".format(username, daytime)
    }
  }

  def mapCreateTrajectory(x: Row, shapeMap: mutable.Map[String, String]): (UserTrajectory, Set[SingleTrajectory]) = {
    val lat = x.getDouble(0)
    val lon = x.getDouble(1)
    val username = x.getString(3)
    val d = x.getAs[String](4).substring(0, 12)
    val point = GeoUtils.getPoint(lon, lat)
    var arr: Array[SingleTrajectory] = Array[SingleTrajectory]()
    for ((place, polygon) <- shapeMap) {
      val pol = GeoUtils.getPolygonFromString(polygon)
      if (GeoUtils.isContained(point, pol)) {
        val trajectory = new SingleTrajectory(place, d)
        arr = arr :+ trajectory
      }
    }
    val ut = new UserTrajectory(username, d)
    (ut, arr.toSet)
  }

  def getSequences(x: Set[SingleTrajectory], mapPoiToInteger: mutable.Map[String, Int]): Array[Array[Int]] = {
    var arr: Array[Array[Int]] = Array[Array[Int]]()
    x.foreach(x => {
      arr = arr :+ Array[Int](mapPoiToInteger(x.poi))
    })
    arr
  }

  def generateFreqItemset(x: PrefixSpan.FreqSequence[Int], mapIntegerToPoi: mutable.Map[Int, String]): FreqItemset[String] = {
    val frequency = x.freq
    var arr: Array[String] = Array[String]()
    x.sequence.foreach(sequence => {
      sequence.foreach(singleSequence => {
        arr = arr :+ mapIntegerToPoi(singleSequence)
      })
    })
    new FreqItemset(arr, frequency)
  }

  def computeTrajectoryUsingFPGrowth(df: DataFrame, stringShapeMap: mutable.Map[String, String], minSupport: Double = 0.01, minConfidence: Double = 0.2) = {
    val prepareTransaction = df
      .rdd
      .map(x => mapCreateTrajectory(x, stringShapeMap))
      .filter(x => x._2.nonEmpty)
      .reduceByKey((x, y) => x ++ y)

    val transactions = prepareTransaction
      .map(x => getTransaction(x._2)).map(x => x.distinct)
    println(transactions.count())

    val fpg = new FPGrowth()
      .setMinSupport(minSupport)

    val model = fpg.run(transactions)

    (model.freqItemsets.collect(), model.generateAssociationRules(minConfidence).collect())
  }

  def getTransaction(_2: Set[SingleTrajectory]): Array[String] = {
    var arr: Array[String] = Array[String]()
    _2.foreach(x => {
      arr = arr :+ x.poi
    })
    arr
  }

  def filterFlickrDataframe(dataframe: DataFrame): DataFrame = {
    dataframe.select("geoData.latitude", "geoData.longitude", "geoData.accuracy", "owner.id", "dateTaken")
  }

  def filterIsGPSValid(r: Row): Boolean = {
    r.getAs[Double]("longitude") > 0 && r.getAs[Double]("latitude") > 0
  }

  def filterIsInRome(r: Row): Boolean = {
    val p = GeoUtils.getPoint(r.getAs[Double]("longitude"), r.getAs[Double]("latitude"))
    GeoUtils.isContained(p, romeShape)
  }

  def main(args: Array[String]): Unit = {
    val dataset = args(0)
    val kmlPath = args(1)
    val spark = SparkSession
      .builder.master("spark://master:7077")
      .appName("Trajectory Mining")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()
    val stringShapeMap = KMLUtils.lookupFromKml(kmlPath).asScala
    var df = spark.read.json(dataset)
    df = filterFlickrDataframe(df)
    df = df.filter(r => filterIsGPSValid(r) && filterIsInRome(r))
    val trajectories = computeTrajectoryUsingFPGrowth(df, stringShapeMap)
    val rulesRDD = spark.sparkContext.parallelize(trajectories._2)
    val rulesStringRDD = rulesRDD.map(ruleToString)
    rulesStringRDD.coalesce(1).saveAsTextFile(args(2))
    spark.close()
  }

  // Define a function to convert each rule to a string
  def ruleToString(rule: Rule[String]): String = {
    s"${rule.antecedent.mkString(",")} => ${rule.consequent.mkString(",")}, confidence: ${rule.confidence}"
  }

}

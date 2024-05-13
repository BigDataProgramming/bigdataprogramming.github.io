package it.unical.dimes.scalab.spark

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

object MarketBasketAnalysis {
  def main(args: Array[String]) {

    val inputFile = args(0)
    val spark = SparkSession
      .builder.master("spark://master:7077")
      .appName("MarketBasketAnalysis")
      .getOrCreate()

    val sc: SparkContext = spark.sparkContext
    val textFile = sc.textFile(inputFile)
    val items: RDD[Array[String]] = textFile.map(line => line.split(','))
    val pairs: RDD[(String, String)] = items.filter(items => items
      .length >= 2).flatMap(items => getAllPairs(items))
    val pairsOcc: RDD[(String, Int)] = pairs.map(pair => (pair._1
      + "-" + pair._2, 1)).reduceByKey((i, j) => i + j)
    val ordPairsOcc: RDD[(String, Int)] = pairsOcc.sortBy(x => x.
      _2, ascending = false)
    ordPairsOcc.collect().foreach(x => println(x._1 + " " + x._2))
  }

  def getAllPairs(items: Array[String]): Array[(String, String)] = {
    for (i1 <- items; i2 <- items; if (i1.compareTo(i2) < 0))
      yield (i1, i2)
  }
}
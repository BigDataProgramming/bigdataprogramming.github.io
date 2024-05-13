package it.unical.dimes.scalab

import org.apache.spark.sql.SparkSession
import org.apache.spark._
import org.apache.spark.graphx.{Graph, _}
import org.apache.spark.rdd.RDD
import scala.math.log

object TextRank {

  // define sentence similarity score
  def sentenceSimilarity(sentence1: String, sentence2: String): Double = {
    val words1 = sentence1.split("[\\s,.;:?!]+").map(_.toLowerCase).toSet
    val words2 = sentence2.split("[\\s,.;:?!]+").map(_.toLowerCase).toSet
    val commonWords = words1.intersect(words2).size
    commonWords.toDouble / (log(words1.size + 1) + log(words2.size + 1) + 1)
  }

  def main(args: Array[String]) = {

    //Create Spark session
    val spark = SparkSession
      .builder.master("spark://master:7077")
      .appName("Spark-GraphX-TextRank")
      .getOrCreate()
    val sc: SparkContext = spark.sparkContext

    // build sentence graph
    val path = args(0)
    val input_sentences = sc.textFile(path).flatMap(line => line.split('.'))
    val vertices = input_sentences.zipWithIndex.map {
      case (sentence, index) => (index, sentence)
    }
    val pairs = vertices.cartesian(vertices)
      .filter { case ((i1, _), (i2, _)) => i1 != i2 }
      .map { case ((i1, s1), (i2, s2)) => (i1, i2, sentenceSimilarity(s1, s2)) }
      .map { case (i1, i2, sim) => Edge(i1, i2, sim) }
    val graph = Graph(vertices, pairs)

    // normalize weighted graph
    val outgoingEdgesSum = graph.aggregateMessages[Double](
      ctx => ctx.sendToSrc(ctx.attr),
      (a, b) => a + b,
      TripletFields.EdgeOnly
    ).collect.toMap
    val normEdges = graph.edges.map(e => {
      val srcSum = outgoingEdgesSum.getOrElse(e.srcId, 1.0)
      val newWeight = e.attr / srcSum
      Edge(e.srcId, e.dstId, newWeight)
    })
    val initialGuess = 1.0
    val preparedGraph = Graph(vertices, normEdges)
      .mapVertices((_, attr) => initialGuess)

    // Define Pregel's UDFs for PageRank
    val d = 0.85
    val numVertices = preparedGraph.numVertices

    def vertexProgram(id: VertexId, attr: Double, msgSum: Double): Double =
      (1 - d) / numVertices + d * msgSum

    def sendMessage(edge: EdgeTriplet[Double, Double]): Iterator[(VertexId, Double)] =
      Iterator((edge.dstId, edge.srcAttr * edge.attr))

    def messageCombiner(a: Double, b: Double): Double = a + b

    // Execute Pregel for a fixed number of iterations.
    val rankGraph = Pregel(graph = preparedGraph, initialMsg = 0.0, maxIterations = 50)(
      vprog = vertexProgram, sendMsg = sendMessage, mergeMsg = messageCombiner)

    /* extract summary:
    the summary is composed of the top 3 sentences per PageRank
    ordered according to how they appear in the original text.
     */
    val summary = graph.vertices.join(rankGraph.vertices)
      .map { case (id, (sentence, rank)) => (id, sentence, rank) }
      .top(3)(Ordering.by(_._3)).sortBy(_._1)
      .map { case (_, sentence, _) => sentence }
      .mkString(".\n") + "."
    println("SUMMARY:\n" + summary)
  }
}
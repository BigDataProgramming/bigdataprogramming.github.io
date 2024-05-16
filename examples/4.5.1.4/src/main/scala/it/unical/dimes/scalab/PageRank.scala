package it.unical.dimes.scalab

import org.apache.spark.sql.SparkSession
import org.apache.spark._
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD

object PageRank {

  def main(args: Array[String]) = {

    //Create Spark session
    val spark = SparkSession
      .builder.master("spark://master:7077")
      .appName("Spark-GraphX-PageRank")
      .getOrCreate()
    val sc: SparkContext = spark.sparkContext

    // Build an example graph
    val vertices: RDD[(VertexId, Double)] = sc.parallelize(Seq(1, 11).map(x => (x.asInstanceOf[Long], x.asInstanceOf[Double])))
    val edges: RDD[Edge[PartitionID]] = sc.parallelize(Seq(Edge(2L, 3L, 1), Edge(3L, 2L, 1), Edge(4L, 2L, 1), Edge(4L, 1L, 1),
      Edge(5L, 4L, 1), Edge(5L, 6L, 1), Edge(5L, 2L, 1), Edge(6L, 5L, 1), Edge(6L, 2L, 1), Edge(7L, 2L, 1), Edge(7L, 5L, 1),
      Edge(8L, 2L, 1), Edge(8L, 5L, 1), Edge(9L, 2L, 1), Edge(9L, 5L, 1), Edge(10L, 5L, 1), Edge(11L, 5L, 1)))
    val graph: Graph[Double, PartitionID] = Graph(vertices, edges)
    val numVertices = graph.numVertices
    val startGraph: Graph[Double, Double] = graph
      // Associate the degree with each vertex
      .outerJoinVertices(graph.outDegrees) {
        (vid, vdata, deg) => deg.getOrElse(0)
      }
      // Set the weight on the edges based on the out-degree
      .mapTriplets(e => 1.0 / e.srcAttr)
      // Set the vertex attributes to an initial guess
      .mapVertices((id, attr) => 1)


    // Set damping factor
    val d = 0.85

    // Define Pregel's UDFs for PageRank
    def vertexProgram(id: VertexId, attr: Double, msgSum: Double): Double =
      (1-d)/numVertices + d * msgSum

    def sendMessage(edge: EdgeTriplet[Double, Double]): Iterator[(VertexId, Double)] =  Iterator((edge.dstId, edge.srcAttr * edge.attr))

    def messageCombiner(a: Double, b: Double): Double = a + b

    // Execute Pregel for a fixed number of iterations.
    val finalGraph = Pregel(graph = startGraph, initialMsg = 0.0, maxIterations = 50)(
      vprog = vertexProgram, sendMsg = sendMessage, mergeMsg = messageCombiner)
    // Normalize to sum 1 (it is required to deal with sink nodes)
    val rankSum = finalGraph.vertices.values.sum()
    val rankGraph = finalGraph.mapVertices((id, rank) => rank / rankSum)
    // Show top 5 nodes ordered by decreasing PageRank
    rankGraph.vertices.top(10)(Ordering.by(_._2)).foreach(println)
  }
}
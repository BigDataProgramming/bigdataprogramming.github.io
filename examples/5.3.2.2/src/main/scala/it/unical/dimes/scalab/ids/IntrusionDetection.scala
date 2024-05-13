package it.unical.dimes.scalab.ids

import org.apache.spark.SparkConf
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.ml.classification.{RandomForestClassifier, RandomForestClassificationModel}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.{StandardScaler, StringIndexer, VectorAssembler}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.{DoubleType, IntegerType, StringType, StructType}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object IntrusionDetection {
  def main(args: Array[String]): Unit = {
    if (args(0) == "offline-training") {
      val df_path = args(1)
      val output_path = args(2)
      // Start a Spark session
      val spark = SparkSession
        .builder.master("spark://master:7077")
        .appName("OfflineTraining")
        .getOrCreate()

      val connectionSchema = new StructType()
        .add("duration", DoubleType)
        .add("protocol_type", IntegerType)
        .add("service", IntegerType)
        .add("flag", IntegerType)
        .add("src_bytes", DoubleType)
        .add("dst_bytes", DoubleType)
        .add("land", IntegerType)
        .add("wrong_fragment", DoubleType)
        .add("urgent", DoubleType)
        .add("hot", DoubleType)
        .add("num_failed_logins", DoubleType)
        .add("logged_in", IntegerType)
        .add("num_compromised", DoubleType)
        .add("root_shell", IntegerType)
        .add("su_attempted", IntegerType)
        .add("num_root", DoubleType)
        .add("num_file_creations", DoubleType)
        .add("num_shells", DoubleType)
        .add("num_access_files", DoubleType)
        .add("num_outbound_cmds", DoubleType)
        .add("is_host_login", IntegerType)
        .add("is_guest_login", IntegerType)
        .add("count", DoubleType)
        .add("srv_count", DoubleType)
        .add("serror_rate", DoubleType)
        .add("srv_serror_rate", DoubleType)
        .add("rerror_rate", DoubleType)
        .add("srv_rerror_rate", DoubleType)
        .add("same_srv_rate", DoubleType)
        .add("diff_srv_rate", DoubleType)
        .add("srv_diff_host_rate", DoubleType)
        .add("dst_host_count", DoubleType)
        .add("dst_host_srv_count", DoubleType)
        .add("dst_host_same_srv_rate", DoubleType)
        .add("dst_host_diff_srv_rate", DoubleType)
        .add("dst_host_same_src_port_rate", DoubleType)
        .add("dst_host_srv_diff_host_rate", DoubleType)
        .add("dst_host_serror_rate", DoubleType)
        .add("dst_host_srv_serror_rate", DoubleType)
        .add("dst_host_rerror_rate", DoubleType)
        .add("dst_host_srv_rerror_rate", DoubleType)
        .add("attack_type", StringType)

      // Load the KDD'99 dataset
      val df = spark.read.format("csv").option("header", "false").schema(connectionSchema).load(df_path)
      val Array(trainingData, testData) = df.randomSplit(Array(0.7, 0.3))

      val assembler = new VectorAssembler()
        .setInputCols(Array("duration", "protocol_type", "service", "flag", "src_bytes", "dst_bytes", "land", "wrong_fragment", "urgent", "hot", "num_failed_logins", "logged_in", "num_compromised", "root_shell", "su_attempted", "num_root", "num_file_creations", "num_shells", "num_access_files", "num_outbound_cmds", "is_host_login", "is_guest_login", "count", "srv_count", "serror_rate", "srv_serror_rate", "rerror_rate", "srv_rerror_rate", "same_srv_rate", "diff_srv_rate", "srv_diff_host_rate", "dst_host_count", "dst_host_srv_count", "dst_host_same_srv_rate", "dst_host_diff_srv_rate", "dst_host_same_src_port_rate", "dst_host_srv_diff_host_rate", "dst_host_serror_rate", "dst_host_srv_serror_rate", "dst_host_rerror_rate", "dst_host_srv_rerror_rate"))
        .setOutputCol("features")

      val indexer = new StringIndexer()
        .setInputCol("attack_type")
        .setOutputCol("indexed_label").setHandleInvalid("keep")

      val scaler = new StandardScaler()
        .setInputCol("features")
        .setOutputCol("scaledFeatures")
        .setWithStd(true)
        .setWithMean(false)

      val rf = new RandomForestClassifier().setFeaturesCol("scaledFeatures").setLabelCol("indexed_label")

      val pipeline = new Pipeline().setStages(Array(assembler, indexer, scaler, rf))

      // Train the pipeline on the training data
      val pipelineModel = pipeline.fit(trainingData)
      // Save the pipeline to the disk for reuse
      pipelineModel.write.overwrite().save(output_path + "pipeline_model")
      pipelineModel.stages(3).asInstanceOf[RandomForestClassificationModel].write.overwrite().save(output_path + "rf_model")

      val predictions = pipelineModel.transform(testData)
      predictions.drop("features", "scaledFeatures", "rawPrediction", "probability").show(10)

      val evaluator = new MulticlassClassificationEvaluator()
        .setLabelCol("indexed_label")
        .setPredictionCol("prediction")
        .setMetricName("accuracy")
      val accuracy = evaluator.evaluate(predictions)
      println(s"Accuracy: ${accuracy}")
      spark.stop()
    }

    else if (args(0) == "real-time-ids"){
      val directory = args(1)
      val pipelinePath = args(2) + "pipeline_model"

      val conf = new SparkConf().setMaster("spark://master:7077").setAppName("RealtimeIntrusionDetection")

      // StreamingContext with a batch interval of 1 second
      val ssc = new StreamingContext(conf, Seconds(1))

      val data = ssc.textFileStream(directory)
      val dataInWindow = data.window(Seconds(30), Seconds(10)).map
      {line => val col = line.split(",")
        ConnectionTest(
          col(0).toDouble,
          col(1).toInt,
          col(2).toInt,
          col(3).toInt,
          col(4).toDouble,
          col(5).toDouble,
          col(6).toInt,
          col(7).toDouble,
          col(8).toDouble,
          col(9).toDouble,
          col(10).toDouble,
          col(11).toInt,
          col(12).toDouble,
          col(13).toInt,
          col(14).toInt,
          col(15).toDouble,
          col(16).toDouble,
          col(17).toDouble,
          col(18).toDouble,
          col(19).toDouble,
          col(20).toInt,
          col(21).toInt,
          col(22).toDouble,
          col(23).toDouble,
          col(24).toDouble,
          col(25).toDouble,
          col(26).toDouble,
          col(27).toDouble,
          col(28).toDouble,
          col(29).toDouble,
          col(30).toDouble,
          col(31).toDouble,
          col(32).toDouble,
          col(33).toDouble,
          col(34).toDouble,
          col(35).toDouble,
          col(36).toDouble,
          col(37).toDouble,
          col(38).toDouble,
          col(39).toDouble,
          col(40).toDouble,
          col(41)
        )}
      val pipelineLoaded = PipelineModel.load(pipelinePath)

      dataInWindow.foreachRDD { rdd =>
      {
        val spark = SparkSessionSingleton.getInstance(rdd.sparkContext.getConf)
        import spark.implicits._
        val predictions = pipelineLoaded.transform(rdd.toDF())
        // Drop useless columns for display purposes
        predictions.drop("features", "scaledFeatures", "rawPrediction", "probability").show(10)
      }
      }

      // Use a Thread to stop the Spark Streaming context after 30 seconds
      val thread = new Thread(new Runnable {
        def run(): Unit = {
          Thread.sleep(30000)
          ssc.stop()
        }
      })

      thread.start()
      ssc.start()
      ssc.awaitTermination()
    }
  }

  case class ConnectionTest(duration: Double,
                            protocol_type: Int,
                            service: Int,
                            flag: Int,
                            src_bytes: Double,
                            dst_bytes: Double,
                            land: Int,
                            wrong_fragment: Double,
                            urgent: Double,
                            hot: Double,
                            num_failed_logins: Double,
                            logged_in: Int,
                            num_compromised: Double,
                            root_shell: Int,
                            su_attempted: Int,
                            num_root: Double,
                            num_file_creations: Double,
                            num_shells: Double,
                            num_access_files: Double,
                            num_outbound_cmds: Double,
                            is_host_login: Int,
                            is_guest_login: Int,
                            count: Double,
                            srv_count: Double,
                            serror_rate: Double,
                            srv_serror_rate: Double,
                            rerror_rate: Double,
                            srv_rerror_rate: Double,
                            same_srv_rate: Double,
                            diff_srv_rate: Double,
                            srv_diff_host_rate: Double,
                            dst_host_count: Double,
                            dst_host_srv_count: Double,
                            dst_host_same_srv_rate: Double,
                            dst_host_diff_srv_rate: Double,
                            dst_host_same_src_port_rate: Double,
                            dst_host_srv_diff_host_rate: Double,
                            dst_host_serror_rate: Double,
                            dst_host_srv_serror_rate: Double,
                            dst_host_rerror_rate: Double,
                            dst_host_srv_rerror_rate: Double,
                            attack_type: String
                           )

  /** Lazily instantiated singleton instance of SparkSession */
  object SparkSessionSingleton {

    @transient private var instance: SparkSession = _

    def getInstance(sparkConf: SparkConf): SparkSession = {
      if (instance == null) {
        instance = SparkSession
          .builder()
          .config(sparkConf)
          .getOrCreate()
      }
      instance
    }
  }
}

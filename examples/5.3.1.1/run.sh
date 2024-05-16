#! /bin/bash
CODE_PATH=/user/custom/5.3.1.1
INPUT=data

sudo chown $(whoami):$(id -gn) -R .
hadoop fs -rm -R $CODE_PATH
hadoop fs -mkdir -p $CODE_PATH
hadoop fs -put -f data/FlickrRomeSample.json data/rome.kml $CODE_PATH

sbt assembly && \
hadoop fs -put -f target/scala-2.12/trajectory-spark_2.12-1.0.jar $CODE_PATH

spark-submit \
--class it.unical.dimes.scalab.trajectory.TrajectoryExtraction \
--deploy-mode client \
--master spark://master:7077 \
hdfs://master:8020$CODE_PATH/trajectory-spark_2.12-1.0.jar \
hdfs://master$CODE_PATH/FlickrRomeSample.json \
hdfs://master$CODE_PATH/rome.kml \
hdfs://master$CODE_PATH/spark-results

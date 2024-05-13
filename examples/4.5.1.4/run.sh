#! /bin/bash
CODE_PATH=/user/custom/4.5.1.4
JAR_FILE=target/scala-2.12/graphx-pagerank_2.12-1.0.jar

sudo chown $(whoami):$(id -gn) -R .

hadoop fs -mkdir -p $CODE_PATH
sbt package && \
hadoop fs -put -f $JAR_FILE $CODE_PATH

spark-submit \
--class it.unical.dimes.scalab.PageRank \
--deploy-mode client \
--master spark://master:7077 \
hdfs://master:8020$CODE_PATH/graphx-pagerank_2.12-1.0.jar
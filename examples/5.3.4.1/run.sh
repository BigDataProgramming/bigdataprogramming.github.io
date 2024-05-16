#! /bin/bash
CODE_PATH=/user/custom/5.3.4.1
JAR_FILE=target/scala-2.12/graphx-textrank_2.12-1.0.jar
INPUT=data
OUTPUT=$CODE_PATH/output

sudo chown $(whoami):$(id -gn) -R .

hadoop fs -mkdir -p $CODE_PATH
hadoop fs -put -f data/ANN.txt $CODE_PATH
sbt package && \
hadoop fs -put -f $JAR_FILE $CODE_PATH

spark-submit \
--class it.unical.dimes.scalab.TextRank \
--deploy-mode client \
--master spark://master:7077 \
hdfs://master:8020$CODE_PATH/graphx-textrank_2.12-1.0.jar hdfs://master$CODE_PATH/ANN.txt
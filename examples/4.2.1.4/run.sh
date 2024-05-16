#! /bin/bash
CODE_PATH=/user/custom/4.2.1.4
JAR_FILE=target/inverted-index-hadoop-1.0-jar-with-dependencies.jar
INPUT=data
OUTPUT=$CODE_PATH/hadoop-output

sudo chown $(whoami):$(id -gn) -R .
hadoop fs -test -d $CODE_PATH && hadoop fs -rm -R $CODE_PATH
hadoop fs -mkdir -p $CODE_PATH
hadoop fs -mkdir -p $CODE_PATH/$OUTPUT
hadoop fs -put -f $INPUT $CODE_PATH

mvn package && \
hadoop fs -put -f $JAR_FILE $CODE_PATH && \
hadoop jar $JAR_FILE it.unical.dimes.scalab.invertedIndex.hadoop.InvertedIndexJob $CODE_PATH/$INPUT $OUTPUT

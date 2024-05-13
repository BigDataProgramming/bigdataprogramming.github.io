#! /bin/bash
CODE_PATH=/user/custom/4.6.2.4

sudo chown $(whoami):$(id -gn) -R .
hadoop fs -rm -R $CODE_PATH
hadoop fs -mkdir -p $CODE_PATH
hadoop fs -put -f data $CODE_PATH

mvn package
hadoop fs -put -f target/PigUDF-1.0-jar-with-dependencies.jar $CODE_PATH

pig -f data/sentiment_analyzer.pig \
-param jarPath=hdfs://master$CODE_PATH/PigUDF-1.0-jar-with-dependencies.jar \
-param dataset=hdfs://master$CODE_PATH/data/reviews.csv \
-param dict=hdfs://master$CODE_PATH/data/dict.txt \
-param outputPath=hdfs://master$CODE_PATH/pig-results


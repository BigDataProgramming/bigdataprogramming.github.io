#! /bin/bash
CODE_PATH=/user/custom/4.3.1.4
INPUT=data

sudo chown $(whoami):$(id -gn) -R .
hadoop fs -mkdir -p $CODE_PATH
hadoop fs -put -f data $CODE_PATH
sbt assembly && \
hadoop fs -put -f target/scala-2.12/spark-examples_2.12-1.0.jar $CODE_PATH && \
while true; do
    read -p "What example do you want to run [1=DataFrameExample, 2=MarketBasketAnalysis, 3=Exit]? " ex
    case $ex in
        [1]* ) spark-submit \
               --class it.unical.dimes.scalab.spark.DataFrameExample \
               --deploy-mode client \
               --master spark://master:7077 \
               hdfs://master:8020$CODE_PATH/spark-examples_2.12-1.0.jar \
               hdfs://master$CODE_PATH/data/ITCompany_employees.json \
               hdfs://master$CODE_PATH/data/ITCompany_projects.json \
               $CODE_PATH/data/transactions.txt;;
        [2]* ) spark-submit \
               --class it.unical.dimes.scalab.spark.MarketBasketAnalysis \
               --deploy-mode client \
               --master spark://master:7077 \
               hdfs://master:8020$CODE_PATH/spark-examples_2.12-1.0.jar \
               hdfs://master$CODE_PATH/data/transactions.txt;;
        [3]* ) break;;
        * ) echo "Please answer 1, 2, or 3.";;
    esac
done
#! /bin/bash
CODE_PATH=/user/custom/5.3.3
INPUT_DATA=../5.3.3.1/data/allFlickrRome.tar.gz

sudo chown $(whoami):$(id -gn) -R .
tar -zxf $INPUT_DATA -C /tmp

hadoop fs -mkdir -p $CODE_PATH/data
hadoop fs -put -f /tmp/allFlickrRome.json $CODE_PATH/data/
hadoop fs -put -f /opt/hive/lib/ion-hive3-serde-all-1.2.0.jar $CODE_PATH

cd ../5.3.3.1 && \
mvn package && \
hadoop fs -put -f target/HiveUDF-1.0-SNAPSHOT-jar-with-dependencies.jar $CODE_PATH

cd ../5.3.3.2 && \
python pyspark/clustering-hive.py
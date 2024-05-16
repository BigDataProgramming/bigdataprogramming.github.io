#! /bin/bash
CODE_PATH=/user/custom/5.3.3

sudo chown $(whoami):$(id -gn) -R .
hadoop fs -mkdir -p $CODE_PATH/hive-data
hadoop fs -put -f ./data/allFlickrRome.json $CODE_PATH/hive-data
hadoop fs -put -f /opt/hive/lib/ion-hive3-serde-all-1.2.0.jar $CODE_PATH

mvn package && \
hadoop fs -put -f target/HiveUDF-1.0-SNAPSHOT-jar-with-dependencies.jar $CODE_PATH

hive -f data/hive_script.sql

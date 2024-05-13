#! /bin/bash
CODE_PATH=/user/custom/5.3.1.2

sudo chown $(whoami):$(id -gn) -R .
hadoop fs -rm -R $CODE_PATH
hadoop fs -mkdir -p $CODE_PATH
hadoop fs -put -f data/FlickrRomeSample.json data/rome.kml $CODE_PATH

mvn install:install-file -Dfile=./lib/mgfsm-hadoop.jar  -DgroupId=de.mpii.fsm -DartifactId=mgfsm-hadoop -Dversion=1.0.0 -Dpackaging=jar && \
mvn package

hadoop fs -put -f target/traj-hadoop-1.0-jar-with-dependencies.jar $CODE_PATH

hadoop jar target/traj-hadoop-1.0-jar-with-dependencies.jar it.unical.dimes.scalab.trajectory.Main \
$CODE_PATH/FlickrRomeSample.json \
$CODE_PATH/outputMR/ \
$CODE_PATH/fpOutput/ 3 50 \
$CODE_PATH/rome.kml
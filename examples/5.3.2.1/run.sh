#! /bin/bash
sudo chown $(whoami):$(id -gn) -R .
tar -zxf data/kddcup_sample_5_3_2_1.tar.gz -C data

echo "Training models..."
mkdir -p python/models
python python/batchLearning.py

echo "Compiling Storm Topology JAR"
rm results.txt
mvn package && \
storm jar target/ids-storm-1.0-jar-with-dependencies.jar it.unical.dimes.scalab.ids.IntrusionTopology && \
storm set_log_level IntrusionDetection -l ROOT=DEBUG:30
sleep 60
storm kill IntrusionDetection

echo "Cleaning files..."
rm -rf python/models
rm data/*.csv

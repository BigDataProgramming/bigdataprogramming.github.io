#! /bin/bash
sudo chown $(whoami):$(id -gn) -R .
echo "Training models..."
tar -zxf data/kddcup_data.tar.gz -C data
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
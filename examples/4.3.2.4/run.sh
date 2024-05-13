#! /bin/bash
sudo chown $(whoami):$(id -gn) -R .
tar -zxf data/*.tar.gz -C data

echo "Compiling Storm Topology JAR"
mvn package && \
storm jar target/hashtag-counter-storm-1.0-jar-with-dependencies.jar it.unical.dimes.scalab.hashtag_count.HashtagCountTopology && \
storm set_log_level HashtagCountTopology -l ROOT=DEBUG:30
sleep 60
storm kill HashtagCountTopology
rm data/*.csv
#! /bin/bash
CODE_PATH=/user/custom/4.6.1.4

sudo chown $(whoami):$(id -gn) -R .
hadoop fs -mkdir -p $CODE_PATH/hive-data
hadoop fs -put -f /opt/examples/4.6.1.4/data/movie_ratings.csv $CODE_PATH/hive-data
hadoop fs -put -f /opt/examples/4.6.1.4/python/week_mapper.py $CODE_PATH

hive -f data/hive_script.sql

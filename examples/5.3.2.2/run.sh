#! /bin/bash
CODE_PATH=/user/custom/5.3.2.2
REAL_TIME_PATH=$CODE_PATH/real_time
INPUT=data

sudo chown $(whoami):$(id -gn) -R .
echo "Training models..."
tar -zxf data/kddcup_data.tar.gz -C data
hadoop fs -rm -R $CODE_PATH
hadoop fs -mkdir -p $CODE_PATH
hadoop fs -put -f data/kddcup_train_10.csv $CODE_PATH

# Offline training
sbt assembly && \
hadoop fs -put -f target/scala-2.12/intrusion-detection_2.12-1.0.jar $CODE_PATH

spark-submit \
--class it.unical.dimes.scalab.ids.IntrusionDetection \
--deploy-mode client \
--master spark://master:7077 \
hdfs://master:8020$CODE_PATH/intrusion-detection_2.12-1.0.jar "offline-training" \
hdfs://master$CODE_PATH/kddcup_train_10.csv \
hdfs://master$CODE_PATH/models/

hadoop fs -rm -R $REAL_TIME_PATH
hadoop fs -mkdir -p $REAL_TIME_PATH

# Path to the local text file
LOCAL_FILE="data/kddcup_test_sample_10.csv"

# Path to the file on HDFS
hdfs_file="$REAL_TIME_PATH/kddcup_test_sample_10.csv"

# Create an empty file on HDFS
hdfs dfs -touchz $hdfs_file

# Real time training
echo "Real time intrusion detection..."
spark-submit \
--class it.unical.dimes.scalab.ids.IntrusionDetection \
--deploy-mode client \
--master spark://master:7077 \
hdfs://master:8020$CODE_PATH/intrusion-detection_2.12-1.0.jar \
"real-time-ids" \
hdfs://master:8020$REAL_TIME_PATH \
hdfs://master:8020$CODE_PATH/models/ &


i=0
while [ "$i" -lt 30 ]; do
    # Read a line from the local text file
    line=$(head -n 1 "$LOCAL_FILE")

    # Append the line to the HDFS file
    echo "$line" | hdfs dfs -appendToFile - $hdfs_file

    # Remove the read line from the local text file
    sed -i '1d' "$LOCAL_FILE"

    i=$(( i + 1 ))

    # Sleep for 1 second
    sleep 1
done

echo "Cleaning files..."
rm data/*.csv

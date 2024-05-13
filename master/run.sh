#!/bin/bash

if [ -z "$(ls -A "$NAMEDIR")" ]; then
  echo "Formatting namenode name directory: $NAMEDIR"
  hdfs namenode -format
fi

echo "Starting SSH service name node..."
sudo service ssh start

echo "Starting Hadoop name node..."
hdfs --daemon start namenode

echo "Starting Hadoop resource manager..."
yarn --daemon start resourcemanager

if [ ! -f "$NAMEDIR"/initialized ]; then
  echo "Configuring Hive..."
  hdfs dfs -mkdir -p  /user/hive/warehouse
  schematool -dbType postgres -initSchema
  touch "$NAMEDIR"/initialized
fi

echo "Starting Hive Metastore..."
hive --service metastore &

echo "Starting Hive server2..."
hiveserver2 &

echo "Starting Zookeeper server..."
sudo /bin/bash -c "/opt/zookeeper/bin/zkServer.sh start-foreground &"
echo "Starting Storm Nimbus service..."
storm nimbus &
echo "Starting Storm UI server..."
storm ui &

echo "Linking Airflow DAGs"
ln -s /opt/examples/4.3.3.2/dags/*.py /opt/airflow/dags/

echo "Starting Airflow services..."
airflow db migrate; airflow connections import $AIRFLOW_HOME/airflow_connections.json
airflow scheduler -D &
airflow triggerer -D &
airflow users create --role Admin --username $AIRFLOW_UI_ADMIN_USERNAME --email admin --firstname admin --lastname admin --password $AIRFLOW_UI_ADMIN_PASSWORD &
airflow webserver -D &

if ! hdfs dfs -test -d /tmp
then
  echo "Formatting directory: /tmp"
  hdfs dfs -mkdir -p  /tmp
fi
if ! hdfs dfs -test -d "$SPARK_LOGS_HDFS_PATH"
then
  echo "Formatting directory: $SPARK_LOGS_HDFS_PATH"
  hdfs dfs -mkdir -p  "$SPARK_LOGS_HDFS_PATH"
fi
if ! hdfs dfs -test -d "$SPARK_JARS_HDFS_PATH"
then
  echo "Formatting directory: $SPARK_JARS_HDFS_PATH"
  hdfs dfs -mkdir -p  "$SPARK_JARS_HDFS_PATH"
  hdfs dfs -put "$SPARK_HOME"/jars/* "$SPARK_JARS_HDFS_PATH"/
fi

echo "Starting Spark master node..."
spark-class org.apache.spark.deploy.master.Master --ip "$SPARK_MASTER_HOST"


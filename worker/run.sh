#!/bin/bash

echo "Starting SSH service name node..."
sudo service ssh start

echo "Starting Storm Supervisor server..."
storm supervisor &

echo "Linking Airflow DAGs"
ln -s /opt/examples/4.3.3.2/dags/*.py /opt/airflow/dags/

echo "Starting Airflow worker..."
airflow celery worker  &

echo "Starting Hadoop data node..."
hdfs --daemon start datanode

echo "Starting Hadoop node manager..."
yarn --daemon start nodemanager

echo "Starting Spark worker node..."
spark-class org.apache.spark.deploy.worker.Worker "spark://master:7077"

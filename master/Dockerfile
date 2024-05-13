FROM bigdata-book-base

ENV NAMEDIR=/opt/hadoop/dfs/name
RUN mkdir -p /opt/hadoop/dfs/name
VOLUME /opt/hadoop/dfs/name

ENV SPARK_PUBLIC_DNS=localhost
ENV SPARK_LOGS_HDFS_PATH=/var/log/spark
ENV SPARK_JARS_HDFS_PATH=/spark/jars
ENV AIRFLOW_UI_ADMIN_USERNAME=admin
ENV AIRFLOW_UI_ADMIN_PASSWORD=admin

COPY run.sh /usr/local/sbin/run.sh
RUN sudo chmod a+x /usr/local/sbin/run.sh
CMD ["run.sh"]
FROM bigdata-book-base AS base

ARG USERNAME=jupyter
ARG GROUPNAME=jupyter
ARG UID=1001
ARG GID=1001

USER $USERNAME

COPY run.sh /usr/local/sbin/run.sh
RUN sudo chmod a+x /usr/local/sbin/run.sh

ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/

# Hadoop
ENV HADOOP_HOME=/opt/hadoop
ENV HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop
ENV PATH=$HADOOP_HOME/sbin:$HADOOP_HOME/bin:$PATH
ENV LD_LIBRARY_PATH=$HADOOP_HOME/lib/native:$LD_LIBRARY_PATH

# Spark
ENV SPARK_HOME=/opt/spark
ENV PYTHONHASHSEED=1
ENV PYSPARK_PYTHON=python3
ENV SPARK_CONF_DIR=$SPARK_HOME/conf
ENV PATH=$SPARK_HOME/sbin:$SPARK_HOME/bin:$PATH

# Hive
ENV HIVE_HOME=/opt/hive
ENV HIVE_CONF_DIR=$HIVE_HOME/conf
ENV PATH=$HIVE_HOME/sbin:$HIVE_HOME/bin:$PATH

RUN sudo pip install -U pip \
    && sudo pip install --no-cache-dir notebook spylon-kernel \
    && python -m spylon_kernel install --user

WORKDIR /home/$USERNAME
CMD ["run.sh"]

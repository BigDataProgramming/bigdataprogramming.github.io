FROM ubuntu:22.04

RUN sed -i -e "s|http://archive.ubuntu.com|http://jp.archive.ubuntu.com|g" /etc/apt/sources.list \
 && apt-get -qq update  \
 && DEBIAN_FRONTEND=noninteractive apt-get -qq install --no-install-recommends \
      sudo \
      openjdk-8-jdk \
      curl \
      openssh-server \
      gnupg \
      maven \
      nano \
      cmake make cpio gfortran gcc g++ openmpi-bin openmpi-doc libopenmpi-dev \
      procps \
      python3 \
      python3-pip \
      python-is-python3 \
      coreutils \
      libc6-dev \
 && rm -rf /var/lib/apt/lists/*

ARG USERNAME=jupyter
ARG GROUPNAME=jupyter
ARG UID=1001
ARG GID=1001

RUN echo $USERNAME ALL=\(root\) NOPASSWD:ALL > /etc/sudoers.d/$USERNAME \
 && chmod 0440 /etc/sudoers.d/$USERNAME \
 && groupadd -g $GID $GROUPNAME \
 && useradd -m -s /bin/bash -u $UID -g $GID $USERNAME

USER $USERNAME

# Java 1.8 as default
ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/
RUN sudo apt purge -y openjdk-11-* \
 && sudo update-alternatives --install /usr/bin/java java $JAVA_HOME/bin/java 1 \
 && sudo update-alternatives --install /usr/bin/javac javac $JAVA_HOME/bin/javac 1

# Hadoop
ARG HADOOP_VERSION=3.3.6
ARG HADOOP_URL=https://www.apache.org/dist/hadoop/common/hadoop-$HADOOP_VERSION/hadoop-$HADOOP_VERSION.tar.gz
ENV HADOOP_HOME=/opt/hadoop

RUN set -x \
 && curl -fsSL https://archive.apache.org/dist/hadoop/common/KEYS -o /tmp/hadoop-KEYS \
 && gpg --import /tmp/hadoop-KEYS \
 && sudo mkdir $HADOOP_HOME  \
 && sudo chown $USERNAME:$GROUPNAME -R $HADOOP_HOME \
 && curl -fsSL $HADOOP_URL -o /tmp/hadoop.tar.gz \
 && curl -fsSL $HADOOP_URL.asc -o /tmp/hadoop.tar.gz.asc \
 && gpg --verify /tmp/hadoop.tar.gz.asc \
 && tar -xf /tmp/hadoop.tar.gz -C $HADOOP_HOME --strip-components 1 \
 && mkdir $HADOOP_HOME/logs \
 && rm /tmp/hadoop*

ENV HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop
ENV PATH=$HADOOP_HOME/sbin:$HADOOP_HOME/bin:$PATH
ENV LD_LIBRARY_PATH=$HADOOP_HOME/lib/native:$LD_LIBRARY_PATH

# Spark
ARG SPARK_VERSION=3.4.0
ARG SPARK_URL=https://archive.apache.org/dist/spark/spark-$SPARK_VERSION/spark-$SPARK_VERSION-bin-hadoop3.tgz
ENV SPARK_HOME=/opt/spark

RUN set -x \
 && curl -fsSL https://archive.apache.org/dist/spark/KEYS -o /tmp/spark-KEYS  \
 && gpg --import /tmp/spark-KEYS \
 && sudo mkdir $SPARK_HOME \
 && sudo chown $USERNAME:$GROUPNAME -R $SPARK_HOME \
 && curl -fsSL $SPARK_URL -o /tmp/spark.tgz \
 && curl -fsSL $SPARK_URL.asc -o /tmp/spark.tgz.asc \
 && gpg --verify /tmp/spark.tgz.asc \
 && tar -xf /tmp/spark.tgz -C $SPARK_HOME --strip-components 1 \
 && rm /tmp/spark* \
 && curl -fsSL https://jdbc.postgresql.org/download/postgresql-42.3.2.jar -o $SPARK_HOME/jars/postgresql-42.3.2.jar

ENV PYTHONHASHSEED=1
ENV PYSPARK_PYTHON=python3
ENV SPARK_CONF_DIR=$SPARK_HOME/conf
ENV PATH=$SPARK_HOME/sbin:$SPARK_HOME/bin:$PATH

# Hive
ARG HIVE_VERSION=3.1.3
ARG HIVE_URL=https://archive.apache.org/dist/hive/hive-$HIVE_VERSION/apache-hive-$HIVE_VERSION-bin.tar.gz
ENV HIVE_HOME=/opt/hive
ENV HIVE_CONF_DIR=$HIVE_HOME/conf

RUN set -x \
 && curl -fsSL https://archive.apache.org/dist/hive/KEYS -o /tmp/hive-KEYS  \
 && gpg --import /tmp/hive-KEYS \
 && sudo mkdir $HIVE_HOME \
 && sudo chown $USERNAME:$GROUPNAME -R $HIVE_HOME \
 && curl -fsSL $HIVE_URL -o /tmp/hive.tar.gz \
 && curl -fsSL $HIVE_URL.asc -o /tmp/hive.tar.gz.asc \
 && gpg --verify /tmp/hive.tar.gz.asc \
 && tar -xf /tmp/hive.tar.gz -C $HIVE_HOME --strip-components 1 \
 && rm /tmp/hive* \
 && curl -fsSL https://github.com/amazon-ion/ion-hive-serde/releases/download/v1.2.0/ion-hive3-serde-all-1.2.0.jar -o $HIVE_HOME/lib/ion-hive3-serde-all-1.2.0.jar

ENV PATH=$HIVE_HOME/sbin:$HIVE_HOME/bin:$PATH

# OpenMPI
ENV OPENMPI_HOME=/opt/openmpi
ENV OPENMPI_VERSION=4.0.1
ENV OPENMPI_BRANCH=4.0
ENV OPENMPI_URL=https://download.open-mpi.org/release/open-mpi/v$OPENMPI_BRANCH/openmpi-$OPENMPI_VERSION.tar.gz
RUN set -x \
    && curl -fsSL $OPENMPI_URL -o /tmp/openmpi.tar.gz \
    && sudo mkdir $OPENMPI_HOME \
    && sudo chown $USERNAME:$GROUPNAME -R $OPENMPI_HOME \
    && tar -zxf /tmp/openmpi.tar.gz -C $OPENMPI_HOME --strip-components 1 \
    && rm /tmp/openmpi* \
    && cd $OPENMPI_HOME \
    && sudo ./configure --enable-mpi-java --prefix=$OPENMPI_HOME/ \
    && sudo make all install

RUN set -x \
    && sudo mkdir -p /usr/lib/jvm/default-java/bin/ \
    && sudo mkdir -p /usr/lib/x86_64-linux-gnu/openmpi/lib/ && sudo cp $OPENMPI_HOME/lib/*.jar /usr/lib/x86_64-linux-gnu/openmpi/lib/ \
    && sudo ln -s /etc/alternatives/javac /usr/lib/jvm/default-java/bin/javac

ENV PATH=$OPENMPI_HOME:$PATH


# Sbt
RUN set -x \
   && sudo apt-get install apt-transport-https curl gnupg -yqq \
   && echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | sudo tee /etc/apt/sources.list.d/sbt.list \
   && echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | sudo tee /etc/apt/sources.list.d/sbt_old.list \
   && curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | sudo -H gpg --no-default-keyring --keyring gnupg-ring:/etc/apt/trusted.gpg.d/scalasbt-release.gpg --import \
   && sudo chmod 644 /etc/apt/trusted.gpg.d/scalasbt-release.gpg \
   && sudo apt-get update \
   && sudo apt-get -y install sbt

# Zookeper
ARG ZOOKEEPER_VERSION=3.8.3
ARG ZOOKEEPER_URL=https://archive.apache.org/dist/zookeeper/zookeeper-$ZOOKEEPER_VERSION/apache-zookeeper-$ZOOKEEPER_VERSION-bin.tar.gz
ENV ZOOKEEPER_HOME=/opt/zookeeper
ENV ZOOKEEPER_CONF_DIR=$ZOOKEEPER_HOME/conf
EXPOSE 2181 2888 3888 8096
RUN set -x \
 && curl -fsSL https://archive.apache.org/dist/zookeeper/KEYS -o /tmp/zookeeper-KEYS  \
 && gpg --import /tmp/zookeeper-KEYS \
 && sudo mkdir $ZOOKEEPER_HOME \
 && sudo chown $USERNAME:$GROUPNAME -R $ZOOKEEPER_HOME \
 && curl -fsSL $ZOOKEEPER_URL -o /tmp/zookeeper.tar.gz \
 && curl -fsSL $ZOOKEEPER_URL.asc -o /tmp/zookeeper.tar.gz.asc \
 && gpg --verify /tmp/zookeeper.tar.gz.asc \
 && tar -xf /tmp/zookeeper.tar.gz -C $ZOOKEEPER_HOME --strip-components 1 \
 && rm /tmp/zookeeper*

ENV PATH=$ZOOKEEPER_HOME/bin:$PATH

# Storm
ARG STORM_VERSION=2.5.0
ARG STORM_URL=https://archive.apache.org/dist/storm/apache-storm-$STORM_VERSION/apache-storm-$STORM_VERSION.tar.gz
ENV STORM_HOME=/opt/storm
ENV STORM_CONF_DIR=$STORM_HOME/conf

RUN set -x \
 && curl -fsSL https://archive.apache.org/dist/storm/KEYS -o /tmp/storm-KEYS  \
 && gpg --import /tmp/storm-KEYS \
 && sudo mkdir $STORM_HOME \
 && sudo chown $USERNAME:$GROUPNAME -R $STORM_HOME \
 && curl -fsSL $STORM_URL -o /tmp/storm.tar.gz \
 && curl -fsSL $STORM_URL.asc -o /tmp/storm.tar.gz.asc \
 && gpg --verify /tmp/storm.tar.gz.asc \
 && tar -xf /tmp/storm.tar.gz -C $STORM_HOME --strip-components 1 \
 && rm /tmp/storm*

ENV PATH=$STORM_HOME/bin:$PATH

# Pig
ARG PIG_VERSION=0.17.0
ARG PIG_URL=https://archive.apache.org/dist/pig/pig-$PIG_VERSION/pig-$PIG_VERSION.tar.gz
ENV PIG_HOME=/opt/pig
ENV PIG_CONF_DIR=$PIG_HOME/conf

RUN set -x \
 && curl -fsSL https://archive.apache.org/dist/pig/KEYS -o /tmp/pig-KEYS  \
 && gpg --import /tmp/pig-KEYS \
 && sudo mkdir $PIG_HOME \
 && sudo chown $USERNAME:$GROUPNAME -R $PIG_HOME \
 && curl -fsSL $PIG_URL -o /tmp/pig.tar.gz \
 && curl -fsSL $PIG_URL.asc -o /tmp/pig.tar.gz.asc \
 && gpg --verify /tmp/pig.tar.gz.asc \
 && tar -xf /tmp/pig.tar.gz -C $PIG_HOME --strip-components 1 \
 && rm /tmp/pig*

ENV PATH=$PIG_HOME/sbin:$PIG_HOME/bin:$PATH


# Airflow
ARG AIRFLOW_VERSION=2.8.2
ENV AIRFLOW_HOME=/opt/airflow
RUN sudo pip install "apache-airflow[celery,hive,apache.hdfs,postgres,rabbitmq]==${AIRFLOW_VERSION}" \
    && sudo mkdir /opt/airflow /opt/airflow/logs /opt/airflow/dags /opt/airflow/plugins /run/airflow \
    && sudo chown $USERNAME:$GROUPNAME -R $AIRFLOW_HOME /run/airflow

# Config
COPY --chown=$USERNAME:$GROUPNAME conf/core-site.xml $HADOOP_CONF_DIR/
COPY --chown=$USERNAME:$GROUPNAME conf/hdfs-site.xml $HADOOP_CONF_DIR/
COPY --chown=$USERNAME:$GROUPNAME conf/yarn-site.xml $HADOOP_CONF_DIR/
COPY --chown=$USERNAME:$GROUPNAME conf/mapred-site.xml $HADOOP_CONF_DIR/
COPY --chown=$USERNAME:$GROUPNAME conf/workers $HADOOP_CONF_DIR/
COPY --chown=$USERNAME:$GROUPNAME conf/spark-defaults.conf $SPARK_CONF_DIR/
COPY --chown=$USERNAME:$GROUPNAME conf/log4j.properties $SPARK_CONF_DIR/
COPY --chown=$USERNAME:$GROUPNAME conf/hive-site.xml $HIVE_CONF_DIR/
COPY --chown=$USERNAME:$GROUPNAME conf/zoo.cfg $ZOOKEEPER_CONF_DIR/
COPY --chown=$USERNAME:$GROUPNAME conf/storm.yaml $STORM_CONF_DIR/
COPY --chown=$USERNAME:$GROUPNAME conf/airflow.cfg $AIRFLOW_HOME/
COPY --chown=$USERNAME:$GROUPNAME conf/airflow_connections.json $AIRFLOW_HOME/
COPY --chown=$USERNAME:$GROUPNAME conf/requirements.txt /tmp/

RUN ln $HADOOP_CONF_DIR/workers $SPARK_CONF_DIR/ \
 && ln $HIVE_CONF_DIR/hive-site.xml $SPARK_CONF_DIR/

RUN pip install -r /tmp/requirements.txt

# SSH service
EXPOSE 22
RUN mkdir -p /home/$USERNAME/.ssh/ && \
    chmod 0700 /home/$USERNAME/.ssh  && \
    touch /home/$USERNAME/.ssh/authorized_keys && \
    chmod 600 /home/$USERNAME/.ssh/authorized_keys && \
    touch /home/$USERNAME/.ssh/config && \
    chmod 600 /home/$USERNAME/.ssh/config

COPY --chown=$USERNAME:$GROUPNAME --chmod=0777  ssh-keys/ /keys/

RUN cat /keys/big_data_book.pub >> /home/$USERNAME/.ssh/authorized_keys && \
    cat /keys/config >> /home/$USERNAME/.ssh/config

# Entry point
COPY entrypoint.sh /usr/local/sbin/entrypoint.sh
RUN sudo chmod a+x /usr/local/sbin/entrypoint.sh
ENTRYPOINT ["entrypoint.sh"]

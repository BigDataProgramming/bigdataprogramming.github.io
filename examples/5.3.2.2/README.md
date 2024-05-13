# Spark Programming Example: Real-time Network Intrusion Detection using Spark Streaming
The proposed application implements a network intrusion detection
system (NIDS) aimed at early detection of network intrusion and
malicious activities. Network intrusion detection is a crucial part
of network management to improve security and ensure the quality
of service. Typically, these systems use data mining or machine
learning techniques to automatically detect attacks against computer
networks and systems.

The first part of the application comprises training an offline
machine learning model, specifically a random forest classifier, on
the training data.  After training and testing the machine learning model, it can be used for the real-time detection of malicious connections. To do
this, the model that was previously saved to disk has to be loaded
and used in conjunction with the Spark Streaming APIs, which are
used to handle the input stream.

> **_NOTE:_** In this example, the *streamingContext.textFileStream(dataDirectory)* is used to allows Spark Streaming to monitor the specified directory
and process any files created in that directory. All files located
directly under this path will be processed as they are detected. 

### Data description 
The ```data/kddcup_data.tar.gz``` contains both the training data for the offline training of the models and the test data for the real-time network intrusion detection. The datasets (https://kdd.ics.uci.edu/databases/kddcup99/kddcup99.html) contain examples of malicious connections, called intrusions or attacks, and normal connections. Each connection is described by a set of 41 features,
which describe the characteristics of a network connection (e.g.,
duration, protocol type, service, flag, source bytes, destination_bytes, etc.), and labeled as either normal, or as an attack. 


## How to run
The application comes with a script that automatically builds the 
application and runs it on the Spark cluster. To launch the application simply open a terminal in the _master_ container: 

```bash
docker exec -ti bigdata-book-master /bin/bash
```

Then, run the following commands in the container shell:
```bash
cd /opt/examples/5.3.2.2
bash ./run.sh
```

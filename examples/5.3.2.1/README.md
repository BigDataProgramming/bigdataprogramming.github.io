# Storm Programming Example: Real-time Network Intrusion Detection

The proposed application implements a network intrusion detection
system (NIDS) aimed at early detection of network intrusion and
malicious activities. Network intrusion detection is a crucial part
of network management to improve security and ensure the quality
of service. Typically, these systems use data mining or machine
learning techniques to automatically detect attacks against computer
networks and systems.

### Storm topology
The application adopts a Storm topology that is composed of one spout and two bolts:
- *ConnectionSpout*, which is the data source in the topology. This
spout streams connections that come from a firewall or are stored
in a log file, and each record is forwarded as a tuple to the next bolt.
- *DataPreprocessingBolt*, which receives the tuples from the spout
and performs some preprocessing steps on the data. Specifically, the preprocessing phase includes the conversion of categorical features
into numerical ones and the standardization of these features
for the machine learning model.
- *ModelBolt*, which performs the classification through a random forest
model that is trained offline and used in real time for monitoring
connections to detect potential malicious activities.

### Machine learning model training
The training phase is performed offline using the Python
*scikit-learn* library (```python/batchLearning.py```) since Storm does not provide any native machine
learning libraries. All the trained models (i.e., the standard scaler
for numerical features, the label encoder for categorical features, and
the random forest model) are dumped in files using the Python *pickle*
module. Hence, the Storm **multi-language protocol** can be adopted to
integrate the trained models into a topology implemented in a JVM
language.  The ```python/storm.py``` script enables the multilang protocol within Storm, facilitating seamless communication between Python and Java components.

### Data description

The ```data/kddcup_data.tar.gz``` contains both the training data for the offline training of the models and the test data for the real-time network intrusion detection. The datasets (https://kdd.ics.uci.edu/databases/kddcup99/kddcup99.html) contain examples of malicious connections, called intrusions or attacks, and normal connections. Each connection is described by a set of 41 features,
which describe the characteristics of a network connection (e.g.,
duration, protocol type, service, flag, source bytes, destination_bytes, etc.), and labeled as either normal, or as an attack. 





## How to run

The application comes with a script that automatically builds the
application and runs it on the Storm cluster.
To launch the application simply open a terminal in the _master_ container:

```bash
docker exec -ti bigdata-book-master /bin/bash
```

Then, run the following commands in the container shell:
```bash
cd /opt/examples/5.3.2.1
bash ./run.sh
```

Results will be stored in a file named ```results.txt```, containing the predicted attack type for each input tuple processed by the topology.


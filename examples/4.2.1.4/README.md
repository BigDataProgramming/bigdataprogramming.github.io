# Hadoop Programming Example: Inverted index

We provide an application example that shows how Hadoop MapReduce
can be exploited for creating an inverted index for a large set
of web documents. An inverted index contains a set of words (index terms), and for each word it specifies the IDs of
all the documents that contain it and the number of occurrences in
each document. 

## How to run

The application comes with a script that automatically builds the 
application and runs it on the Hadoop cluster. 
To launch the application simply open a terminal in the _master_ container: 

```bash
docker exec -ti bigdata-book-master /bin/bash
```

Then, run the following commands in the container shell:
```bash
cd /opt/examples/4.2.1.4
bash ./run.sh
```

Results will be stored in HDFS in the directory ```/user/custom/4.2.1.4/hadoop-output```. To display them, run the following command:

```bash
hdfs dfs -cat /user/custom/4.2.1.4/hadoop-output/part-r-00000
```

Alternatively, copy the output file from HDFS to a local directory with the following command:
```bash
hdfs dfs -copyToLocal /user/custom/4.2.1.4/hadoop-output/part-r-00000 <local_output_path>
```

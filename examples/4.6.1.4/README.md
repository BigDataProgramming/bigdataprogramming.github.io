# Hive Programming Example: Querying on a Movie Rating Dataset

The proposed application shows how Hive can be employed to store
data about the ratings of users for a set of movies and to perform
some queries on these data using HiveQL. Hive also allows users to perform more complex data
analyses by defining UDFs in other programming languages. For example, the Python script ```python/week mapper.py``` maps the timestamp of each movie review to the week of the year it was produced and aggregates the results. 

### Data description
Data are organized in a *csv* file (```data/movie_ratings.csv```) with the following fields:
- *user id*, 
- *movie id*, 
- *rating* (from 0 to 5),
- *timestamp* at which the rating was generated.

> **_NOTE:_** This example shows also the process of storing results from a Hive query into HDFS.

## How to run

The application comes with a script that automatically builds the
application and runs it in a distributed manner with one master and two workers.
To launch the application simply open a terminal in the _master_ container:

```bash
docker exec -ti bigdata-book-master /bin/bash
```

Then, run the following commands in the container shell:
```bash
cd /opt/examples/4.6.1.4
bash ./run.sh
```

Results will be stored in HDFS in the directory ```/user/custom/4.6.1.4/hive-results```. To display them, run the following command:

```bash
hdfs dfs -cat /user/custom/4.6.1.4/hive-results/000000_0
```

Alternatively, copy the output file from HDFS to a local directory with the following command:
```bash
hdfs dfs -copyToLocal /user/custom/4.6.1.4/hive-results/000000_0 <local_output_path>
```

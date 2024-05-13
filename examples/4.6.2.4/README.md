# Pig Programming Example: Sentiment Analysis on Customer Reviews
The Pig application discussed here shows how to use Pig for implementing a dictionary-based sentiment analyzer on a dataset containing customer reviews about some e-commerce products (```data/reviews.csv```). Since Pig does not provide a built-in library
for sentiment analysis, the system exploits external dictionaries (```data/dict.txt```) to associate words to their sentiments and determine the semantic orientation of the opinion words.
Given a dictionary of words associated with positive or negative sentiment, the sentiment of a review is calculated by summing
the scores of positive and negative words in the text and then by calculating the average rating.

### Using UDFs for advanced analytics
Developers can include advanced analytics in a script by defining UDFs. For example, the ```PROCESS``` UDF is aimed at processing a
tuple by removing punctuation as a preprocessing step. Other functionalities, if required, can be added to the *exec* method, which is implemented in Java (```src/main/java/it/unical/dimes/scalab/pig/Processing.java```). 


## How to run
The application comes with a script that automatically builds the
application and runs it on the Hadoop cluster. To launch the application simply open a terminal in the _master_ container:


```bash
docker exec -ti bigdata-book-master /bin/bash
```

Then, run the following commands in the container shell:
```bash
cd /opt/examples/4.6.2.4
bash ./run.sh
```

Results will be stored in HDFS in the directory ```/user/custom/4.6.2.4/pig-results```. To display them, run the following command:

```bash
hdfs dfs -cat /user/custom/4.6.2.4/pig-results/part-r-00000
```

Alternatively, copy the output file from HDFS to a local directory with the following command:
```bash
hdfs dfs -copyToLocal /user/custom/4.6.2.4/pig-results/part-r-00000 <local_output_path>
```



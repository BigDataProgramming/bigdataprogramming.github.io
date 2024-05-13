# MPI Programming Example: TextRank for Extractive Summarization

Extractive summarization involves condensing essential information from a document into a brief summary, by selecting key sentences from the original text. The proposed programming example shows how MPI can be leveraged to realize an extractive summarization application based on the **TextRank** algorithm (_Mihalcea and Tarau, 2004_).
In particular, it represents the input text as a graph of semantically connected sentences and extracts a summary by identifying the top-k most representative sentences using PageRank.

Given the input text ${\cal T}$ (```data/ANN.txt```), TextRank creates a graph ${\cal G}=\langle S,E\rangle$, where $S=s_1, \dots,s_n$ are the sentences in ${\cal T}$. Each pair of sentences in $S$ is connected by an edge, with weights representing their textual similarity. The similarity between two sentences is determined as follows:

$$w_{i,j} = \frac{|s_i \cap s_j|}{\log(|s_i + 1|) + \log(|s_j + 1|) + 1}$$

Similarity between two sentences is directly proportional to the words they have in common but is inversely proportional to their length, following the assumption that very long phrases may have a high intersection with each other, which does not necessarily entail a high similarity.
Given this graph, the PageRank algorithm is then used to extract the most representative sentences, which are concatenated and returned as the summary.


## How to run

The application comes with a script that automatically builds the
application and runs it on the MPI cluster.
To launch the application simply open a terminal in the _master_ container:

```bash
docker exec -ti bigdata-book-master /bin/bash
```

Then, run the following commands in the container shell:
```bash
cd /opt/examples/5.3.4.2
bash ./run.sh
```
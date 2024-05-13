# MPI Programming Example: Character Count

This example presents an MPI application written in Java + Open MPI for counting
english characters in parallel through a set of processes reading from a text
file. In particular,
given an input file of M bytes and N processes, each process
reads a chunk of M/N bytes and counts each character in a private
data structure. The master process, with rank equal to 0, receives
the partial counts from all the processes within the group with the
specified tag and aggregates them.


The ```data/data.txt``` file is a plain text document comprising English text. The output is stored in a ```output.txt``` file, which will contain the occurrences of each of the 26 characters of the English alphabet in the text.

> **_NOTE:_** In this example, a more general approach to handling files of arbitrary size is presented compared to the method outlined in the book. The example in the book assumes that the file's size is evenly divisible by the number of workers involved in the MPI cluster. 

## How to run

The application comes with a script that automatically builds the
application and runs on the MPI cluster.
To launch the application simply open a terminal in the _master_ container:

```bash
docker exec -ti bigdata-book-master /bin/bash
```

Then, run the following commands in the container shell:
```bash
cd /opt/examples/4.4.1.2
bash ./run.sh
```

Results will be stored by the master in a file named ```output.txt```.

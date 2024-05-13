# GraphX Programming Example: PageRank

The proposed programming example shows how the GraphX can be leveraged to implement the **PageRank** algorithm (_Brin and Page, 1998_), used by search engines like Google Search to rank website pages.
It is based on the idea that a link from one page to another can be seen as a vote of confidence, i.e., pages with more incoming links from reputable sources are considered more important or authoritative.

PageRank models the process that leads a user to a given page:
- The user generally lands on that page through a sequence of random links, following a path through multiple pages.
- The user can eventually stop clicking on outgoing links and searching for a different URL that is not directly reachable from the current page (i.e., the *random hop*).

The probability that a user will continue clicking on outgoing links is the damping factor ``d``, generally set to ``0.85``, while the random hop probability is ``1 − d``.

### Vertex-centric programming using the Pregel operator
The provided implementation of the PageRank algorithm
leverages the **Pregel API** provided by GraphX. The ``Pregel`` operator is a *Bulk Synchronous Parallel (BSP)* message-passing abstraction in which the computation consists of a sequence of supersteps. During a Pregel superstep, the framework invokes a user-defined function (UDF) for each vertex, which specifies its behavior and runs in parallel. Each vertex can change its status and read the messages sent
in the previous superstep or send new ones, which will be delivered
in the next superstep. In the end, when there are no more messages to be processed,
the Pregel operator stops iterating and returns the final state of
the graph.


## How to run

The application comes with a script that automatically builds the
application and runs it on the Spark cluster.
To launch the application simply open a terminal in the _master_ container:

```bash
docker exec -ti bigdata-book-master /bin/bash
```

Then, run the following commands in the container shell:
```bash
cd /opt/examples/4.5.1.4
bash ./run.sh
```
# Spark Programming Example: Trajectory Mining
This example presents an application for automatically discovering
user mobility patterns from geotagged Flickr posts generated in
the city of Rome. More in detail, the application aims at discovering
the most frequent user trajectories across some specific locations
or areas that are of interest for our analysis, commonly referred to
as points of interest (PoIs). Specifically, a PoI is a location that is
considered useful or interesting, such as a tourist attraction or business
location. Since information on a PoI is generally only limited to an
address or GPS coordinates, it is often useful to define the so-called regions
of interest (RoIs) that represent the boundaries of the PoIs’ areas. In such a way, a trajectory can be defined as a sequence of RoIs, representing a movement pattern over time. A
frequent trajectory is a sequence of RoIs that are frequently visited by
users.

The workflow of the proposed application
is composed of different steps. In particular, after collecting a set
of geotagged posts from Flickr, we apply some preprocessing to filter out the geotagged posts and map each geotagged post to a RoI.
Finally, we apply trajectory mining to extract frequent mobility patterns
in user trajectories across RoIs, aiming to better understand
how people move in the city of Rome.

### Data description 
The Flickr dataset used in this example (```data/FlickrRomeSample.json```) contains social media items, which include the following fields:

- a textual description,
- a set of keywords associated with the post,
- a pair of latitude and longitude that represents the coordinates where the post was created,
- an ID that identifies the user who created the post,
- a timestamp that indicates the date of creation of the post.

The file ```data/rome.kml``` is a Keyhole Markup Language (KML) file containing
the coordinates of some PoIs in Rome, which is used to define the RoIs.


## How to run
The application comes with a script that automatically builds the 
application and runs it on the Spark cluster. To launch the application simply open a terminal in the _master_ container: 

```bash
docker exec -ti bigdata-book-master /bin/bash
```

Then, run the following commands in the container shell:
```bash
cd /opt/examples/5.3.1.1
bash ./run.sh
```

Results will be stored in HDFS in the directory ```/user/custom/5.3.1.1/spark-results```. To display them, run the following command:

```bash
hdfs dfs -cat /user/custom/5.3.1.1/spark-results/part-00000
```

Alternatively, copy the output file from HDFS to a local directory with the following command:
```bash
hdfs dfs -copyToLocal /user/custom/5.3.1.1/spark-results/part-00000 <local_output_path>
```


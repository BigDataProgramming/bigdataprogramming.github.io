# Spark Programming Example: Region-of-Interest Mining using SparkSQL

One leading trend in social media research involves analyzing geotagged data to identify whether users have visited interesting locations, known as *Points-of-Interest* (PoIs), like tourist spots, malls, squares, and parks. To match user trajectories with PoIs, defining a *Region-of-Interest* (RoI) is helpful, representing the area boundaries of a PoI.

The proposed programming example shows how SparkSQL can be utilized to implement a data analytics application that extracts a suitable RoI by analyzing a large set of geotagged posts from social media. Posts are considered geotagged if they contain coordinates indicating the place of creation. Additionally, a post can be associated with a PoI if its text or tags refer to it. For instance, users may refer to the Colosseum using various keywords. Once all coordinates related to a PoI are extracted, a RoI is obtained by employing a spatial clustering algorithm like DBSCAN, forming a polygon enclosing the largest cluster of points.

### Using SparkSQL without UDAF functions
After retrieving data from a Hive table, it is stored in a ``DataFrame``, allowing for clustering using various machine learning libraries  (e.g., *Scikit-Learn*), without necessarily having to leverage any UDAF functions.

### Data description
The Flickr dataset used in this example (```data/allFlickrRome.json```) contains social media items, which include the following fields:

- a textual description,
- a pair of latitude and longitude that represents the coordinates where the post was created,
- a timestamp that indicates the date of creation of the post.

## How to run

The application comes with a script that automatically builds the
application and runs it on the Spark cluster.
To launch the application simply open a terminal in the _master_ container:

```bash
docker exec -ti bigdata-book-master /bin/bash
```

Then, run the following commands in the container shell:
```bash
cd /opt/examples/5.3.3.2
bash ./run.sh
```
package it.unical.dimes.scalab.utils;

import java.io.File;
import java.util.*;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.api.java.JavaRDD;
import org.apache.hadoop.fs.Path;
import de.micromata.opengis.kml.v_2_2_0.*;

public class KMLUtils {

    public static Map<String, String> lookupFromKml(String path) {
        Map<String, String> result = new HashMap<>();

        // Create a Spark session
        SparkSession spark = SparkSession.builder().getOrCreate();
        // Create a JavaSparkContext
        JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());

        // Read the text file into a JavaRDD
        JavaRDD<String> myRDD = jsc.textFile(path);

        // Coalesce the RDD into a single partition and reduce it to concatenate all strings
        String kml = myRDD.coalesce(1).reduce((str1, str2) -> str1 + "" + str2);

        Kml kmlContent = Kml.unmarshal(kml);
        Document document = (Document) kmlContent.getFeature();
        List<Feature> features = document.getFeature();
        LinkedList<Feature> featuresFound = new LinkedList<>();
        for (Feature f : features) {
            if (f instanceof Folder) {
                Folder folder = (Folder) f;
                featuresFound.addAll(folder.getFeature());
            } else if (f instanceof Placemark)
                featuresFound.add(f);
            for (Feature f1 : featuresFound) {
                Placemark placemark = (Placemark) f1;
                String placesName = placemark.getName().toLowerCase().trim();
                Polygon poly = (Polygon) placemark.getGeometry();
                Boundary boundary = poly.getOuterBoundaryIs();
                LinearRing linear = boundary.getLinearRing();
                result.put(placesName, GeoUtils.getPolygonAsString(linear.getCoordinates()));
            }
        }
        return result;
    }

}

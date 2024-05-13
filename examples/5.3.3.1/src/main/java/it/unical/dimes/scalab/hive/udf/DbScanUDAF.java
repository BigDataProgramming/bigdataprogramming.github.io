package it.unical.dimes.scalab.hive.udf;

import it.unical.dimes.scalab.dbscan.ElkiDBSCAN;
import it.unical.dimes.scalab.dbscan.GeoCluster;
import it.unical.dimes.scalab.utils.GeoPoint;
import it.unical.dimes.scalab.utils.GeoUtils;
import it.unical.dimes.scalab.utils.KMLUtils;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDAF;
import org.apache.hadoop.hive.ql.exec.UDAFEvaluator;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.locationtech.spatial4j.shape.Shape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


@Description(name = "dbscan",
        value = "_FUNC_(x, eps, minPts) - Returns the largest cluster find by dbscan in a list of points"
)
public class DbScanUDAF extends UDAF {

    public static class DBSCANUDAFEvaluator implements UDAFEvaluator {

        public static class DbscanGeoDataset {
            private List<String> points = new LinkedList<>();
            private double eps = 0;
            private int minPts = 0;
        }


        private DbscanGeoDataset dataset = null;

        public DBSCANUDAFEvaluator() {
            super(); init();
        }

        @Override
        public void init() {
            // Initialize evaluator
            dataset = new DbscanGeoDataset();
        }

        public boolean iterate(double latitude, double longitude,
                               double eps, int minPts) throws HiveException {
            // Iterating over each value for aggregation
            if (dataset == null)
                throw new HiveException("DBSCAN dataset is not initialized");
            if (latitude > 0 && longitude > 0) {
                dataset.points.add(longitude + "," + latitude);
                dataset.eps = eps;
                dataset.minPts = minPts;
                return true;
            } else {
                return false;
            }
        }

        // Called when Hive wants partially aggregated results.
        public DbscanGeoDataset terminatePartial() {
            return dataset;
        }

        // Called when Hive decides to combine one partial aggregation with another
        public boolean merge(DbscanGeoDataset other) throws HiveException {
            // merging by combining partial aggregation
            if (other != null) {
                this.dataset.points.addAll(other.points);
                this.dataset.minPts = other.minPts;
                this.dataset.eps = other.eps;
            }
            return true;
        }

        // Called when the final result of the aggregation needed.
        public String terminate() throws HiveException {
            // At the end of last record of the group - returning final result
            ElkiDBSCAN dbscan = new ElkiDBSCAN(
                    this.dataset.points.stream().map(p -> {
                        String[] data = p.split(",");
                        return new GeoPoint(Double.parseDouble(data[0]),
                                Double.parseDouble(data[1]));
                    }).collect(Collectors.toList()),
                    this.dataset.eps, this.dataset.minPts);
            dbscan.cluster();
            List<GeoCluster> clusters = dbscan.getAllGeoClusters(false);
            int max = -1;
            GeoCluster maxCluster = null;
            for (GeoCluster c : clusters) {
                if (c.getPoints().size() > max) {
                    maxCluster = c;
                    max = c.getPoints().size();
                }
            }
            if (maxCluster != null) {
                try {
                    Shape clusterShape = GeoUtils.convexHull(maxCluster.getPoints());
                    return KMLUtils.serialize(clusterShape);
                } catch (IOException e) {
                    return "ERROR_NOT_FOUND";
                }
            } else return "NOT_FOUND";
        }

    }


}
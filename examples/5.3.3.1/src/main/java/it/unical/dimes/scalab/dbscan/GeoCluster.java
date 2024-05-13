package it.unical.dimes.scalab.dbscan;

import org.locationtech.spatial4j.shape.Point;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class GeoCluster {
    List<Point> points;

    public GeoCluster(List<Point> points) {
        this.points = points;
    }

    public List<Point> getPoints() {
        return points;
    }

    public Set<Point> getDistinctPoints() {
        return points.stream().collect(toSet());
    }
}

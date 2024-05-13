package it.unical.dimes.scalab.utils;

import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.context.jts.JtsSpatialContext;
import org.locationtech.spatial4j.distance.DistanceCalculator;
import org.locationtech.spatial4j.distance.DistanceUtils;
import org.locationtech.spatial4j.distance.GeodesicSphereDistCalc;
import org.locationtech.spatial4j.exception.InvalidShapeException;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.Rectangle;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.*;
import org.locationtech.spatial4j.shape.jts.JtsGeometry;

import java.awt.*;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


public class GeoUtils {

    final static double RAD = DistanceUtils.EARTH_MEAN_RADIUS_KM;
    final static SpatialContext ctx = SpatialContext.GEO;
    final static SpatialContext jtsctx = JtsSpatialContext.GEO;
    final static double SURFACE_AREA_ONE_SQUARE_DEGREE = 12365.1613;
    public static final double STEP_1M_Y = 8.992909382672273E-6;
    //	public static final double STEP_1M_X = 1.2080663828690774E-5;
    public static final double STEP_1M_X = 9.1319418354628E-6; // Valore approssimato per QGIS
    private static Random random = new Random();

    public static double getDistanceMeters(Point a, Point b) {
        DistanceCalculator haversine = new GeodesicSphereDistCalc.Haversine();
        double dist_degree = haversine.distance(a, b);
        double dist_rad = DistanceUtils.toRadians(dist_degree);
        return dist_rad * RAD * 1000;

    }

    public static double getAreaSquaredKm(Shape p) {

        return p.getArea(ctx) * SURFACE_AREA_ONE_SQUARE_DEGREE;
    }

    public static Shape convexHull(Collection<Point> points) {
        GeometryFactory geometryFactory = new GeometryFactory();
        List<Coordinate> coords = new LinkedList<Coordinate>();
        for (Point p : points) {
            coords.add(new Coordinate(p.getX(), p.getY()));
        }
        MultiPoint mp = geometryFactory.createMultiPoint(coords.toArray(new Coordinate[0]));
        Geometry geoHull = mp.convexHull();
        return (Shape) (new JtsGeometry(geoHull, JtsSpatialContext.GEO, false, false));
    }

    public static double getDistanceKM(Point a, Point b) {
        DistanceCalculator haversine = new GeodesicSphereDistCalc.Haversine();
        double dist_degree = haversine.distance(a, b);
        double dist_rad = DistanceUtils.toRadians(dist_degree);
        return dist_rad * RAD;

    }

    public static double distanceRadiant(double[] a, double[] b) {
        DistanceCalculator haversine = new GeodesicSphereDistCalc.Haversine();
        double dist_degree = haversine.distance(getPoint(a[0], a[1]), getPoint(b[0], b[1]));
        double dist_rad = DistanceUtils.toRadians(dist_degree);
        return dist_rad;
    }

    public static double distance(double[] a, double[] b) {
        return distanceRadiant(a,b) * RAD * 1000.0d;
    }

    public static double distance(Point a, Point b) {
        DistanceCalculator haversine = new GeodesicSphereDistCalc.Haversine();
        double dist_degree = haversine.distance(a, b);
        double dist_rad = DistanceUtils.toRadians(dist_degree);
        return dist_rad * RAD * 1000;
    }

    public static double degreeToKM(double degrees) {
        double dist_rad = DistanceUtils.toRadians(degrees);
        return dist_rad * RAD;
    }

    public static double degreeToMeters(double degrees) {
        double dist_rad = DistanceUtils.toRadians(degrees);
        return dist_rad * RAD * 1000;
    }

    public static double metersToDegree(double meters) {
        double dist_rad = meters / (RAD * 1000);
        return DistanceUtils.toDegrees(dist_rad);
    }

    public static Circle getCircle(Point p, double radiusInMeters) {
        return ctx.getShapeFactory().circle(p, GeoUtils.metersToDegree(radiusInMeters));
    }

    public static Rectangle getRectangle(Point lowerLeft, Point upperRight) {
        return ctx.getShapeFactory().rect(lowerLeft, upperRight);
    }

    public static Shape getLineString(Point... points) {
        String pointString = "";
        for (Point point : points) {
            pointString += "," + point.getY() + " " + point.getX();
        }
        return jtsctx.readShape("LINESTRING (" + pointString.substring(1) + ")");
    }

    public static Shape getLineString(String pointString) {
        return jtsctx.readShape("LINESTRING (" + pointString + ")");
    }

    public static Shape getPolygon(Point... points)
            throws InvalidShapeException, IOException, java.text.ParseException {
        String tmp = "[[";
        for (Point point : points) {
            tmp += "[" + point.getX() + "," + point.getY() + "],";
        }
        tmp = tmp.substring(0, tmp.length() - 1);
        tmp += "]]";
        String shapeString = "{'type':'Polygon','coordinates':" + tmp + "}";

        return jtsctx.readShape(shapeString);
    }

    public static Shape getPolygon(boolean close, Point... points)
            throws InvalidShapeException, IOException, java.text.ParseException {
        String tmp = "[[";
        for (Point point : points) {
            tmp += "[" + point.getX() + "," + point.getY() + "],";
        }
        if (close) {
            tmp += "[" + points[0].getX() + "," + points[0].getY() + "],";
        }
        tmp = tmp.substring(0, tmp.length() - 1);
        tmp += "]]";
        String shapeString = "{'type':'Polygon','coordinates':" + tmp + "}";

        return jtsctx.readShape(shapeString);
    }

    public static Shape getPolygon(boolean close, Coordinate... points)
            throws InvalidShapeException, IOException, java.text.ParseException {
        String tmp = "[[";
        for (Coordinate point : points) {
            tmp += "[" + point.x + "," + point.y + "],";
        }
        if (close) {
            tmp += "[" + points[0].x + "," + points[0].y + "],";
        }
        tmp = tmp.substring(0, tmp.length() - 1);
        tmp += "]]";
        String shapeString = "{'type':'Polygon','coordinates':" + tmp + "}";

        return jtsctx.readShape(shapeString);
    }

    public static Shape getPolygon(boolean close, de.micromata.opengis.kml.v_2_2_0.Coordinate... points)
            throws InvalidShapeException, IOException, java.text.ParseException {
        String tmp = "[[";
        for (de.micromata.opengis.kml.v_2_2_0.Coordinate point : points) {
            tmp += "[" + point.getLongitude() + "," + point.getLatitude() + "],";
        }
        if (close) {
            tmp += "[" + points[0].getLongitude() + "," + points[0].getLatitude() + "],";
        }
        tmp = tmp.substring(0, tmp.length() - 1);
        tmp += "]]";
        String shapeString = "{'type':'Polygon','coordinates':" + tmp + "}";

        return jtsctx.readShape(shapeString);
    }


    public static Shape getPolygon(boolean close, Collection<Point> points) {
        String tmp = "[[";
        Point pFirst = null;
        for (Point point : points) {
            if (pFirst == null) pFirst = point;
            tmp += "[" + point.getX() + "," + point.getY() + "],";
        }
        if (close) {
            tmp += "[" + pFirst.getX() + "," + pFirst.getY() + "],";
        }
        tmp = tmp.substring(0, tmp.length() - 1);
        tmp += "]]";
        String shapeString = "{'type':'Polygon','coordinates':" + tmp + "}";
        try {
            return jtsctx.readShape(shapeString);
        } catch (Exception e) {
            return null;
        }
    }

    public static Shape getPolygonOpenGISCoordinate(boolean close, Collection<de.micromata.opengis.kml.v_2_2_0.Coordinate> points) {
        String tmp = "[[";
        de.micromata.opengis.kml.v_2_2_0.Coordinate pFirst = null;
        for (de.micromata.opengis.kml.v_2_2_0.Coordinate point : points) {
            if (pFirst == null) pFirst = point;
            tmp += "[" + point.getLongitude() + "," + point.getLatitude() + "],";
        }
        if (close) {
            tmp += "[" + pFirst.getLongitude() + "," + pFirst.getLatitude() + "],";
        }
        tmp = tmp.substring(0, tmp.length() - 1);
        tmp += "]]";
        String shapeString = "{'type':'Polygon','coordinates':" + tmp + "}";
        try {
            return jtsctx.readShape(shapeString);
        } catch (Exception e) {
            return null;
        }
    }

    public static Shape getPolygon(String polygon) throws InvalidShapeException, IOException, java.text.ParseException {
        return jtsctx.readShape(polygon);
    }

    public static boolean isContained(Shape inner, Shape container) {
        if (container.equals(inner))
            return true;
        if (container.relate(inner) == SpatialRelation.CONTAINS)
            return true;
        return false;
    }

    public static boolean isWithin(Shape inner, Shape container) {
        if (container.equals(inner))
            return true;
        if (container.relate(inner) == SpatialRelation.WITHIN)
            return true;
        return false;
    }

    public static SpatialContext getSpatialContext() {
        return ctx;
    }

    public static Point getPoint(String pointString, String separator, boolean isLatLon) {
        String[] values = pointString.split(separator);
        if (isLatLon)
            return ctx.getShapeFactory().pointXY(Double.parseDouble(values[1]), Double.parseDouble(values[0]));
        else
            return ctx.getShapeFactory().pointXY(Double.parseDouble(values[0]), Double.parseDouble(values[1]));

    }

    public static Point getPoint(double longitude, double latitude) {
        return ctx.getShapeFactory().pointXY(longitude, latitude);
    }

    public static Color randomColor() {
        Random random = new Random(); // Probably really put this somewhere
        // where it gets executed only once
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);
        return new Color(red, green, blue, 120);
    }

    public static Point getRandomPointInsideCircle(Circle c, Random rand) {
        double r = c.getRadius() * Math.sqrt(rand.nextDouble());
        double theta = rand.nextDouble() * 2 * Math.PI;
        double x = c.getCenter().getX() + r * Math.cos(theta);
        double y = c.getCenter().getY() + r * Math.sin(theta);
        return getPoint(x, y);
    }

    public static Point getRandomPointInsideRectangle(Rectangle r, Random rand) {
        double x = rand.nextDouble() * (r.getMaxX() - r.getMinX()) + r.getMinX();
        double y = rand.nextDouble() * (r.getMaxY() - r.getMinY()) + r.getMinY();
        return getPoint(x, y);
    }

    public static Point getRandomPointInsideShape(Shape s, Random rand) {
        if (s instanceof Circle)
            return getRandomPointInsideCircle((Circle) s, rand);
        else if (s instanceof Rectangle)
            return getRandomPointInsideRectangle((Rectangle) s, rand);
        else return null;
    }

    public static Point getRandomPointInsideShape(Shape s) {
        if (s instanceof Circle)
            return getRandomPointInsideCircle((Circle) s, random);
        else if (s instanceof Rectangle)
            return getRandomPointInsideRectangle((Rectangle) s, random);
        else return null;
    }
}

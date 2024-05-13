package it.unical.dimes.scalab.utils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.Rectangle;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.impl.GeoCircle;
import org.locationtech.spatial4j.shape.jts.JtsGeometry;

import java.io.IOException;
import java.util.*;

public class KMLUtils {

    public final static String OPEN_TAGS = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><kml xmlns=\"http://www.opengis.net/kml/2.2\"><Document>";
    public final static String CLOSE_TAGS = "</Document></kml>";

    public static String serialize(Shape shape) throws IOException {
        return serialize(shape, false, null);
    }

    public static String serialize(Shape shape, boolean closeFile, Map<String, String> ext) throws IOException {
        if (shape instanceof JtsGeometry) {
            Geometry geometry = ((JtsGeometry) shape).getGeom();
            if (geometry instanceof Point) {
                return serializePoint((Point) geometry, closeFile, ext);
            } else if (geometry instanceof org.locationtech.jts.geom.Point) {
                return serializeGeometryPoint((org.locationtech.jts.geom.Point) geometry, closeFile, ext);
            } else if (geometry instanceof Polygon) {
                return serializePolygon((Polygon) geometry, closeFile, ext);
            } else {
                throw new IllegalArgumentException("Geometry type [" + geometry.getGeometryType() + "] not supported");
            }
        } else if (shape instanceof Point) {
            return serializePoint((Point) shape, closeFile, ext);
        } else if (shape instanceof Rectangle) {
            return serializeRectangle((Rectangle) shape, closeFile, ext);
        } else if (shape instanceof GeoCircle) {
            return serializeCircle((GeoCircle) shape, closeFile, ext);
        } else {
            throw new IllegalArgumentException("Shape type [" + shape.getClass().getSimpleName() + "] not supported");
        }
    }

    private static String serializeGeometryPoint(org.locationtech.jts.geom.Point geometry, boolean closeFile, Map<String, String> extendedData) {
        StringBuilder sb = new StringBuilder();
        if (closeFile) {
            sb.append(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?><kml xmlns=\"http://www.opengis.net/kml/2.2\"><Document>");
        }
        HashMap<String, String> ext = generateExtendedDataString(extendedData);
        sb.append("<Placemark>" + ext.get("data") + "<Point><coordinates>" + geometry.getX() + "," + geometry.getY()
                + "</coordinates></Point></Placemark>");
        if (closeFile) {
            sb.append("</Document></kml>");
        }
        return sb.toString();
    }

    private static String serializeCircle(GeoCircle shape, boolean closeFile, Map<String, String> extendedData) {
        StringBuilder sb = new StringBuilder();
        if (closeFile) {
            sb.append(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?><kml xmlns=\"http://www.opengis.net/kml/2.2\"><Document>");
        }

        double centerLat = shape.getCenter().getY();
        double centerLng = shape.getCenter().getX();
        double radius = GeoUtils.degreeToMeters(shape.getRadius()); // diameter of circle in km
        // start generating KML
        HashMap<String, String> ext = generateExtendedDataString(extendedData);
        sb.append("<Placemark>" + ext.get("data") + "<Polygon><outerBoundaryIs><LinearRing><tessellate>1</tessellate><coordinates>");

        for (int x = 0; x <= 360; x++) {
            double latitude = centerLat + GeoUtils.STEP_1M_Y * radius * Math.cos(Math.toRadians(x));
            double longitude = centerLng + GeoUtils.STEP_1M_X * radius * Math.sin(Math.toRadians(x));
            sb.append(longitude + "," + latitude + " ");
        }
        sb.append("</coordinates></LinearRing></outerBoundaryIs></Polygon></Placemark>");
        if (ext.containsKey("style"))
            sb.append(ext.get("style"));

        if (closeFile) {
            sb.append("</Document></kml>");
        }

        return sb.toString();
    }

    private static String serializeRectangle(Rectangle rectangle, boolean closeFile, Map<String, String> extendedData) {
        StringBuilder sb = new StringBuilder();

        if (closeFile) {
            sb.append(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?><kml xmlns=\"http://www.opengis.net/kml/2.2\"><Document>");
        }

        double[][] points = new double[4][2];
        points[0][0] = rectangle.getMinX();
        points[0][1] = rectangle.getMinY();
        points[1][0] = rectangle.getMaxX();
        points[1][1] = rectangle.getMaxY();
        points[2][0] = rectangle.getMinX();
        points[2][1] = rectangle.getMaxY();
        points[3][0] = rectangle.getMaxX();
        points[3][1] = rectangle.getMinY();

        HashMap<String, String> ext = generateExtendedDataString(extendedData);
        sb.append("<Placemark>" + ext.get("data") + "<Polygon><outerBoundaryIs><LinearRing><coordinates>");
        sb.append(points[0][0] + "," + points[0][1] + " ");
        sb.append(points[3][0] + "," + points[3][1] + " ");
        sb.append(points[1][0] + "," + points[1][1] + " ");
        sb.append(points[2][0] + "," + points[2][1] + " ");
        sb.append("</coordinates></LinearRing></outerBoundaryIs></Polygon></Placemark>");
        if (ext.containsKey("style"))
            sb.append(ext.get("style"));

        if (closeFile) {
            sb.append("</Document></kml>");
        }

        return sb.toString();

    }

    private static String serializePolygon(Polygon geometry, boolean closeFile, Map<String, String> extendedData) {

        StringBuilder sb = new StringBuilder();

        if (closeFile) {
            sb.append(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?><kml xmlns=\"http://www.opengis.net/kml/2.2\"><Document>");
        }

        HashMap<String, String> ext = generateExtendedDataString(extendedData);
        sb.append("<Placemark>" + ext.get("data")
                + "<Polygon><outerBoundaryIs><LinearRing><tessellate>0</tessellate><coordinates>");
        Coordinate[] coordinates = geometry.getCoordinates();
        List<Coordinate> pointsList = new LinkedList<Coordinate>();

        double sumLat = 0;
        double sumLng = 0;
        for (Coordinate coordinate : coordinates) {
            pointsList.add(coordinate);
            sumLat += coordinate.y;
            sumLng += coordinate.x;
        }
        Coordinate reference = new Coordinate(sumLng / coordinates.length, sumLat / coordinates.length);
        Collections.sort(pointsList, new ClockwiseCoordinateComparator(reference));
        Coordinate tmp = pointsList.remove(coordinates.length - 1);
        pointsList.add(0, tmp);
        for (Coordinate c : pointsList) {
            sb.append(c.x + "," + c.y + ",0.0 ");
        }
        sb.append("</coordinates></LinearRing></outerBoundaryIs></Polygon></Placemark>");

        sb.append(ext.get("style"));

        if (closeFile) {
            sb.append("</Document></kml>");
        }

        return sb.toString();
    }

    private static HashMap<String, String> serializeLineString(LineString geometry) {
        StringBuilder sb = new StringBuilder();
        HashMap<String, String> ret = new HashMap<String, String>();
        sb.append("<Placemark><LineString><coordinates>");
        double[][] coordinates = getCoordinateMatrix(geometry);
        for (double[] ds : coordinates) {
            sb.append(ds[0] + "," + ds[1] + " ");
        }
        sb.append("</coordinates></LineString>");
        sb.append("</Placemark>");
        ret.put("placemark", sb.toString());
        return ret;

    }

    private static String serializePoint(Point geometry, boolean closeFile, Map<String, String> extendedData) {
        StringBuilder sb = new StringBuilder();
        if (closeFile) {
            sb.append(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?><kml xmlns=\"http://www.opengis.net/kml/2.2\"><Document>");
        }
        HashMap<String, String> ext = generateExtendedDataString(extendedData);
        sb.append("<Placemark>" + ext.get("data") + "<Point><coordinates>" + geometry.getX() + "," + geometry.getY()
                + "</coordinates></Point></Placemark>");
        if (closeFile) {
            sb.append("</Document></kml>");
        }
        return sb.toString();
    }

    private static HashMap<String, String> generateExtendedDataString(Map<String, String> extendedData) {
        String ext = "";
        String preText = "";
        HashMap<String, String> ret = new HashMap<String, String>();
        if (extendedData != null && extendedData.size() > 0) {
            ext = "<ExtendedData>";
            for (Map.Entry<String, String> entry : extendedData.entrySet()) {
                if (entry.getKey().equals("styleUrl") || entry.getKey().equals("description")) {
                    preText += "<styleUrl>" + entry.getValue().trim() + "</styleUrl>";
                } else if (entry.getKey().equals("color")) {
                    preText += "<styleUrl>#poly-" + entry.getValue().trim() + "</styleUrl>";
                    String style = "<Style id=\"poly-" + entry.getValue().trim() + "\">" + "<LineStyle>" + "<color>"
                            + entry.getValue().trim() + "</color>" + "	<width>2</width>" + "</LineStyle>"
                            + "<PolyStyle>" + "<color>" + entry.getValue().trim() + "</color>" + "	<fill>1</fill>"
                            + "<outline>1</outline>" + "</PolyStyle></Style>";
                    ret.put("style", style);

                } else if (entry.getKey().equals("description")) {
                    preText += "<description><![CDATA[descrizione:" + entry.getValue() + "]]></description>";
                } else if (entry.getKey().equals("name")) {
                    preText += "<name>" + entry.getValue() + "</name>";
                } else {
                    ext += "<Data name=\"" + entry.getKey() + "\"><value>" + entry.getValue() + "</value></Data>";
                }
            }
            ext += "</ExtendedData>";
        }
        ret.put("data", preText + ext);
        return ret;
    }

    public static double[][] getCoordinateMatrix(Geometry geometry) {
        Coordinate[] points = geometry.getCoordinates();

        List<Coordinate> pointsList = new LinkedList<Coordinate>();
        double sumLat = 0;
        double sumLng = 0;
        for (Coordinate coordinate : points) {
            pointsList.add(coordinate);
            sumLat += coordinate.y;
            sumLng += coordinate.x;
        }

        Coordinate reference = new Coordinate(sumLng / points.length, sumLat / points.length);
        Collections.sort(pointsList, new ClockwiseCoordinateComparator(reference));
        Coordinate tmp = pointsList.remove(points.length - 1);
        pointsList.add(0, tmp);

        // Create the coordinate's matrix
        double[][] coordinates = new double[points.length][2];
        for (int i = 0; i < points.length; i++) {
            tmp = pointsList.get(i);
            coordinates[i][0] = tmp.x;
            coordinates[i][1] = tmp.y;
        }

        return coordinates;
    }

    public static double[][][] getCoordinateMatrixPolygon(Geometry geometry) {
        Coordinate[] points = geometry.getCoordinates();

        List<Coordinate> pointsList = new LinkedList<Coordinate>();
        double sumLat = 0;
        double sumLng = 0;
        for (Coordinate coordinate : points) {
            pointsList.add(coordinate);
            sumLat += coordinate.y;
            sumLng += coordinate.x;
        }

        Coordinate reference = new Coordinate(sumLng / points.length, sumLat / points.length);

        Collections.sort(pointsList, new ClockwiseCoordinateComparator(reference));

        Coordinate tmp = pointsList.remove(0);
        pointsList.add(tmp);

        // Create the coordinate's matrix
        double[][][] coordinates = new double[1][points.length][2];
        for (int i = 0; i < points.length; i++) {
            tmp = pointsList.get(i);
            coordinates[0][i][0] = tmp.x;
            coordinates[0][i][1] = tmp.y;
        }

        return coordinates;
    }

}

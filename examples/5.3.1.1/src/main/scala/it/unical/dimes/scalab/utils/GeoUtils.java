package it.unical.dimes.scalab.utils;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;

import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.shape.Circle;
import com.spatial4j.core.shape.Point;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.SpatialRelation;

public class GeoUtils {

    final static double RAD = DistanceUtils.EARTH_MEAN_RADIUS_KM;
    final static SpatialContext ctx = SpatialContext.GEO;
    final static SpatialContext jtsctx = JtsSpatialContext.GEO;

    public static String getPolygonAsString(Collection<Coordinate> points) {
        StringBuilder tmp = new StringBuilder();
        Coordinate pFirst = null;
        Coordinate pLast = null;
        for (Coordinate point : points) {
            if (pFirst == null) pFirst = point;
            tmp.append(",").append(point.getLongitude()).append(" ").append(point.getLatitude());
            pLast = point;
        }
        if (pFirst != null && !pFirst.equals(pLast))
            tmp.append(",").append(pFirst.getLongitude()).append(" ").append(pFirst.getLatitude());
        tmp.append(")");
        return "POLYGON((" + tmp.substring(1) + ")";
    }


    public static Shape getPolygonFromString(String shapeString) throws IOException, ParseException {
        return jtsctx.getFormats().getWktReader().read(shapeString);
    }


    public static boolean isContained(Shape inner, Shape container) {
        if (container.equals(inner))
            return true;
        return container.relate(inner) == SpatialRelation.CONTAINS;
    }

    public static double metersToDegree(double meters) {
        double dist_rad = meters / (RAD * 1000);
        return DistanceUtils.toDegrees(dist_rad);
    }

    public static Circle getCircle(Shape s, double radiusInMeters) {
        return ctx.makeCircle((Point)s, GeoUtils.metersToDegree(radiusInMeters));
    }


    public static Shape getPoint(double longitude, double latitude) {
        return ctx.makePoint(longitude, latitude);
    }


}


package it.unical.dimes.scalab.utils;

import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.shape.impl.PointImpl;

import java.io.Serializable;

public class GeoPoint extends PointImpl implements Serializable {

    public GeoPoint() {
        super(0, 0, SpatialContext.GEO);
    }
    public GeoPoint(double x, double y) {
        super(x, y, SpatialContext.GEO);
    }
}

package it.unical.dimes.scalab.utils;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import de.micromata.opengis.kml.v_2_2_0.Boundary;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Polygon;

public class KMLUtils {

	public static Map<String, String> lookupFromKml(String kmlString) {
		Map<String, String> result = new HashMap<>();
		Kml kmlContent = Kml.unmarshal(kmlString);
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

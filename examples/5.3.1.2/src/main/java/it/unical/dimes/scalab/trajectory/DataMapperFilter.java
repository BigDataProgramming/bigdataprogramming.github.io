package it.unical.dimes.scalab.trajectory;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.Configuration;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Shape;
import it.unical.dimes.scalab.utils.GeoUtils;
import it.unical.dimes.scalab.utils.Flickr;
import it.unical.dimes.scalab.utils.KMLUtils;


public class DataMapperFilter extends Mapper<LongWritable, Text, Text, Text> {

	private Shape romeShape;
	private Map<String, String> shapeMap;
	private String kmlPath;

	private Text outputKey = new Text();
	private Text outputValue = new Text();

	private static int count = 0;

	@Override
	protected void setup(Mapper<LongWritable, Text, Text, Text>.Context context)
			throws IOException, InterruptedException {
		romeShape = GeoUtils.getCircle(GeoUtils.getPoint(12.492373, 41.890251), 10000);
		Configuration conf = context.getConfiguration();
		kmlPath = conf.get("kmlFile");
		FileSystem fs = FileSystem.get(conf);
		Path hdfsreadpath = new Path(kmlPath);
		FSDataInputStream inputStream = fs.open(hdfsreadpath);
		String kmlString = IOUtils.toString(inputStream, "UTF-8");
		inputStream.close();
		shapeMap = KMLUtils.lookupFromKml(kmlString);
	}

	public void map(LongWritable key, Text value, Context context) throws InterruptedException, IOException {
		Flickr f = new Flickr();
		f.importFromFlickrJSON(value.toString());

		if (!(filterIsGPSValid(f) && filterIsInRome(f)))
			return;

		Shape point = GeoUtils.getPoint(f.getLongitude(), f.getLatitude());
		boolean found = false;
		for (Entry<String, String> entry : shapeMap.entrySet()) {
			String place = entry.getKey();
			String polygon = entry.getValue();
			try {
				Shape pol = GeoUtils.getPolygonFromString(polygon);
				if (GeoUtils.isContained(point, pol)) {
					f.setRoi(place);
					found = true;
				}
			} catch (IOException | ParseException e) {
				e.printStackTrace();
			}

		}

		if (found) {
			outputKey.set(f.getUserId());
			// Serialize value in JSON format
			outputValue.set(f.export());
			context.write(outputKey, outputValue);
		}

	}

	private boolean filterIsGPSValid(Flickr f) {
		return f.getLongitude() > 0 && f.getLatitude() > 0;
	}

	private boolean filterIsInRome(Flickr f) {
		Point p = (Point) GeoUtils.getPoint(f.getLongitude(), f.getLatitude());
		return GeoUtils.isContained(p, romeShape);
	}
}

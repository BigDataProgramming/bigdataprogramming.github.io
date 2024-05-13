package it.unical.dimes.scalab.trajectory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.joda.time.Days;
import org.joda.time.LocalDateTime;
import org.joda.time.Seconds;
import it.unical.dimes.scalab.utils.Flickr;


public class DataReducerByDay extends Reducer<Text, Text, NullWritable, Text> {

	private Text outputValue = new Text();

	@Override
	public void reduce(Text key, Iterable<Text> values, Context context)
			throws java.io.IOException, InterruptedException {

		List<String> res = concatenateLocationsByDay(values);

		for (String s : res) {
			outputValue.set(s);
			context.write(NullWritable.get(), outputValue);
		}
	}

	private static List<String> concatenateLocationsByDay(Iterable<Text> listItems) {
		LocalDateTime oldTimestamp = new LocalDateTime(0);
		LocalDateTime currTimestamp = null;
		List<String> ret = new LinkedList<String>();
		Set<String> s = null;
		String oldLocation = null;
		String currentLocation = null;

		List<Flickr> lf = new LinkedList<Flickr>();

		for (Text value : listItems) {
			Flickr item = new Flickr();
			item.importFromString(value.toString());
			item.removeTime();
			lf.add(item);
		}

		lf.sort(new Comparator<Flickr>() {

			@Override
			public int compare(Flickr f1, Flickr f2) {
				return (f1.getDate().compareTo(f2.getDate()));
			}

		});
		for (Flickr f : lf) {
			System.out.println(f);
			currTimestamp = f.getDate();
			if (Days.daysBetween(oldTimestamp, currTimestamp).getDays() > 0) {
				if (s != null) {
					ret.add(s.toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(", ", " ").trim());
				}
				s = new HashSet<String>();
				oldLocation = null;
				oldTimestamp = currTimestamp;
			}
			currentLocation = f.getRoi();
			if (!currentLocation.equals(oldLocation)) {
				s.add(currentLocation);
				oldLocation = currentLocation;
			}
		}
		if (s != null) {
			ret.add(s.toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(", ", " ").trim());
		}

		return ret;
	}
}
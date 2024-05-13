package it.unical.dimes.scalab.trajectory;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import it.unical.dimes.scalab.utils.GeoUtils;
import it.unical.dimes.scalab.utils.KMLUtils;
import FPGrowth.main.FpGrowth;

public class Main extends Configured implements Tool {

	private static String inputPath;
	private static String trajOutputPath;
	private static String fpOutputPath;
	private static int minimumSupport;
	private static int maxPatterns;

	@Override
	public int run(String[] args) throws Exception {

		inputPath = args[0];
		trajOutputPath = args[1];
		fpOutputPath = args[2];
		minimumSupport = Integer.valueOf(args[3]);
		maxPatterns = Integer.valueOf(args[4]);

		Configuration conf = new Configuration();
		conf.set("kmlFile", args[5]);

		/** Trajectory Job configuration */
		Job traJob = Job.getInstance(conf, "TrajectoryJob");
		traJob.setJarByClass(Main.class);

		/* FileSystem setting */
		FileInputFormat.addInputPaths(traJob, inputPath);
		FileOutputFormat.setOutputPath(traJob, new Path(trajOutputPath));
		FileSystem fs = FileSystem.get(conf);
		if (fs.exists(new Path(trajOutputPath)))
			fs.delete(new Path(trajOutputPath), true);

		traJob.setMapperClass(DataMapperFilter.class);
		// Set the output class of key and value for the mapper
		traJob.setMapOutputKeyClass(Text.class);
		traJob.setMapOutputValueClass(Text.class);

		traJob.setReducerClass(DataReducerByDay.class);
		// Set the output class of key and value for the reducer
		traJob.setOutputKeyClass(Text.class);
		traJob.setOutputValueClass(Text.class);

		boolean completed;
		completed = traJob.waitForCompletion(true);
		if (!completed)
			return 1;

		/** Run FPGrowth algorithm */
		if (fs.exists(new Path(fpOutputPath)))
			fs.delete(new Path(fpOutputPath), true);

		FpGrowth.runFpGrowth(trajOutputPath, fpOutputPath, minimumSupport, maxPatterns);
		return 1;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new Main(), args);
		System.exit(res);
	}
}

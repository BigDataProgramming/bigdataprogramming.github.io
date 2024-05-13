package it.unical.dimes.scalab.invertedIndex.hadoop;

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


public class InvertedIndexJob extends Configured implements Tool {

    @Override
    public int run(String[] args) throws Exception {
        String inputPath = args[0];
        String outputPath = args[1];

        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);
        if (fs.exists(new Path(outputPath)))
            fs.delete(new Path(outputPath), true);

        Job job = Job.getInstance(conf, "invertedIndex");
        job.setJarByClass(InvertedIndexJob.class);
        job.setMapperClass(MapTask.class);

        job.setCombinerClass(CombineTask.class);
        job.setReducerClass(ReduceTask.class);

        // Set the output class of key and value for the mapper
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        // Set the output class of key and value for the reducer
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        // Specify input and output paths
        FileInputFormat.addInputPaths(job, inputPath);
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        int exit = job.waitForCompletion(true) ? 0 : 1;
        return exit;
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new InvertedIndexJob(), args);
        System.exit(res);
    }

}

package it.unical.dimes.scalab.invertedIndex.hadoop;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

public class MapTask extends Mapper<Object, Text, Text, Text> {

    private final Text keyContent = new Text();
    private final Text valueContent = new Text();
    private final static IntWritable one = new IntWritable(1);

    @Override
    protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        // Extract the filename from the current input split
        FileSplit fileSplit = (FileSplit) context.getInputSplit();
        String filename = fileSplit.getPath().getName();
        StringTokenizer it = new StringTokenizer(value.toString());

        while (it.hasMoreTokens()) {
            // Remove punctuation, apply lemmatization and stemming
            String word = process(it.nextToken());
            keyContent.set(word);
            valueContent.set(filename + ":" + one);
            context.write(keyContent, valueContent);
        }
    }

    private String process(String token) {
        return token.replaceAll("\\p{Punct}", "").toLowerCase();
    }
}

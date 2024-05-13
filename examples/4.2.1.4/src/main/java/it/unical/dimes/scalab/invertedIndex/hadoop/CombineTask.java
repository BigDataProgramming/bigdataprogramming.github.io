package it.unical.dimes.scalab.invertedIndex.hadoop;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CombineTask extends Reducer<Text, Text, Text, Text> {
    private final Text sumContent = new Text();

    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context)
            throws IOException, InterruptedException {
        // Sum all the occurrences of a word in the document
        HashMap<String, Integer> sumMap = new HashMap<>();
        for (Text value : values) {
            String[] parts = value.toString().split(":");
            sumMap.merge(parts[0], 1, Integer::sum);
        }
        for (Map.Entry<String, Integer> e : sumMap.entrySet()) {
            sumContent.set(e.getKey() + ":" + e.getValue());
            context.write(key, sumContent);
        }
    }
}

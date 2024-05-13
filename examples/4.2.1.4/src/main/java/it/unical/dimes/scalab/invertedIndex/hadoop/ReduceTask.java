package it.unical.dimes.scalab.invertedIndex.hadoop;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class ReduceTask extends Reducer<Text, Text, Text, Text> {

    private final Text result = new Text();

    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        StringBuilder fileList = new StringBuilder();
        HashMap<String, Integer> sumMap = new HashMap<>();
        for (Text value : values) {
            String[] parts = value.toString().split(":");
            sumMap.merge(parts[0], Integer.parseInt(parts[1]), Integer::sum);
        }
        for (Map.Entry<String, Integer> e : sumMap.entrySet()) {
            fileList.append(e.getKey()+":"+e.getValue()).append(";");
        }
        result.set(fileList.toString());
        context.write(key, result);
    }
}

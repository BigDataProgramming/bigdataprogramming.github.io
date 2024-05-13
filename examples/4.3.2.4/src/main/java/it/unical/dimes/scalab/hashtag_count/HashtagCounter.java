package it.unical.dimes.scalab.hashtag_count;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import it.unical.dimes.scalab.hashtag_count.utils.BaseRichBolt;

public class HashtagCounter extends BaseRichBolt {
    Map<String, Integer> _counts;
    OutputCollector _collector;
    String outputPath = "/opt/examples/4.3.2.4/";

    @Override
    public void prepare(Map conf, TopologyContext context,
                        OutputCollector collector) {
        _counts = new HashMap<>();
        _collector = collector;
    }
    @Override
    public void execute(Tuple tuple) {
        String hashtag = tuple.getString(0).toLowerCase();
        Integer count = _counts.get(hashtag);
        if (count == null)
            count = 0;
        count++;
        _counts.put(hashtag, count);
        _collector.ack(tuple);
    }

    @Override
    public void cleanup() {
        try (FileWriter writer = new FileWriter(outputPath + "hashtag_counts.txt")) {
            for (Map.Entry<String, Integer> entry : _counts.entrySet()) {
                writer.write(entry.getKey() + " : " + entry.getValue() + "\n");
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer
                                            outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("hashtag"));
    }
}
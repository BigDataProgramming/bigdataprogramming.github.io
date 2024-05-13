package it.unical.dimes.scalab.hashtag_count;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichSpout;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;
import it.unical.dimes.scalab.hashtag_count.utils.BaseRichSpout;

public class TweetSpout extends BaseRichSpout {

    private SpoutOutputCollector _collector;
    private String filePath = "/opt/examples/4.3.2.4/data/Twitter_dataset_USA2020.csv";
    private BufferedReader reader;

    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        _collector = spoutOutputCollector;

        try {
            this.reader = new BufferedReader(new FileReader(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void nextTuple() {
        String tweet;
        try {
            while ((tweet = reader.readLine()) != null) {
                _collector.emit(new Values(tweet));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("tweet"));
    }
}

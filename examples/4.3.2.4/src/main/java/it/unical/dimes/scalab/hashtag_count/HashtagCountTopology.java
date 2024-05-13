package it.unical.dimes.scalab.hashtag_count;

import org.apache.storm.Config;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.apache.storm.utils.Utils;
import org.apache.storm.StormSubmitter;

public class HashtagCountTopology {
    public static void main(String[] args) throws Exception{
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("spout", new TweetSpout(), 1);
        builder.setBolt("split", new SplitHashtag(), 3).
                shuffleGrouping("spout");
        builder.setBolt("count", new HashtagCounter(), 10).
                fieldsGrouping("split", new Fields("hashtag"));
        Config conf = new Config();
        String topologyName = "HashtagCountTopology";
        StormSubmitter.submitTopology(topologyName, conf, builder.createTopology());

    }
}

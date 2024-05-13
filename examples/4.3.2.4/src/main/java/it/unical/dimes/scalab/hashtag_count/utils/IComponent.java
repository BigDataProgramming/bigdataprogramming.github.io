package it.unical.dimes.scalab.hashtag_count.utils;

import java.io.Serializable;
import java.util.Map;
import org.apache.storm.topology.OutputFieldsDeclarer;

public interface IComponent extends Serializable {
    void declareOutputFields(OutputFieldsDeclarer declarer);

    Map<String, Object> getComponentConfiguration();

}
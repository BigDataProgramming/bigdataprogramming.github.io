package it.unical.dimes.scalab.hashtag_count;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import java.util.StringTokenizer;
import java.util.Map;
import it.unical.dimes.scalab.hashtag_count.utils.BaseRichBolt;

public class SplitHashtag extends BaseRichBolt {
    private OutputCollector _collector;

    @Override
    public void prepare(Map<String, Object> map, TopologyContext topologyContext, OutputCollector outputCollector) {
        _collector = outputCollector;
    }

    public void execute(Tuple tuple) {
        String tweet = tuple.getString(0);
        StringTokenizer st = new StringTokenizer(tweet);
        while (st.hasMoreElements()) {
            String tmp = (String) st.nextElement();
            if (tmp.startsWith("#")) {
                _collector.emit(new Values(tmp));
            }
        }
        _collector.ack(tuple);
    }

    @Override
    public void declareOutputFields
            (OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("hashtag"));
    }
}


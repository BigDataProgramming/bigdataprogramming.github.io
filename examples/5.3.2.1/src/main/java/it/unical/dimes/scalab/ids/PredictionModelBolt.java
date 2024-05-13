package it.unical.dimes.scalab.ids;

import org.apache.storm.task.ShellBolt;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;

import java.util.Map;

public class PredictionModelBolt extends ShellBolt implements IRichBolt {

    public PredictionModelBolt() {
        super("python", "/opt/examples/5.3.2.1/python/modelBolt.py");
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("prediction"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}

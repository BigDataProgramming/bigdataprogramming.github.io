package it.unical.dimes.scalab.ids;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.utils.Utils;
import org.apache.storm.thrift.TException;

import java.lang.Exception;
import java.util.HashMap;
import java.util.Map;

public class IntrusionTopology {

    public static void main(String[] args) throws TException, Exception {

        Config conf = new Config();
        TopologyBuilder builder = new TopologyBuilder();
        DataPreprocessingBolt dataPreprocessingBolt = new DataPreprocessingBolt();
        PredictionModelBolt dataPredictionModelBolt = new PredictionModelBolt();

        builder.setSpout("spout", new ConnectionSpout(), 1);
        builder.setBolt("process", dataPreprocessingBolt, 2).shuffleGrouping("spout");
        builder.setBolt("model", new PredictionModelBolt(), 2).shuffleGrouping("process");

        conf.setDebug(true);
        conf.setNumWorkers(4);

        StormSubmitter.submitTopology("IntrusionDetection", conf, builder.createTopology());
    }
}

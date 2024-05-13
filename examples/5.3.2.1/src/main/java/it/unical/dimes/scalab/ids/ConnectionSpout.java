package it.unical.dimes.scalab.ids;

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

public class ConnectionSpout implements IRichSpout {

    String dataSource = "/opt/examples/5.3.2.1/data/kddcup_test_sample_10.csv";
    public ConnectionSpout() {
    }
    public ConnectionSpout(String dataSource) {
        this.dataSource = dataSource;
    }

    private SpoutOutputCollector collector;
    private FileReader fileReader;
    private boolean read = false;

    private final String[] field_names = new String[]{"duration", "protocol_type", "service", "flag", "src_bytes", "dst_bytes", "land", "wrong_fragment", "urgent", "hot", "num_failed_logins", "logged_in",
            "num_compromised", "root_shell", "su_attempted", "num_root", "num_file_creations", "num_shells", "num_access_files", "num_outbound_cmds", "is_host_login", "is_guest_login", "count",
            "srv_count", "serror_rate", "srv_serror_rate", "rerror_rate", "srv_rerror_rate", "same_srv_rate", "diff_srv_rate", "srv_diff_host_rate", "dst_host_count", "dst_host_srv_count",
            "dst_host_same_srv_rate", "dst_host_diff_srv_rate", "dst_host_same_src_port_rate", "dst_host_srv_diff_host_rate", "dst_host_serror_rate", "dst_host_srv_serror_rate",
            "dst_host_rerror_rate", "dst_host_srv_rerror_rate"};

    @Override
    public void ack(Object msgId) {
    }

    @Override
    public void close() {
    }

    @Override
    public void activate() {
    }

    @Override
    public void deactivate() {
    }

    @Override
    public void fail(Object msgId) {
    }

    @Override
    public void nextTuple() {
        if (read) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return;
        }
        String str;
        BufferedReader reader = new BufferedReader(fileReader);
        try {
            while ((str = reader.readLine()) != null) {
                String[] fields = str.split(",");
                this.collector.emit(new Values(fields));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            read = true;
        }
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        try {
            this.fileReader = new FileReader(dataSource);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        this.collector = collector;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(field_names));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}

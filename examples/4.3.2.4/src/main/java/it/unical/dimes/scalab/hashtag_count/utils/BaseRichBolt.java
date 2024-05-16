package it.unical.dimes.scalab.hashtag_count.utils;

import org.apache.storm.topology.IRichBolt;

public abstract class BaseRichBolt extends BaseComponent implements IRichBolt {
    @Override
    public void cleanup() {
    }
}
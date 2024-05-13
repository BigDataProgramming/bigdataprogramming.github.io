package it.unical.dimes.scalab.pig;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import java.io.IOException;

public class Processing extends EvalFunc<String> {

    @Override
    public String exec(Tuple tuple) throws IOException {
        if (tuple == null || tuple.size() == 0 || tuple.get(0) == null)
            return null;
        try {
            String str = (String) tuple.get(0);
            // Remove punctuation and, if required, apply lemmatization, stemming and other
            String clean = str.toLowerCase().replaceAll("\\p{Punct}", "");
            return clean;
        } catch (Exception e) {
            throw new IOException("Caught exception processing input row ", e);
        }
    }
}

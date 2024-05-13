package it.unical.dimes.scalab.hive.udf;

import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.StringObjectInspector;

public class GeoDataUDF extends GenericUDF {
    private StringObjectInspector input;
    private StringObjectInspector input2;

    @Override
    public ObjectInspector initialize(ObjectInspector[] args) throws UDFArgumentException {
        // check to make sure the input has 1 argument
        if (args.length != 2)
            throw new UDFArgumentException("input must have length 2");
        // create an ObjectInspector for the inputs and check to make sure they are strings
        if (!(args[0] instanceof StringObjectInspector))
            throw new UDFArgumentException("input 1 must be a string object");
        if (!(args[1] instanceof StringObjectInspector))
            throw new UDFArgumentException("input 2 must be a string object");

        this.input = (StringObjectInspector) args[0];
        this.input2 = (StringObjectInspector) args[1];
        return PrimitiveObjectInspectorFactory.javaStringObjectInspector;
    }

    @Override
    public Object evaluate(DeferredObject[] args) throws HiveException {
        if (input == null || input2 == null || args.length != 2 || args[0].get() == null
                || args[1].get() == null)
            return null;
        String postText = input.getPrimitiveJavaObject(args[0].get()).toString().toLowerCase();
        String[] forwardKeywordsArray = input2.getPrimitiveJavaObject(args[1].get()).toString().split(",");

        for (String k : forwardKeywordsArray) {
            if (postText.contains(k.toLowerCase()))
                return forwardKeywordsArray[0];
        }
        return null;
    }

    @Override
    public String getDisplayString(String[] strings) {
        return getStandardDisplayString("geo_data", strings);
    }
}

package be.ugent.rml.records;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import net.minidev.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is a specific implementation of a record for JSON.
 * Every record corresponds with a JSON object in a data source.
 */
public class JSONRecord extends Record {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String path;
    private final Object document;

    public JSONRecord(Object document, String path) {
        this.path = path;
        this.document = document;
    }

    /**
     * This method returns the objects for a reference (JSONPath) in the record.
     * @param value the reference for which objects need to be returned.
     * @return a list of objects for the reference.
     */
    @Override
    public List<Object> get(String value) {
        List<Object> results = new ArrayList<>();

        // We put simple values between square brackets to make sure no non-escaped shenanigans happen.
        if (!value.contains("[") && !value.contains(".") && !value.equals("@")) {
            value = "['" + value + "']";
        } else if (value.equals("@")) {
            value = "" ;
        } else {
            value = "." + value;
        }

        // TODO do we need to be smarter that this? Below isn't complete yet, but also doesn't seem necessary
//        String[] valueParts = value.split("\\.");
//        StringBuilder escapedValue = new StringBuilder();
//        for (String valuePart : valueParts) {
//            // This JSONPath library specifically cannot handle keys with commas, so we need to escape it
//            String escapedValuePart = valuePart.replaceAll(",", "\\\\,");
//            if (!(escapedValuePart.startsWith("["))) {
//                escapedValue.append("['").append(escapedValuePart).append("']");
//            } else {
//                escapedValue.append(escapedValuePart);
//            }
//        }

        // This JSONPath library specifically cannot handle keys with commas, so we need to escape it
        String fullValue = (this.path + value).replaceAll(",", "\\\\,");

        try {
            Object t = JsonPath.read(document, fullValue);

            if (t instanceof JSONArray) {
                JSONArray array = (JSONArray) t;
                ArrayList<String> tempList = new ArrayList<>();

                for (Object o : array) {
                    if (o != null) {
                        tempList.add(o.toString());
                    }
                }

                results.add(tempList);
            } else {
                if (t != null) {
                    results.add(t.toString());
                }
            }
        } catch (JsonPathException e) {
            logger.warn(e.getMessage() + " for path " + this.path + value, e);
        }

        return results;
    }
}

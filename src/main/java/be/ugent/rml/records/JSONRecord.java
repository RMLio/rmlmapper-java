package be.ugent.rml.records;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
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

    private String path;
    private Object document;

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

        try {
            // JSONPaths with spaces need to have [ ] around it for the library we use.
            if (value.contains(" ")) {
                value = "['" + value + "']";
            }

            Object t = JsonPath.read(document, this.path + "." + value);

            if (t instanceof JSONArray) {
                JSONArray array = (JSONArray) t;
                ArrayList<String> tempList = new ArrayList<>();

                for (Object o : array) {
                    tempList.add(o.toString());
                }

                results.add(tempList);
            } else {
                if (t != null) {
                    results.add(t.toString());
                }
            }
        } catch(PathNotFoundException e) {
            logger.warn(e.getMessage(), e);
        }

        return results;
    }
}

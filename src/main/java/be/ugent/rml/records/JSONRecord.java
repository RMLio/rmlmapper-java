package be.ugent.rml.records;

import be.ugent.rml.Utils;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import net.minidev.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

public class JSONRecord implements Record {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private String path;
    private Object document;

    public JSONRecord(Object document, String path) {
        this.path = path;
        this.document = document;
    }

    @Override
    public List<String> get(String value) {
        List<String> results = new ArrayList<>();

        try {
            if (value.contains(" ")) {
                value = "['" + value + "']";
            }

            Object t = JsonPath.read(document, this.path + "." + value);

            if (t instanceof JSONArray) {
                JSONArray array = (JSONArray) t;

                for (int i = 0; i < array.size(); i ++) {
                    results.add(array.get(i).toString());
                }
            } else {
                results.add(t.toString());
            }
        } catch(PathNotFoundException e) {
            logger.warn(e.getMessage(), e);
        }

        return results;
    }
}

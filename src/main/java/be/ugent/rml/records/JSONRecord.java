package be.ugent.rml.records;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import net.minidev.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

public class JSONRecord extends Record {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private String path;
    private Object document;

    public JSONRecord(Object document, String path) {
        this.path = path;
        this.document = document;
    }

    @Override
    public List<Object> get(String value) {
        List<Object> results = new ArrayList<>();

        try {
            if (value.contains(" ")) {
                value = "['" + value + "']";
            }

            Object t = JsonPath.read(document, this.path + "." + value);

            if (t instanceof JSONArray) {
                JSONArray array = (JSONArray) t;
                ArrayList<String> tempList = new ArrayList<>();

                for (int i = 0; i < array.size(); i ++) {
                    tempList.add(array.get(i).toString());
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

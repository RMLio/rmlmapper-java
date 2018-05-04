package be.ugent.rml.records;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
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
            Object t = JsonPath.read(document, this.path + "." + value);
            results.add(t.toString());
        } catch(PathNotFoundException e) {
            logger.warn(e.getMessage(), e);
        }

        return results;
    }
}

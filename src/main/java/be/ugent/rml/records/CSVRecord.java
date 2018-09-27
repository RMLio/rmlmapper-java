package be.ugent.rml.records;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CSVRecord implements Record {

    private Map<String, List<Object>> values;

    public CSVRecord(Map<String, List<Object>> values) {
        this.values = values;
    }

    public List<Object> get(String value) {
        List<Object> result = values.get(value);

        if (result == null) {
            result =  new ArrayList<>();
        }

        return result;
    }
}

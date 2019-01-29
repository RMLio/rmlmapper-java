package be.ugent.rml.records;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVRecord extends Record {

    private Map<String, List<Object>> values;
    private Map<String, String> datatypes;


    public CSVRecord(Map<String, List<Object>> values) {
        this.values = values;
        this.datatypes = new HashMap<>();
    }

    public CSVRecord(Map<String, List<Object>> values, Map<String, String> datatypes) {
        this.values = values;
        this.datatypes = datatypes;
    }

    public List<Object> get(String value) {
        List<Object> result = values.get(value);

        if (result == null) {
            result =  new ArrayList<>();
        }

        return result;
    }

    public String getDataType(String value) {
        if (this.datatypes.containsKey(value)) {
            return this.datatypes.get(value);
        }
        return null;
    }
}

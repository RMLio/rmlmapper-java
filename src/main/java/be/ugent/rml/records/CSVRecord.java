package be.ugent.rml.records;

import java.util.List;
import java.util.Map;

public class CSVRecord implements Record {

    private Map<String, List<String>> values;

    public CSVRecord(Map<String, List<String>> values) {
        this.values = values;
    }

    public List<String> get(String value) {
        return values.get(value);
    }
}

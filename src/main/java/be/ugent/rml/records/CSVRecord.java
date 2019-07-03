package be.ugent.rml.records;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CSVRecord extends Record {

    private org.apache.commons.csv.CSVRecord record;
    private Map<String, String> datatypes;

    CSVRecord(org.apache.commons.csv.CSVRecord record, Map<String, String> datatypes) {
        this.record = record;
        this.datatypes = datatypes;
    }

    public String getDataType(String value) {
        String datatype = null;

        if (datatypes != null) {
            datatype = datatypes.get(value);
        }

        return datatype;
    }

    @Override
    public List<Object> get(String value) {
        List<Object> result = new ArrayList<>();
        Object obj;
        try {
            obj = this.record.get(value);
            result.add(obj);
        } catch (Exception e) {
            return result;
        }
        return result;
    }
}

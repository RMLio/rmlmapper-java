package be.ugent.rml.records;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RDBsRecord implements Record {

    HashMap<String, List<Object>> values;

    public RDBsRecord(HashMap<String, List<Object>> values) {
        this.values = values;
    }

    @Override
    public List<Object> get(String value) {
        List<Object> result = values.get(value);

        if (result == null) {
            result = new ArrayList<>();
        }

        return result;
    }
}

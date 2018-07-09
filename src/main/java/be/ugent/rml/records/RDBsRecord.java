package be.ugent.rml.records;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RDBsRecord implements Record {

    HashMap<String, List<String>> values;

    public RDBsRecord(HashMap<String, List<String>> values) {
        this.values = values;
    }

    @Override
    public List<String> get(String value) {
        List<String> result = values.get(value);

        if (result == null) {
            result = new ArrayList<>();
        }

        return result;
    }
}

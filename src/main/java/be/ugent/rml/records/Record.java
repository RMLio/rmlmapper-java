package be.ugent.rml.records;

import java.util.List;

public abstract class Record {

    public abstract List<Object> get(String value);

    public String getDataType(String value) {
        return null;
    }
}

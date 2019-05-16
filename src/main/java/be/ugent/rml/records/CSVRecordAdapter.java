package be.ugent.rml.records;

import org.apache.commons.csv.CSVRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CSVRecordAdapter extends Record {

    private CSVRecord record;

    CSVRecordAdapter(CSVRecord record) {
        this.record = record;
    }

    public String getDataType(String value) {
//        if (this.record.toMap().containsKey(value)) {
//            return this.record.toMap().get(value);
//        }
        return null;
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

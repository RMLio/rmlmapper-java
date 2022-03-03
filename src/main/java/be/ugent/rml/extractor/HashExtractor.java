package be.ugent.rml.extractor;

import be.ugent.rml.functions.SingleRecordFunctionExecutor;
import be.ugent.rml.records.Record;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HashExtractor implements Extractor, SingleRecordFunctionExecutor {

    public HashExtractor() {
    }

    @Override
    public List<Object> extract(Record record) {
        ArrayList<Object> result = new ArrayList<>();
        result.add("" + record.hashCode());
        return result;
    }

    @Override
    public Object execute(Record record) throws IOException {
        return extract(record);
    }

    /**
     * to String method
     *
     * @return string
     */
    @Override
    public String toString() {
        return "HashExtractor";
    }
}

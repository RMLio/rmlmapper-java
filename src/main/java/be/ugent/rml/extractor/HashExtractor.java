package be.ugent.rml.extractor;

import be.ugent.rml.functions.SingleRecordFunctionExecutor;
import be.ugent.rml.records.Record;

import java.io.IOException;
import java.util.List;

public class HashExtractor implements Extractor, SingleRecordFunctionExecutor {

    public HashExtractor() {
    }

    @Override
    public List<Object> extract(Record record) {
        return List.of(String.valueOf(record.hashCode()));
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

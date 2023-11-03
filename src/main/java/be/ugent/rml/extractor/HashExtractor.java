package be.ugent.rml.extractor;

import be.ugent.idlab.knows.dataio.record.Record;
import be.ugent.rml.functions.SingleRecordFunctionExecutor;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class HashExtractor implements Extractor, SingleRecordFunctionExecutor {

    public HashExtractor() {
    }

    @Override
    public List<Object> extract(Record record) {
        return Collections.singletonList(Integer.toString(record.hashCode()));
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

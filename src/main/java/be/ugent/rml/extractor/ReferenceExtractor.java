package be.ugent.rml.extractor;

import be.ugent.rml.functions.SingleRecordFunctionExecutor;
import be.ugent.rml.records.Record;

import java.io.IOException;
import java.util.List;

public class ReferenceExtractor implements Extractor, SingleRecordFunctionExecutor {

    public String reference;

    public ReferenceExtractor(String reference, boolean ignoreDoubleQuotes) {
        if (ignoreDoubleQuotes && reference.charAt(0) == '"' && reference.charAt(reference.length() - 1) == '"') {
            this.reference = reference.substring(1, reference.length() - 1);
        }
        else {
            this.reference = reference;
        }
    }

    public ReferenceExtractor(String reference) {
        this(reference, false);
    }

    @Override
    public List<Object> extract(Record record) {
        return record.get(reference);
    }

    @Override
    public String toString() {
        return "ReferenceExecutor that works with " + reference;
    }

    @Override
    public Object execute(Record record) throws IOException {
        return extract(record);
    }
}

package be.ugent.rml.extractor;

import be.ugent.idlab.knows.dataio.source.Source;
import be.ugent.rml.functions.SingleRecordFunctionExecutor;
import be.ugent.rml.records.Record;

import java.io.IOException;
import java.util.List;

public class ReferenceExtractor implements Extractor, SingleRecordFunctionExecutor {

    public String reference;
    private boolean ignoreDoubleQuotes;

    public ReferenceExtractor(String reference, boolean ignoreDoubleQuotes) {
        this.reference = reference;
        this.ignoreDoubleQuotes = ignoreDoubleQuotes;
    }

    public ReferenceExtractor(String reference) {
        this(reference, false);
    }

    @Override
    public List<Object> extract(Source source) {
        String temp = this.reference;

        if (ignoreDoubleQuotes && temp.startsWith("\"") && temp.endsWith("\"")) {
            temp = temp.substring(1, temp.length() - 1);
        }

        return source.get(temp);
    }

    @Override
    public String toString() {
        return "ReferenceExecutor that works with " + reference;
    }

    @Override
    public Object execute(Source source) throws IOException {
        return extract(source);
    }
}

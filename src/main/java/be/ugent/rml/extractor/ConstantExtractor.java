package be.ugent.rml.extractor;

import be.ugent.idlab.knows.dataio.record.Record;
import be.ugent.rml.functions.SingleRecordFunctionExecutor;

import java.io.IOException;
import java.util.List;

public class ConstantExtractor implements Extractor, SingleRecordFunctionExecutor {

    private final String constant;
    private final List<Object> constantList;

    /**
     * Becomes true when a function is detected that needs a special marker to indicate "End-of-File" (EOF).
     */
    private final boolean needsEOFMarker;

    public ConstantExtractor(String constant) {
        this. constantList = List.of(constant);
        this.constant = constant;
        needsEOFMarker = constant.equals("https://w3id.org/imec/idlab/function#implicitDelete")
            || constant.equals("http://example.com/idlab/function/implicitDelete");
    }

    @Override
    public List<Object> extract(Record record) {
        return this.constantList;
    }

    @Override
    public Object execute(Record record) throws IOException {
        return this.constant;
    }

    /**
     * Returns {@code true} if this extractor needs an End-of-File (EOF) marker the end of the dataset.
     * At this moment only required if function <a href="https://w3id.org/imec/idlab/function#implicitDelete">https://w3id.org/imec/idlab/function#implicitDelete</a> is used.
     * @return {@code true} if an EOF marker is required.
     */
    @Override
    public boolean needsEOFMarker() {
        return needsEOFMarker;
    }

    /**
     * to String method
     *
     * @return string
     */
    @Override
    public String toString() {
        return "\"" + constant + '\"';
    }
}

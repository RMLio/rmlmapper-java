package be.ugent.rml.functions;


import be.ugent.idlab.knows.dataio.record.Record;

public interface SingleRecordFunctionExecutor {

    Object execute(Record record) throws Exception;

    /**
     * Returns {@code true} when a function is used in this extractor that needs a special marker
     * to indicate "End-of-File" (EOF).
     */
    default boolean needsEOFMarker() {
        return false;
    }
}

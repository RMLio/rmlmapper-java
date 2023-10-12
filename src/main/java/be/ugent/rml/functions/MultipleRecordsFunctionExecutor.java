package be.ugent.rml.functions;

import be.ugent.idlab.knows.dataio.record.Record;

import java.util.Map;

public interface MultipleRecordsFunctionExecutor {

    Object execute(Map<String, Record> records) throws Exception;

    /**
     * Returns {@code true} when a function is used in this executor that needs a special marker
     * to indicate "End-of-File" (EOF).
     */
    default boolean needsEOFMarker() {return false;}
}

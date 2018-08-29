package be.ugent.rml.functions;

import be.ugent.rml.records.Record;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface MultipleRecordsFunctionExecutor {

    List<?> execute(Map<String, Record> records) throws IOException;
}

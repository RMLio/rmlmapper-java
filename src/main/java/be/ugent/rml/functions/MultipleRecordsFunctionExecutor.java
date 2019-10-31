package be.ugent.rml.functions;

import be.ugent.rml.records.Record;

import java.io.IOException;
import java.util.Map;

public interface MultipleRecordsFunctionExecutor {

    Object execute(Map<String, Record> records) throws Exception;
}

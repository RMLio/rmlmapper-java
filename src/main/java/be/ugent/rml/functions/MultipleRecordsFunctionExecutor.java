package be.ugent.rml.functions;

import be.ugent.idlab.knows.dataio.record.Record;

import java.util.Map;

public interface MultipleRecordsFunctionExecutor {

    Object execute(Map<String, Record> records) throws Exception;
}

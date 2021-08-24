package be.ugent.rml.functions;

import be.ugent.rml.records.Record;

public interface SingleRecordFunctionExecutor {

    Object execute(Record record) throws Exception;
}

package be.ugent.rml.functions;

import be.ugent.rml.records.Record;

import java.util.HashMap;

public abstract class AbstractSingleRecordFunctionExecutor implements SingleRecordFunctionExecutor {

    protected MultipleRecordsFunctionExecutor functionExecutor;

    public Object execute(Record record) throws Exception {
        HashMap<String, Record> recordsMap = new HashMap<>();
        recordsMap.put("_default", record);

        return this.functionExecutor.execute(recordsMap);
    }
}

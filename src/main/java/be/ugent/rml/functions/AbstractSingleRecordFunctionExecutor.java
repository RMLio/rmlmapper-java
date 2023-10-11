package be.ugent.rml.functions;

import be.ugent.rml.records.Record;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractSingleRecordFunctionExecutor implements SingleRecordFunctionExecutor {

    protected MultipleRecordsFunctionExecutor functionExecutor;

    public Object execute(Record record) throws Exception {
        Map<String, Record> recordsMap = new HashMap<>();
        recordsMap.put("_default", record);

        return this.functionExecutor.execute(recordsMap);
    }

    @Override
    public boolean needsMagicEndValue() {
        return functionExecutor.needsMagicEndValue();
    }
}

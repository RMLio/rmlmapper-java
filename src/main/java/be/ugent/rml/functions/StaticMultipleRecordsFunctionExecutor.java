package be.ugent.rml.functions;

import be.ugent.rml.records.Record;

import java.util.HashMap;
import java.util.Map;

public class StaticMultipleRecordsFunctionExecutor implements MultipleRecordsFunctionExecutor {

    private final FunctionModel functionModel;
    private final Map<String, Object[]> parameters;

    public StaticMultipleRecordsFunctionExecutor(FunctionModel model, Map<String, Object[]> parameters) {
        this.functionModel = model;
        this.parameters = parameters;
    }

    @Override
    public Object execute(Map<String, Record> records) throws Exception {
        Map <String, Object> filledInParameters = new HashMap<>();

        for (Map.Entry<String, Object[]> entry : this.parameters.entrySet()) {
            SingleRecordFunctionExecutor executor = (SingleRecordFunctionExecutor) entry.getValue()[1];
            String recordType = (String) entry.getValue()[0];

            Object o = executor.execute(records.get(recordType));

            if (o != null) {
                filledInParameters.put(entry.getKey(), o);
            } else {
                // TODO check whether key is actually optional!
                filledInParameters.put(entry.getKey(), null);
            }
        }

        return this.functionModel.execute(filledInParameters);
    }
}

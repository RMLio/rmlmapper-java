package be.ugent.rml.functions;

import be.ugent.idlab.knows.functions.agent.Agent;
import be.ugent.idlab.knows.functions.agent.Arguments;
import be.ugent.rml.records.Record;

import java.util.HashMap;
import java.util.Map;

public class StaticMultipleRecordsFunctionExecutor implements MultipleRecordsFunctionExecutor {

    private final FunctionModel functionModel;  // TODO: remove
    private final Map<String, Object[]> parameters;

    private final Agent functionAgent;
    private final String functionId;

    public StaticMultipleRecordsFunctionExecutor(FunctionModel model, Map<String, Object[]> parameters, Agent functionAgent, String functionId) {
        this.functionModel = model;
        this.parameters = parameters;
        this.functionAgent = functionAgent;
        this.functionId = functionId;
    }

    @Override
    public Object execute(Map<String, Record> records) throws Exception {
        Map <String, Object> filledInParameters = new HashMap<>();
        final Arguments functionArguments = new Arguments();

        for (Map.Entry<String, Object[]> entry : this.parameters.entrySet()) {
            SingleRecordFunctionExecutor executor = (SingleRecordFunctionExecutor) entry.getValue()[1];
            String recordType = (String) entry.getValue()[0];

            Object o = executor.execute(records.get(recordType));

            // TODO check whether key is actually optional!
            filledInParameters.put(entry.getKey(), o);
            functionArguments.add(entry.getKey(), o);
        }

        Object result1 = functionModel.execute(filledInParameters);
        Object result2 = functionAgent.execute(functionId, functionArguments);
        assert(result1.equals(result2));
        return result1;
    }
}

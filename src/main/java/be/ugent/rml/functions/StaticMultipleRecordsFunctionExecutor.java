package be.ugent.rml.functions;

import be.ugent.idlab.knows.dataio.source.Source;
import be.ugent.idlab.knows.functions.agent.Agent;
import be.ugent.idlab.knows.functions.agent.Arguments;
import be.ugent.rml.records.Record;

import java.util.Map;

public class StaticMultipleRecordsFunctionExecutor implements MultipleRecordsFunctionExecutor {

    private final Map<String, Object[]> parameters;

    private final Agent functionAgent;
    private final String functionId;

    public StaticMultipleRecordsFunctionExecutor(final Map<String, Object[]> parameters, Agent functionAgent, String functionId) {
        this.parameters = parameters;
        this.functionAgent = functionAgent;
        this.functionId = functionId;
    }

    @Override
    public Object execute(Map<String, Source> sources) throws Exception {
        final Arguments functionArguments = new Arguments();

        for (Map.Entry<String, Object[]> entry : this.parameters.entrySet()) {
            SingleRecordFunctionExecutor executor = (SingleRecordFunctionExecutor) entry.getValue()[1];
            String recordType = (String) entry.getValue()[0];

            Object o = executor.execute(sources.get(recordType));

            // TODO check whether key is actually optional!
            functionArguments.add(entry.getKey(), o);
        }

        return functionAgent.execute(functionId, functionArguments);
    }
}

package be.ugent.rml.functions;

import be.ugent.idlab.knows.functions.agent.Agent;

import java.util.ArrayList;
import java.util.List;

public class DynamicSingleRecordFunctionExecutor extends AbstractSingleRecordFunctionExecutor {

    public DynamicSingleRecordFunctionExecutor(List<ParameterValuePair> parameterValuePairs, final Agent functionAgent) {
        ArrayList<ParameterValueOriginPair> pairs = new ArrayList<>();

        parameterValuePairs.forEach(pair -> {
            ArrayList<TermGeneratorOriginPair> objectGeneratorOriginPairs = new ArrayList<>();

            pair.getValueGenerators().forEach(vGen -> {
                objectGeneratorOriginPairs.add(new TermGeneratorOriginPair(vGen, "_default"));
            });

            pairs.add(new ParameterValueOriginPair(pair.getParameterGenerators(), objectGeneratorOriginPairs));
        });

        functionExecutor = new DynamicMultipleRecordsFunctionExecutor(pairs, functionAgent);
    }
}
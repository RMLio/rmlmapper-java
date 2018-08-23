package be.ugent.rml.functions;

import be.ugent.rml.NAMESPACES;
import be.ugent.rml.records.Record;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DynamicJoinConditionFunctionExecutor implements JoinConditionFunctionExecutor {

    private List<ParameterValueOriginPair> parameterValuePairs;
    private FunctionLoader functionLoader;

    public DynamicJoinConditionFunctionExecutor(List<ParameterValueOriginPair> parameterValuePairs, FunctionLoader functionLoader) {
        this.parameterValuePairs = parameterValuePairs;
        this.functionLoader = functionLoader;
    }

    @Override
    public boolean execute(Record child, Record parent) throws IOException {
        final ArrayList<Term> fnTerms = new ArrayList<>();
        final HashMap<String, Object> args =  new HashMap<>();

        parameterValuePairs.forEach(pv -> {
            ArrayList<Term> parameters = new ArrayList<>();
            ArrayList<Term> values = new ArrayList<>();

            pv.getParameterGenerators().forEach(parameterGen -> {
                try {
                    parameters.addAll(parameterGen.generate(child));
                } catch (IOException e) {
                    //todo be more nice and gentle
                    e.printStackTrace();
                }
            });

            pv.getValueGeneratorPairs().forEach(pair -> {
                Record record;

                if (pair.getOrigin().equals("child")) {
                    record = child;
                } else {
                    record = parent;
                }

                try {
                    values.addAll(pair.getTermGenerator().generate(record));
                } catch (IOException e) {
                    //todo be more nice and gentle
                    e.printStackTrace();
                }
            });

            if (parameters.contains(new NamedNode(NAMESPACES.FNO + "executes"))){
                fnTerms.add(values.get(0));
            } else {
                parameters.forEach(parameter -> {
                    values.forEach(value -> {
                        args.put(parameter.getValue(), value.getValue());
                    }) ;
                });
            }
        });

        List<String> results = functionLoader.getFunction(fnTerms.get(0)).execute(args);

        return !results.isEmpty() && results.get(0).equals("true");
    }
}

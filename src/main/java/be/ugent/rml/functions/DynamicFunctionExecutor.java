package be.ugent.rml.functions;

import be.ugent.rml.NAMESPACES;
import be.ugent.rml.records.Record;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DynamicFunctionExecutor implements FunctionExecutor {

    private List<ParameterValuePair> parameterValuePairs;
    private FunctionLoader functionLoader;
    private StaticFunctionExecutor fn;

    public DynamicFunctionExecutor(List<ParameterValuePair> parameterValuePairs, FunctionLoader functionLoader) {
        this.parameterValuePairs = parameterValuePairs;
        this.functionLoader = functionLoader;
    }

    public List<?> execute(Record record) throws IOException {
        final ArrayList<Term> fnTerms = new ArrayList<>();
        final HashMap<String, Object> args =  new HashMap<>();

        parameterValuePairs.forEach(pv -> {
            ArrayList<Term> parameters = new ArrayList<>();
            ArrayList<Term> values = new ArrayList<>();

            pv.getParameterGenerators().forEach(parameterGen -> {
                try {
                    parameters.addAll(parameterGen.generate(record));
                } catch (IOException e) {
                    //todo be more nice and gentle
                    e.printStackTrace();
                }
            });

            pv.getValueGenerators().forEach(valueGenerator -> {
                try {
                    values.addAll(valueGenerator.generate(record));
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

        if (fnTerms.isEmpty()) {
            //todo throw error
            return new ArrayList<>();
        } else {
            return functionLoader.getFunction(fnTerms.get(0)).execute(args);
        }
    }
}
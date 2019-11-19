package be.ugent.rml.functions;

import be.ugent.rml.NAMESPACES;
import be.ugent.rml.records.Record;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class DynamicMultipleRecordsFunctionExecutor implements MultipleRecordsFunctionExecutor {

    private static final Logger logger = LoggerFactory.getLogger(DynamicMultipleRecordsFunctionExecutor.class);
    private List<ParameterValueOriginPair> parameterValuePairs;
    private FunctionLoader functionLoader;

    public DynamicMultipleRecordsFunctionExecutor(List<ParameterValueOriginPair> parameterValuePairs, FunctionLoader functionLoader) {
        this.parameterValuePairs = parameterValuePairs;
        this.functionLoader = functionLoader;
    }

    @Override
    public Object execute(Map<String, Record> records) throws Exception {
        final ArrayList<Term> fnTerms = new ArrayList<>();
        final HashMap<String, Object> args =  new HashMap<>();

        parameterValuePairs.forEach(pv -> {
            ArrayList<Term> parameters = new ArrayList<>();
            ArrayList<Term> values = new ArrayList<>();

            pv.getParameterGenerators().forEach(parameterGen -> {
                try {
                    parameters.addAll(parameterGen.generate(records.get("child")));
                } catch (Exception e) {
                    //todo be more nice and gentle
                    e.printStackTrace();
                }
            });

            pv.getValueGeneratorPairs().forEach(pair -> {
                try {
                    values.addAll(pair.getTermGenerator().generate(records.get(pair.getOrigin())));
                } catch (Exception e) {
                    //todo be more nice and gentle
                    e.printStackTrace();
                }
            });

            if (parameters.contains(new NamedNode(NAMESPACES.FNO + "executes")) || parameters.contains(new NamedNode(NAMESPACES.FNO_S + "executes"))){
                if (parameters.contains(new NamedNode(NAMESPACES.FNO + "executes"))) {
                    logger.warn("http is used instead of https for " + NAMESPACES.FNO_S + ". " +
                            "Still works for now, but will be deprecated in the future.");
                }

                fnTerms.add(values.get(0));
            } else {
                parameters.forEach(parameter -> {
                    ArrayList<Object> temp = new ArrayList<>();

                    values.forEach(value -> {
                        temp.add(value.getValue());
                    });

                    args.put(parameter.getValue(), temp);
                });
            }
        });

        if (fnTerms.isEmpty()) {
            throw new Exception("No function was defined for parameters: " + args.keySet());
        } else {
            return functionLoader.getFunction(fnTerms.get(0)).execute(args);
        }
    }
}

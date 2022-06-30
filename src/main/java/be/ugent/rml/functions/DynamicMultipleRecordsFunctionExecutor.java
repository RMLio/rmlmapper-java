package be.ugent.rml.functions;

import be.ugent.idlab.knows.functions.agent.Agent;
import be.ugent.idlab.knows.functions.agent.Arguments;
import be.ugent.rml.NAMESPACES;
import be.ugent.rml.records.Record;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DynamicMultipleRecordsFunctionExecutor implements MultipleRecordsFunctionExecutor {

    private static final Logger logger = LoggerFactory.getLogger(DynamicMultipleRecordsFunctionExecutor.class);
    private final List<ParameterValueOriginPair> parameterValuePairs;

    private final Agent functionAgent;

    public DynamicMultipleRecordsFunctionExecutor(final List<ParameterValueOriginPair> parameterValuePairs, final Agent functionAgent) {
        this.parameterValuePairs = parameterValuePairs;
        this.functionAgent = functionAgent;
    }

    @Override
    public Object execute(Map<String, Record> records) throws Exception {
        final ArrayList<Term> fnTerms = new ArrayList<>();
        final Arguments arguments = new Arguments();

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

            if (parameters.contains(new NamedNode(NAMESPACES.FNO + "executes")) || parameters.contains(new NamedNode(NAMESPACES.FNO_S + "executes"))) {
                if (parameters.contains(new NamedNode(NAMESPACES.FNO + "executes"))) {
                    logger.warn("http is used instead of https for {}. Still works for now, but will be deprecated in the future.", NAMESPACES.FNO_S);
                }

                fnTerms.add(values.get(0));
            } else {
                for (Term parameter : parameters) {
                    for (Term value : values) {
                        arguments.add(parameter.getValue(), value.getValue());
                    }
                }
            }
        });

        if (fnTerms.isEmpty()) {
            throw new Exception("No function was defined for parameters: " + arguments.getArgumentNames());
        } else {
            final String functionId = fnTerms.get(0).getValue();
            return functionAgent.execute(functionId, arguments);
        }
    }
}
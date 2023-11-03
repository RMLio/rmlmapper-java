package be.ugent.rml.functions;

import be.ugent.idlab.knows.dataio.record.Record;
import be.ugent.idlab.knows.functions.agent.Agent;
import be.ugent.idlab.knows.functions.agent.Arguments;
import be.ugent.rml.NAMESPACES;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DynamicMultipleRecordsFunctionExecutor implements MultipleRecordsFunctionExecutor {

    private static final Logger logger = LoggerFactory.getLogger(DynamicMultipleRecordsFunctionExecutor.class);
    private final List<ParameterValueOriginPair> parameterValuePairs;
    private final Agent functionAgent;


    private boolean needsEOFMarker = false;

    public DynamicMultipleRecordsFunctionExecutor(final List<ParameterValueOriginPair> parameterValuePairs, final Agent functionAgent) {
        this.parameterValuePairs = parameterValuePairs;
        this.functionAgent = functionAgent;
        // check if executor contains term generator that needs an EOF marker
        for (ParameterValueOriginPair parameterValuePair : parameterValuePairs) {
            for (TermGeneratorOriginPair valueGeneratorPair : parameterValuePair.getValueGeneratorPairs()) {
                if (valueGeneratorPair.getTermGenerator().needsEOFMarker()) {
                    needsEOFMarker = true;
                    return;
                }
            }
        }
    }

    @Override
    public Object execute(Map<String, Record> records) throws Exception {
        final List<Term> fnTerms = new ArrayList<>();
        final Arguments arguments = new Arguments();
        final Record child = records.get("child");

        parameterValuePairs.forEach(pv -> {
            List<Term> parameters = new ArrayList<>();
            List<Term> values = new ArrayList<>();

            pv.getParameterGenerators().forEach(parameterGen -> {
                try {
                    parameters.addAll(parameterGen.generate(child));
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            });

            pv.getValueGeneratorPairs().forEach(pair -> {
                try {
                    values.addAll(pair.getTermGenerator().generate(records.get(pair.getOrigin())));
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
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
            try {
                return functionAgent.execute(functionId, arguments);
            } catch (InvocationTargetException e) {
                logger.error("Function '{}' failed to execute with {}", functionId, e.getTargetException().getMessage());
                return null;
            }
        }
    }

    @Override
    public boolean needsEOFMarker() {
        return needsEOFMarker;
    }
}
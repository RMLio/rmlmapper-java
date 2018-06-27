package be.ugent.rml.functions;

import be.ugent.rml.Template;

import java.util.List;
import java.util.Map;

public class FunctionDetails {

    private Function function;
    private Map<String, List<Template>> parameters;

    public FunctionDetails(Function function, Map<String, List<Template>> parameters) {
        this.function = function;
        this.parameters = parameters;
    }

    public Function getFunction() {
        return function;
    }

    public Map<String, List<Template>> getParameters() {
        return parameters;
    }
}

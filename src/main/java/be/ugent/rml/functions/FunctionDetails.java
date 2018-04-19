package be.ugent.rml.functions;

import be.ugent.rml.Element;

import java.util.List;
import java.util.Map;

public class FunctionDetails {

    private Function function;
    private Map<String, List<List<Element>>> parameters;

    public FunctionDetails(Function function, Map<String, List<List<Element>>> parameters) {
        this.function = function;
        this.parameters = parameters;
    }

    public Function getFunction() {
        return function;
    }

    public Map<String, List<List<Element>>> getParameters() {
        return parameters;
    }
}

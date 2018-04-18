package be.ugent.rml.functions;

import be.ugent.rml.Function;
import be.ugent.rml.Value;

import java.util.Map;

public class FunctionDetails {

    private Function function;
    private Map<String, Value> parameters;

    public Function getFunction() {
        return function;
    }

    public Map<String, Value> getParameters() {
        return parameters;
    }
}

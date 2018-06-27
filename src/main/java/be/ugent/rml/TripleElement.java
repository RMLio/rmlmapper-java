package be.ugent.rml;

import be.ugent.rml.functions.Function;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TripleElement {

    private List<Template> graphs;
    private String termType;
    private Function function;
    private Map<String, List<Template>> parameters;

    public TripleElement(List<Template> graphs, String termType, Function function, Map<String, List<Template>> parameters) {
        this.graphs = graphs;
        this.termType = termType;
        this.function = function;
        this.parameters = parameters;
    }

    public List<Template> getGraphs() {
        if (graphs == null) {
            return new ArrayList<>();
        } else {
            return graphs;
        }
    }

    public String getTermType() {
        return termType;
    }

    public Function getFunction() {
        return function;
    }

    public Map<String, List<Template>> getParameters() {
        return parameters;
    }

    public void setGraphs(List<Template> graphs) {
        this.graphs = graphs;
    }

    public void setFunction(Function function) {
        this.function = function;
    }

    public void setParameters(Map<String, List<Template>> parameters) {
        this.parameters = parameters;
    }
}

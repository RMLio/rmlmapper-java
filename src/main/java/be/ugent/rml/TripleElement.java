package be.ugent.rml;

import be.ugent.rml.functions.Function;

import java.util.List;
import java.util.Map;

public class TripleElement {

    private List<List<Element>> graphs;
    private String termType;
    private Function function;
    private Map<String, List<List<Element>>> parameters;

    public TripleElement(List<List<Element>> graphs, String termType, Function function, Map<String, List<List<Element>>> parameters) {
        this.graphs = graphs;
        this.termType = termType;
        this.function = function;
        this.parameters = parameters;
    }

    public List<List<Element>> getGraphs() {
        return graphs;
    }

    public String getTermType() {
        return termType;
    }

    public Function getFunction() {
        return function;
    }

    public Map<String, List<List<Element>>> getParameters() {
        return parameters;
    }

    public void setGraphs(List<List<Element>> graphs) {
        this.graphs = graphs;
    }
}

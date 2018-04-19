package be.ugent.rml;

import be.ugent.rml.functions.Function;

import java.util.List;
import java.util.Map;

public class TripleElement {

    private List<List<Element>> graphs;

    public TripleElement(List<List<Element>> graphs, String termType, Function function, Map<String, List<List<Element>>> parameters) {
        this.graphs = graphs;
    }

    public List<List<Element>> getGraphs() {
        return graphs;
    }

    public String getTermType() {
        return "";
    }

    public Function getFunction() {
        return null;
    }

    public Map<String, List<List<Element>>> getParameters() {
        return null;
    }

    public void setGraphs(List<List<Element>> graphs) {
        this.graphs = graphs;
    }
}

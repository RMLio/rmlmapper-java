package be.ugent.rml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TripleElement {

    private List<String> graphs;

    public TripleElement(List<String> graphs, String termType, Function function, Map<String, Value> parameters) {
        this.graphs = graphs;
    }

    public List<String> getGraphs() {
        return graphs;
    }

    public String getTermType() {
        return "";
    }

    public Function getFunction() {
        return null;
    }

    public Map<String, Value> getParameters() {
        return null;
    }

    public void setGraphs(List<String> graphs) {
        this.graphs = graphs;
    }
}

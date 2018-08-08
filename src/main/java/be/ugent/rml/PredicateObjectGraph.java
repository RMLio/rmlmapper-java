package be.ugent.rml;

import be.ugent.rml.term.ProvenancedTerm;

public class PredicateObjectGraph {

    private ProvenancedTerm predicate;
    private ProvenancedTerm object;
    private ProvenancedTerm graph;

    public PredicateObjectGraph(ProvenancedTerm predicate, ProvenancedTerm object, ProvenancedTerm graph) {
        this.predicate = predicate;
        this.object = object;
        this.graph = graph;
    }

    public ProvenancedTerm getPredicate() {
        return predicate;
    }

    public ProvenancedTerm getObject() {
        return object;
    }

    public ProvenancedTerm getGraph() {
        return graph;
    }
}

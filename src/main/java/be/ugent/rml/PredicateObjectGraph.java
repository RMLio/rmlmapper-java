package be.ugent.rml;

import be.ugent.rml.term.ProvenancedTerm;
import be.ugent.rml.term.Term;

public class PredicateObjectGraph {

    private ProvenancedTerm predicate;
    private ProvenancedTerm object;
    private ProvenancedTerm graph;
    private Term predicateObjectMap;

    public PredicateObjectGraph(ProvenancedTerm predicate, ProvenancedTerm object, ProvenancedTerm graph, Term predicateObjectMap) {
        this.predicate = predicate;
        this.object = object;
        this.graph = graph;
        this.predicateObjectMap = predicateObjectMap;
    }

    public ProvenancedTerm getPredicate() { return predicate; }

    public ProvenancedTerm getObject() {
        return object;
    }

    public ProvenancedTerm getGraph() {
        return graph;
    }

    public Term getPredicateObjectMap() { return predicateObjectMap; }
}

package be.ugent.rml.term;

public class ProvenancedQuad {

    private ProvenancedTerm subject, predicate, object, graph;

    public ProvenancedQuad(ProvenancedTerm subject, ProvenancedTerm predicate, ProvenancedTerm object, ProvenancedTerm graph) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.graph = graph;
    }

    public ProvenancedQuad(ProvenancedTerm subject, ProvenancedTerm predicate, ProvenancedTerm object) {
        this(subject, predicate, object, null);
    }

    public ProvenancedTerm getSubject() {
        return subject;
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

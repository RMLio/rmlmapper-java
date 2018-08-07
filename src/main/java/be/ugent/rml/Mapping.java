package be.ugent.rml;

import java.util.List;

public class Mapping {

    private TripleElement subject;
    private List<PredicateObjectGenerator> predicateObjectGenerators;

    public Mapping(TripleElement subject, List<PredicateObjectGenerator> predicateObjectGenerators) {
        this.subject = subject;
        this.predicateObjectGenerators = predicateObjectGenerators;
    }

    public TripleElement getSubject() {
        return subject;
    }

    public List<PredicateObjectGenerator> getPredicateObjectGenerators() {
        return predicateObjectGenerators;
    }
}

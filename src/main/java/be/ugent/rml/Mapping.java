package be.ugent.rml;

import java.util.List;

public class Mapping {

    private TripleElement subject;
    private List<PredicateObject> predicateObjects;

    public Mapping(TripleElement subject, List<PredicateObject> predicateObjects) {
        this.subject = subject;
        this.predicateObjects = predicateObjects;
    }

    public TripleElement getSubject() {
        return subject;
    }

    public List<PredicateObject> getPredicateObjects() {
        return predicateObjects;
    }
}

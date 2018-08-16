package be.ugent.rml.store;

import be.ugent.rml.term.Term;

public class Quad implements Comparable<Quad> {

    private Term subject, predicate, object, graph;

    public Quad(Term subject, Term predicate, Term object, Term graph) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.graph = graph;
    }

    public Quad(Term subject, Term predicate, Term object) {
        this(subject, predicate, object, null);
    }

    public Term getSubject() {
        return subject;
    }

    public Term getPredicate() {
        return predicate;
    }

    public Term getObject() {
        return object;
    }

    public Term getGraph() {
        return graph;
    }

    @Override
    public int compareTo(Quad o) {
        int compareGraph;
        String oGraph = null;

        if (o.getGraph() != null) {
            oGraph = o.getGraph().toString();
        }

        if (this.graph == null && oGraph == null) {
            compareGraph = 0;
        } else {
            if (this.graph == null) {
                compareGraph = -1;
            } else if (oGraph == null) {
                compareGraph = 1;
            } else {
                compareGraph = this.graph.toString().compareTo(oGraph);
            }
        }
        if (compareGraph == 0) {
            int compareSubject = this.subject.toString().compareTo(o.getSubject().toString());
            if (compareSubject == 0) {
                int comparePredicate = this.predicate.toString().compareTo(o.getPredicate().toString());
                if (comparePredicate == 0) {
                    return this.object.toString().compareTo(o.getObject().toString());
                }
                return comparePredicate;
            } else {
                return compareSubject;
            }
        } else {
            return compareGraph;
        }
    }
}

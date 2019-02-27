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
        int comparison;

        if (this.graph == null || o.getGraph() == null) {
            comparison = 0;
        } else {
            if (this.graph == null) {
                comparison = -1;
            } else if (o.getGraph() == null) {
                comparison = 1;
            } else {
                comparison = this.graph.toString().compareTo(o.getGraph().toString());
            }
        }

        if (comparison == 0) {
            comparison = compareTerms(this.subject, o.getSubject());
            if (comparison == 0) {
                comparison = compareTerms(this.predicate, o.getPredicate());
                if (comparison == 0) {
                    return compareTerms(this.object, o.getObject());
                } else {
                    return comparison;
                }
            } else {
                return comparison;
            }
        } else {
            return comparison;
        }
    }

    private int compareTerms(Term t1, Term t2) {
        if (t1 == null || t2 == null) {
            return 0;
        } else {
            return t1.toString().compareTo(t2.toString());
        }
    }
}

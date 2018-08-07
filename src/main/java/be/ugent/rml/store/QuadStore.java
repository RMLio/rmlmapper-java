package be.ugent.rml.store;

import be.ugent.rml.Term;

import java.util.Collections;
import java.util.List;

public abstract class QuadStore {
    public abstract void removeDuplicates();

    public abstract void addTriple(Term subject, Term predicate, Term object);

    public abstract void addQuad(Term subject, Term predicate, Term object, Term graph);

    public abstract List<Quad> getQuads(Term subject, Term predicate, Term object, Term graph);
    public abstract List<Quad> getQuads(Term subject, Term predicate, Term object);

    public String toString() {
        StringBuilder output = new StringBuilder();

        List<Quad> quads = getQuads(null, null, null);

        for (Quad q : quads) {
            output.append(q.getSubject()).append(" ").append(q.getPredicate()).append(" ").append(q.getObject()).append(" ").append(q.getGraph()).append("\n");
        }

        return output.toString();
    }

    public String toSortedString() {
        StringBuilder output = new StringBuilder();

        List<Quad> quads = getQuads(null, null, null);

        Collections.sort(quads);

        for (Quad q : quads) {
            output.append(q.getSubject()).append(" ").append(q.getPredicate()).append(" ").append(q.getObject()).append(" ").append(q.getGraph()).append("\n");
        }

        return output.toString();
    }
}

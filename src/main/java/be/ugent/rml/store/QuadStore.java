package be.ugent.rml.store;

import java.util.Collections;
import java.util.List;

public abstract class QuadStore {
    public abstract void removeDuplicates();

    public abstract void addTriple(String subject, String predicate, String object);

    public abstract void addQuad(String subject, String predicate, String object, String graph);

    public abstract List<Quad> getQuads(String subject, String predicate, String object, String graph);
    public abstract List<Quad> getQuads(String subject, String predicate, String object);

    public String toString() {
        StringBuilder output = new StringBuilder();

        List<Quad> quads = getQuads(null, null, null);

        for (Quad q : quads) {
            output.append(q.getSubject()).append(" ").append(q.getPredicate()).append(" ").append(q.getObject()).append("\n");
        }

        return output.toString();
    }

    public String toSortedString() {
        StringBuilder output = new StringBuilder();

        List<Quad> quads = getQuads(null, null, null);

        Collections.sort(quads);

        for (Quad q : quads) {
            output.append(q.getSubject()).append(" ").append(q.getPredicate()).append(" ").append(q.getObject()).append("\n");
        }

        return output.toString();
    }
}

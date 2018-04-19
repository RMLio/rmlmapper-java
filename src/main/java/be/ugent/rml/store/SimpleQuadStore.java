package be.ugent.rml.store;

import java.util.ArrayList;
import java.util.List;

public class SimpleQuadStore implements QuadStore {

    private ArrayList<Quad> quads;

    public SimpleQuadStore(ArrayList<Quad> quads) {
        this.quads = quads;
    }

    public SimpleQuadStore() {
        quads = new ArrayList<Quad>();
    }

    public void removeDuplicates() {

    }

    public void addTriple(String subject, String predicate, String object) {
        addQuad(subject, predicate, object, null);
    }

    public void addQuad(String subject, String predicate, String object, String graph) {
        quads.add(new Quad(subject, predicate, object, graph));
    }

    public List<Quad> getQuads(String subject, String predicate, String object, String graph) {
        return quads;
    }

    public List<Quad> getQuads(String subject, String predicate, String object) {
        return getQuads(subject, predicate, object, null);
    }

    public String toString() {
        String output = "";

        for(Quad q: quads) {
            output += q.getSubject() + " " + q.getPredicate() + " " + q.getObject() + "\n";
        }

        return output;
    }
}

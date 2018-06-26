package be.ugent.rml.store;

import java.util.ArrayList;
import java.util.List;

public class SimpleQuadStore extends QuadStore {

    private List<Quad> quads;

    public SimpleQuadStore(ArrayList<Quad> quads) {
        this.quads = quads;
    }

    public SimpleQuadStore() {
        quads = new ArrayList<Quad>();
    }

    public void removeDuplicates() {
        List<Quad> quadsWithDuplicates = new ArrayList<>();

        for (Quad q : quads) {
            int i = 0;

            while (i < quadsWithDuplicates.size() && ! (quadsWithDuplicates.get(i).getSubject().equals(q.getSubject())
                    && quadsWithDuplicates.get(i).getObject().equals(q.getObject())
                    && quadsWithDuplicates.get(i).getPredicate().equals(q.getPredicate())
                    && ((quadsWithDuplicates.get(i).getGraph() == null && q.getGraph() == null) || quadsWithDuplicates.get(i).getGraph().equals(q.getGraph()))
            )) {
                i ++;
            }

            if (i == quadsWithDuplicates.size()) {
                quadsWithDuplicates.add(q);
            }
        }

        quads = quadsWithDuplicates;
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
}

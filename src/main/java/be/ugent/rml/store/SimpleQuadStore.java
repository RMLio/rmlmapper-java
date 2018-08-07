package be.ugent.rml.store;

import be.ugent.rml.Term;

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
                    && !(quadsWithDuplicates.get(i).getGraph() == null && q.getGraph() != null)
                    && !(quadsWithDuplicates.get(i).getGraph() != null && q.getGraph() == null)
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

    public void addTriple(Term subject, Term predicate, Term object) {
        addQuad(subject, predicate, object, null);
    }

    public void addQuad(Term subject, Term predicate, Term object, Term graph) {
        quads.add(new Quad(subject, predicate, object, graph));
    }

    public List<Quad> getQuads(Term subject, Term predicate, Term object, Term graph) {
        return quads;
    }

    public List<Quad> getQuads(Term subject, Term predicate, Term object) {
        return getQuads(subject, predicate, object, null);
    }
}

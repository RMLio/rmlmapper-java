package be.ugent.rml.store;

import be.ugent.rml.term.Term;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of QuadStore with a List of Quads.
 * Package-private
 */
public class SimpleQuadStore extends QuadStore {

    private List<Quad> quads;

    public SimpleQuadStore() {
        quads = new ArrayList<>();
    }

    @Override
    public void removeDuplicates() {
        List<Quad> quadsWithDuplicates = new ArrayList<>();

        for (Quad q : quads) {
            int i = 0;

            while (i < quadsWithDuplicates.size() && !(quadsWithDuplicates.get(i).getSubject().equals(q.getSubject())
                    && quadsWithDuplicates.get(i).getObject().equals(q.getObject())
                    && quadsWithDuplicates.get(i).getPredicate().equals(q.getPredicate())
                    && !(quadsWithDuplicates.get(i).getGraph() == null && q.getGraph() != null)
                    && !(quadsWithDuplicates.get(i).getGraph() != null && q.getGraph() == null)
                    && ((quadsWithDuplicates.get(i).getGraph() == null && q.getGraph() == null) || quadsWithDuplicates.get(i).getGraph().equals(q.getGraph()))
            )) {
                i++;
            }

            if (i == quadsWithDuplicates.size()) {
                quadsWithDuplicates.add(q);
            }
        }

        quads = quadsWithDuplicates;
    }

    @Override
    public void addQuad(Term subject, Term predicate, Term object, Term graph) {
        if (subject != null && predicate != null && object != null) {
            quads.add(new Quad(subject, predicate, object, graph));
        }
    }

    @Override
    public List<Quad> getQuads(Term subject, Term predicate, Term object, Term graph) {
        Quad quad = new Quad(subject, predicate, object, graph);

        List<Quad> filteredQuads = new ArrayList<>();

        for (Quad q : quads) {
            if (quad.compareTo(q) == 0) {
                filteredQuads.add(q);
            }
        }

        return filteredQuads;
    }

    @Override
    public void copyNameSpaces(QuadStore store) {
        // Namespace passing is not needed for .nquads and .hdt
    }

    @Override
    public boolean isEmpty() {
        return quads.isEmpty();
    }

    @Override
    public int size() {
        return quads.size();
    }

    @Override
    public void read(InputStream is, String base, RDFFormat format) {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    public void write(Writer out, String format) throws IOException {
        switch (format) {
            case "nquads":
                toNQuads(out);
                break;
            default:
                throw new Error("Serialization " + format + " not supported");
        }
    }

    @Override
    public boolean equals(Object o) {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    public void removeQuads(Term subject, Term predicate, Term object, Term graph) {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    public boolean contains(Term subject, Term predicate, Term object, Term graph) {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    public boolean isIsomorphic(QuadStore store) {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    @Override
    public boolean isSubset(QuadStore store) {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    private void toNQuads(Writer out) throws IOException {
        for (Quad q : quads) {
            out.write(getNQuadOfQuad(q) + "\n");
        }
    }

    private String getNQuadOfQuad(Quad q) {
        String str = q.getSubject() + " " + q.getPredicate() + " " + q.getObject();

        if (q.getGraph() != null) {
            str += " " + q.getGraph();
        }

        str += ".";

        return str;
    }
}

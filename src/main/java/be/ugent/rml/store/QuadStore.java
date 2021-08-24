package be.ugent.rml.store;

import be.ugent.rml.term.Term;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.*;
import java.util.Collections;
import java.util.List;

/**
 * Vendor-neutral interface for managing RDF collections.
 * Implemented for RDF4J
 * and custom SimpleQuadStore (for faster implementation without indexing of RDF libraries)
 */
public abstract class QuadStore {
    // START ABSTRACT METHODS
    // implement these when extending QuadStore

    @Override
    public abstract boolean equals(Object o);

    /**
     * Remove all Quads matching input from store.
     * @param subject
     * @param predicate
     * @param object
     * @param graph
     */
    public abstract void removeQuads(Term subject, Term predicate, Term object, Term graph);

    /**
     * True if Quad matching input is present in store.
     * @param subject
     * @param predicate
     * @param object
     * @param graph
     * @return
     */
    public abstract boolean contains(Term subject, Term predicate, Term object, Term graph);

    /**
     * Test if given store and this store are isomorphic RDF graph representations
     * @param store
     * @return
     */
    public abstract boolean isIsomorphic(QuadStore store);

    /**
     * Test if given store is subset of this store
     * @param store
     * @return
     */
    public abstract boolean isSubset(QuadStore store);

    /**
     * Remove duplicate quads
     */
    public abstract void removeDuplicates();

    /**
     * Add given Quad to store
     * @param subject
     * @param predicate
     * @param object
     * @param graph
     */
    public abstract void addQuad(Term subject, Term predicate, Term object, Term graph);

    /**
     * Get all Quads in store matching arguments.
     * Null can be used as a wildcard.
     * @param subject
     * @param predicate
     * @param object
     * @param graph
     * @return
     */
    public abstract List<Quad> getQuads(Term subject, Term predicate, Term object, Term graph);

    /**
     * Copy namespaces between stores. Used in retaining the prefixes in the mapping file in the output.
     * TODO define general Namespace class to use between QuadStore instances
     *
     * @param store QuadStore with namespaces to be copied to this store
     */
    public abstract void copyNameSpaces(QuadStore store);

    /**
     * True if RDF quads present is 0
     *
     * @return boolean
     */
    public abstract boolean isEmpty();

    /**
     * Number of RDF quads
     *
     * @return int
     */
    public abstract int size();

    /**
     * Read RDF to QuadStore
     * TODO use class or enum for input format
     *
     * @param is     Stream of RDF in given format
     * @param base   Base URL
     * @param format Given format for RDF
     */
    public abstract void read(InputStream is, String base, RDFFormat format) throws Exception;

    /**
     * Write out the QuadStore in given format
     * TODO use class or enum for output format
     *
     * @param out    Writer output location
     * @param format QuadStore format (.TTL)
     * @throws Exception
     */
    public abstract void write(Writer out, String format) throws Exception;

    // END OF ABSTRACT METHODS

    // following final methods use the abstract methods to provide additional functionality or helper functions
    /**
     * Helper function
     *
     * @param out
     * @param format
     * @throws Exception
     */
    public final void write(ByteArrayOutputStream out, String format) throws Exception {
        write(new BufferedWriter(new OutputStreamWriter(out)), format);
    }

    /**
     * Helper function
     *
     * @param out
     * @param format
     * @throws Exception
     */
    public final void write(PrintStream out, String format) throws Exception {
        write(new PrintWriter(out), format);
    }

    /**
     * Helper function
     *
     * @param subject
     * @param predicate
     * @param object
     * @param graph
     * @return
     * @throws Exception
     */
    public final Quad getQuad(Term subject, Term predicate, Term object, Term graph) throws Exception {
        List<Quad> list = getQuads(subject, predicate, object, graph);
        if (list.size() != 1) {
            throw new Exception(String.format("Single Quad expected, found %s", list.size()));
        }
        return list.get(0);
    }

    /**
     * Helper function
     *
     * @param subject
     * @param predicate
     * @param object
     * @return
     * @throws Exception
     */
    public final Quad getQuad(Term subject, Term predicate, Term object) throws Exception {
        return getQuad(subject, predicate, object, null);
    }

    /**
     * Helper function
     *
     * @param subject
     * @param predicate
     * @param object
     * @return
     */
    public final List<Quad> getQuads(Term subject, Term predicate, Term object) {
        return getQuads(subject, predicate, object, null);
    }

    /**
     * Helper function
     * @param subject
     * @param predicate
     * @param object
     * @return
     */
    public final boolean contains(Term subject, Term predicate, Term object) {
        return contains(subject, predicate, object, null);
    }

    /**
     * Helper function
     *
     * @param subject
     * @param predicate
     * @param object
     */
    public final void addQuad(Term subject, Term predicate, Term object) {
        addQuad(subject, predicate, object, null);
    }

    /**
     * Helper function
     * @param q
     */
    public final void addQuad(Quad q) {
        addQuad(q.getSubject(), q.getPredicate(), q.getObject(), q.getGraph());
    }

    /**
     * Add all quads in given list
     *
     * @param quads to be added to QuadStore
     */
    public final void addQuads(List<Quad> quads) {
        quads.forEach(quad -> addQuad(quad.getSubject(), quad.getPredicate(), quad.getObject(), quad.getGraph()));
    }

    /**
     * Helper function
     * @param subject
     * @param predicate
     * @param object
     */
    public final void removeQuads(Term subject, Term predicate, Term object) {
        removeQuads(subject, predicate, object, null);
    }

    public final void removeQuads(Quad quad) {
        removeQuads(quad.getSubject(), quad.getPredicate(), quad.getObject(), quad.getGraph());
    }

    public final void removeQuads(List<Quad> quads) {
        for (Quad quad : quads) {
            removeQuads(quad);
        }
    }

    /**
     * If fromPredicate is present on from, rename it to toPredicate and move it to to
     * @param from
     * @param fromPredicate
     * @param to
     * @param toPredicate
     */
    public final void tryPropertyTranslation(Term from, Term fromPredicate, Term to, Term toPredicate) {
        List<Quad> quads = getQuads(from, fromPredicate, null);
        for (Quad quad : quads) {
            addQuad(to, toPredicate, quad.getObject());
        }
        removeQuads(quads);
    }

    /**
     * Rename all predicates in graph
     * @param fromPredicate predicate to be renamed
     * @param toPredicate new predicate name
     */
    public final void renameAll(Term fromPredicate, Term toPredicate) {
        List<Quad> quads = getQuads(null, fromPredicate, null);
        for (Quad q : quads) {
            addQuad(q.getSubject(), toPredicate, q.getObject());
        }
        removeQuads(quads);
    }

    /**
     * Uses Quads in string representation
     *
     * @return String of QuadStore
     */
    public final String toString() {
        StringBuilder output = new StringBuilder();

        List<Quad> quads = getQuads(null, null, null);

        for (Quad q : quads) {
            output.append(q.getSubject()).append(" ").append(q.getPredicate()).append(" ").append(q.getObject()).append(" ").append(q.getGraph()).append("\n");
        }

        return output.toString();
    }

    /**
     * Use sorted Quads in string representation
     *
     * @return sorted String of QuadStore
     */
    public final String toSortedString() {
        StringBuilder output = new StringBuilder();

        List<Quad> quads = getQuads(null, null, null);

        Collections.sort(quads);

        for (Quad q : quads) {
            output.append(q.getSubject()).append(" ").append(q.getPredicate()).append(" ").append(q.getObject()).append(" ").append(q.getGraph()).append("\n");
        }

        return output.toString();
    }
}

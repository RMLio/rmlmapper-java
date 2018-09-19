package be.ugent.rml.metadata;

import be.ugent.rml.term.Term;

/**
 * Holds the source triplesMap and Subject-, Object- or PredicateMap for a specific (provenanced) term.
 */
public class Metadata {

    private Term triplesMap;
    private Term sourceMap;

    public Metadata() {
    }

    public Metadata(Term triplesMap) {
        this(triplesMap, null);
    }

    public Metadata(Term triplesMap, Term sourceMap) {
        this.triplesMap = triplesMap;
        this.sourceMap = sourceMap;
    }

    Term getTriplesMap() {
        return triplesMap;
    }

    Term getSourceMap() {
        return sourceMap;
    }

    public void setSourceMap(Term sourceMap) {
        this.sourceMap = sourceMap;
    }
}

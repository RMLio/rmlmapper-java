package be.ugent.rml;

import be.ugent.rml.term.Term;

public class Metadata {

    private Term triplesMap;
    private Term sourceMap;

    public Metadata(Term triplesMap) {
        this(triplesMap, null);
    }

    public Metadata(Term triplesMap, Term sourceMap) {
        this.triplesMap = triplesMap;
        this.sourceMap = sourceMap;
    }

    public Term getTriplesMap() {
        return triplesMap;
    }

    public Term getSourceMap() {
        return sourceMap;
    }

    public void setSourceMap(Term sourceMap) {
        this.sourceMap = sourceMap;
    }
}

package be.ugent.rml;

import be.ugent.rml.term.Term;

public class Metadata {

    private Term triplesMap;

    public Metadata(Term triplesMap) {
        this.triplesMap = triplesMap;
    }
    public void setTripleMap(Term triplesMap) {
        this.triplesMap = triplesMap;
    }

    public Term getTriplesMap() {
        return triplesMap;
    }
}

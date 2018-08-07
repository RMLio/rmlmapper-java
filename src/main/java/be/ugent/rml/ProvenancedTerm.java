package be.ugent.rml;

import be.ugent.rml.term.Term;

public class ProvenancedTerm {

    private Term term;
    private Metadata metdata;

    public ProvenancedTerm(Term term, Metadata metdata) {
        this.term = term;
        this.metdata = metdata;
    }

    public ProvenancedTerm(Term term) {
        this(term, null);
    }

    public Term getTerm() {
        return term;
    }

    public Metadata getMetdata() {
        return metdata;
    }
}

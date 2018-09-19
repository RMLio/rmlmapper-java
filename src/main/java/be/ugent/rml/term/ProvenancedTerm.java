package be.ugent.rml.term;

import be.ugent.rml.MappingInfo;
import be.ugent.rml.metadata.Metadata;

public class ProvenancedTerm {

    private Term term;
    private Metadata metdata;

    public ProvenancedTerm(Term term, Metadata metdata) {
        this.term = term;
        this.metdata = metdata;
    }

    public ProvenancedTerm(Term term, MappingInfo mappingInfo) {
        this.term = term;
        this.metdata = new Metadata();
        this.metdata.setSourceMap(mappingInfo.getTerm());
    }

    public ProvenancedTerm(Term term) {
        this.term = term;
    }

    public Term getTerm() {
        return term;
    }

    public Metadata getMetdata() {
        return metdata;
    }
}

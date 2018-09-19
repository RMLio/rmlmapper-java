package be.ugent.rml.term;

import be.ugent.rml.MappingInfo;
import be.ugent.rml.metadata.Metadata;

public class ProvenancedTerm {

    private Term term;
    private Metadata metadata;

    public ProvenancedTerm(Term term, Metadata metadata) {
        this.term = term;
        this.metadata = metadata;
    }

    public ProvenancedTerm(Term term, MappingInfo mappingInfo) {
        this.term = term;
        this.metadata = new Metadata();
        this.metadata.setSourceMap(mappingInfo.getTerm());
    }

    public ProvenancedTerm(Term term) {
        this.term = term;
    }

    public Term getTerm() {
        return term;
    }

    public Metadata getMetadata() {
        return metadata;
    }
}

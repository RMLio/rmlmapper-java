package be.ugent.rml.term;

import be.ugent.rml.MappingInfo;
import be.ugent.rml.metadata.Metadata;
import be.ugent.rml.store.QuadStore;

import java.util.ArrayList;
import java.util.List;

public class ProvenancedTerm {

    private Term term;
    private Metadata metadata;
    private List<Term> targets;

    public ProvenancedTerm(Term term, Metadata metadata, List<Term> targets) {
        this.term = term;
        this.metadata = metadata;
        this.targets = targets;
    }

    public ProvenancedTerm(Term term, MappingInfo mappingInfo) {
        this.term = term;
        this.metadata = new Metadata();
        this.metadata.setSourceMap(mappingInfo.getTerm());
        this.targets = mappingInfo.getTargets();
    }

    public ProvenancedTerm(Term term) {
        this.term = term;
        this.targets = new ArrayList<Term>();
    }

    public Term getTerm() {
        return term;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public List<Term> getTargets() { return targets; };
}

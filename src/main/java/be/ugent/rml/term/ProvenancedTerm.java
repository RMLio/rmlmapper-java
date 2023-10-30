package be.ugent.rml.term;

import be.ugent.rml.MappingInfo;
import be.ugent.rml.metadata.Metadata;
import org.eclipse.rdf4j.model.Value;

import java.util.ArrayList;
import java.util.List;

public class ProvenancedTerm {

    private Value term;
    private Metadata metadata;
    private List<Value> targets;

    public ProvenancedTerm(Value term, Metadata metadata, List<Value> targets) {
        this.term = term;
        this.metadata = metadata;
        this.targets = targets;
    }

    public ProvenancedTerm(Value term, MappingInfo mappingInfo) {
        this.term = term;
        this.metadata = new Metadata();
        this.metadata.setSourceMap(mappingInfo.getTerm());
        this.targets = mappingInfo.getTargets();
    }

    public ProvenancedTerm(Value term) {
        this.term = term;
        this.targets = new ArrayList<Value>();
    }

    public Value getTerm() {
        return term;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public List<Value> getTargets() { return targets; };
}

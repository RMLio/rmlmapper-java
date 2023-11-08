package be.ugent.rml;

import be.ugent.rml.termgenerator.TermGenerator;
import org.eclipse.rdf4j.model.Value;

import java.util.ArrayList;
import java.util.List;

public class MappingInfo {
    private Value term;
    private TermGenerator termGenerator;
    private List<Value> targets;

    public MappingInfo(Value term, TermGenerator termGenerator, List<Value> targets) {
        this.term = term;
        this.termGenerator = termGenerator;
        this.targets = targets;
    }

    public MappingInfo(Value term, TermGenerator termGenerator) {
        this.term = term;
        this.termGenerator = termGenerator;
        this.targets = new ArrayList<Value>();
    }

    public MappingInfo(Value term, List<Value> targets) {
        this.term = term;
        this.termGenerator = null;
        this.targets = targets;
    }

    public Value getTerm() {
        return term;
    }

    public TermGenerator getTermGenerator() {
        return termGenerator;
    }

    public List<Value> getTargets() {
        return targets;
    }

    public List<Value> addTargets(List<Value> targets) {
        this.targets.addAll(targets);
        return this.targets;
    }
}

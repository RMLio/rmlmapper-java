package be.ugent.rml;

import be.ugent.rml.term.Term;
import be.ugent.rml.termgenerator.TermGenerator;

import java.util.ArrayList;
import java.util.List;

public class MappingInfo {
    private Term term;
    private TermGenerator termGenerator;
    private List<Term> targets;

    public MappingInfo(Term term, TermGenerator termGenerator, List<Term> targets) {
        this.term = term;
        this.termGenerator = termGenerator;
        this.targets = targets;
    }

    public MappingInfo(Term term, TermGenerator termGenerator) {
        this.term = term;
        this.termGenerator = termGenerator;
        this.targets = new ArrayList<Term>();
    }

    public MappingInfo(Term term, List<Term> targets) {
        this.term = term;
        this.termGenerator = null;
        this.targets = targets;
    }

    public Term getTerm() {
        return term;
    }

    public TermGenerator getTermGenerator() {
        return termGenerator;
    }

    public List<Term> getTargets() {
        return targets;
    }

    public List<Term> addTargets(List<Term> targets) {
        this.targets.addAll(targets);
        return this.targets;
    }
}

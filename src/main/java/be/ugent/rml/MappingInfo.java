package be.ugent.rml;

import be.ugent.rml.term.Term;
import be.ugent.rml.termgenerator.TermGenerator;

import java.util.Collections;
import java.util.List;

public class MappingInfo {
    private final Term term;
    private final TermGenerator termGenerator;
    private final List<Term> targets;
    private final List<TermGenerator> targetGenerators;

    public MappingInfo(Term term, TermGenerator termGenerator, List<Term> targets, List<TermGenerator> targetGenerators) {
        this.term = term;
        this.termGenerator = termGenerator;
        this.targets = targets;
        this.targetGenerators = targetGenerators;
    }

    public MappingInfo(Term term, TermGenerator termGenerator) {
        this(term, termGenerator, Collections.emptyList(), Collections.emptyList());
    }

    public MappingInfo(Term term, List<Term> targets, List<TermGenerator> targetGenerators) {
        this(term, null, targets, targetGenerators);
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

    public List<TermGenerator> getTargetGenerators() {
        return this.targetGenerators;
    }

    public void addTargets(List<Term> targets) {
        this.targets.addAll(targets);
    }

    public void addTargetGenerators(List<TermGenerator> targetGenerators) {
        getTargetGenerators().addAll(targetGenerators);
    }
}

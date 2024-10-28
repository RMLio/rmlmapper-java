package be.ugent.rml;

import be.ugent.rml.term.Term;
import be.ugent.rml.termgenerator.TermGenerator;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class MappingInfo {
    private final Term term;
    private final TermGenerator termGenerator;
    private final List<Term> targets;
    private List<TermGenerator> targetGenerators;

    public MappingInfo(@Nonnull Term term, @Nonnull TermGenerator termGenerator, @Nonnull List<Term> targets, List<TermGenerator> targetGenerators) {
        this.term = term;
        this.termGenerator = termGenerator;
        this.targets = targets;
        this.targetGenerators = targetGenerators;
    }

    public MappingInfo(@Nonnull Term term, @Nonnull TermGenerator termGenerator) {
        this.term = term;
        this.termGenerator = termGenerator;
        this.targets = Collections.emptyList();
        this.targetGenerators = Collections.emptyList();
    }

    public MappingInfo(@Nonnull Term term, @Nonnull List<Term> targets, List<TermGenerator> targetGenerators) {
        this.term = term;
        this.termGenerator = null;
        this.targets = targets;
        this.targetGenerators = targetGenerators;
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

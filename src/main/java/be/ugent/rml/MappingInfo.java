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

    public MappingInfo(@Nonnull Term term, @Nonnull TermGenerator termGenerator, @Nonnull List<Term> targets) {
        this.term = term;
        this.termGenerator = termGenerator;
        this.targets = targets;
    }

    public MappingInfo(@Nonnull Term term, @Nonnull TermGenerator termGenerator) {
        this.term = term;
        this.termGenerator = termGenerator;
        this.targets = Collections.emptyList();
    }

    public MappingInfo(@Nonnull Term term, @Nonnull List<Term> targets) {
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

    public void addTargets(List<Term> targets) {
        this.targets.addAll(targets);
    }
}

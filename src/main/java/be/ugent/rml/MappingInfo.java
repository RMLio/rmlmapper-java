package be.ugent.rml;

import be.ugent.rml.term.Term;
import be.ugent.rml.termgenerator.TermGenerator;

public class MappingInfo {
    private Term term;
    private TermGenerator termGenerator;

    public MappingInfo(Term term, TermGenerator termGenerator) {
        this.term = term;
        this.termGenerator = termGenerator;
    }

    public Term getTerm() {
        return term;
    }

    public TermGenerator getTermGenerator() {
        return termGenerator;
    }
}

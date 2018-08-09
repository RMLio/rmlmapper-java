package be.ugent.rml.termgenerator;

import be.ugent.rml.functions.Function;
import be.ugent.rml.records.Record;
import be.ugent.rml.term.Term;

import java.util.List;

public abstract class TermGenerator {

    protected Function fn;

    public TermGenerator(Function fn) {
        this.fn = fn;
    }

    public abstract List<Term> generate(Record record);
}

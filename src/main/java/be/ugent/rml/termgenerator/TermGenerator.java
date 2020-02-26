package be.ugent.rml.termgenerator;

import be.ugent.rml.functions.SingleRecordFunctionExecutor;
import be.ugent.rml.records.Record;
import be.ugent.rml.term.Term;

import java.io.IOException;
import java.util.List;

public abstract class TermGenerator {

    protected SingleRecordFunctionExecutor functionExecutor;

    public TermGenerator(SingleRecordFunctionExecutor functionExecutor) {
        this.functionExecutor = functionExecutor;
    }

    public abstract List<Term> generate(Record record) throws Exception;

    /**
     * to string method
     * @return string
     */
    @Override
    public String toString() {
        return "gen(" + functionExecutor +
                ')';
    }
}

package be.ugent.rml.termgenerator;

import be.ugent.idlab.knows.dataio.record.Record;
import be.ugent.rml.functions.SingleRecordFunctionExecutor;
import be.ugent.rml.term.Term;

import java.util.List;

public abstract class TermGenerator {

    protected SingleRecordFunctionExecutor functionExecutor;

    private final boolean needsMagicEndValue;

    public TermGenerator(SingleRecordFunctionExecutor functionExecutor) {
        this.functionExecutor = functionExecutor;
        needsMagicEndValue = functionExecutor != null && functionExecutor.needsMagicEndValue();
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

    public boolean needsMagicEndValue() {
        return needsMagicEndValue;
    }
}

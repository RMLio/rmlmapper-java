package be.ugent.rml.termgenerator;

import be.ugent.idlab.knows.dataio.record.Record;
import be.ugent.rml.functions.SingleRecordFunctionExecutor;
import be.ugent.rml.term.Term;

import java.util.List;

public abstract class TermGenerator {

    protected SingleRecordFunctionExecutor functionExecutor;

    /**
     * Indicates whether the functionExecutor needs a special marker in the data to indicate End-of-File (EOF)
     */
    private final boolean needsEOFMarker;

    public TermGenerator(SingleRecordFunctionExecutor functionExecutor) {
        this.functionExecutor = functionExecutor;
        needsEOFMarker = functionExecutor != null && functionExecutor.needsEOFMarker();
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

    public boolean needsEOFMarker() {
        return needsEOFMarker;
    }
}

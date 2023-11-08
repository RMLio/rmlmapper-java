package be.ugent.rml.termgenerator;

import be.ugent.idlab.knows.dataio.record.Record;
import be.ugent.rml.functions.SingleRecordFunctionExecutor;
import org.eclipse.rdf4j.model.Value;

import java.util.List;

public abstract class TermGenerator {

    protected SingleRecordFunctionExecutor functionExecutor;

    public TermGenerator(SingleRecordFunctionExecutor functionExecutor) {
        this.functionExecutor = functionExecutor;
    }

    public abstract List<Value> generate(Record record) throws Exception;

    /**
     * to string method
     * @return string
     */
    @Override
    public String toString() {
        return "gen(" + functionExecutor +
                ')';
    }

    public boolean magic() {
        if (this.functionExecutor != null)
            return this.functionExecutor.magic();

        return false;
    }
}

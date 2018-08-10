package be.ugent.rml.termgenerator;

import be.ugent.rml.functions.FunctionExecutor;
import be.ugent.rml.functions.StaticFunctionExecutor;
import be.ugent.rml.functions.DynamicFunctionExecutor;
import be.ugent.rml.records.Record;
import be.ugent.rml.term.Term;

import java.io.IOException;
import java.util.List;

public abstract class TermGenerator {

    protected FunctionExecutor functionExecutor;

    public TermGenerator(FunctionExecutor functionExecutor) {
        this.functionExecutor = functionExecutor;
    }

    public abstract List<Term> generate(Record record) throws IOException;
}

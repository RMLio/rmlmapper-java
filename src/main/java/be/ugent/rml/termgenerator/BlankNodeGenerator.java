package be.ugent.rml.termgenerator;

import be.ugent.rml.Executor;
import be.ugent.rml.functions.FunctionUtils;
import be.ugent.rml.functions.SingleRecordFunctionExecutor;
import be.ugent.rml.records.Record;
import be.ugent.rml.term.BlankNode;
import be.ugent.rml.term.Term;

import java.util.ArrayList;
import java.util.List;

public class BlankNodeGenerator extends TermGenerator {

    public BlankNodeGenerator() {
        this(null);
    }

    public BlankNodeGenerator(SingleRecordFunctionExecutor functionExecutor) {
        super(functionExecutor);
    }

    @Override
    public List<Term> generate(Record record) throws Exception {
        ArrayList<Term> nodes = new ArrayList<>();

        if (this.functionExecutor != null) {
            List<String> objectStrings = new ArrayList<>();
            FunctionUtils.functionObjectToList(functionExecutor.execute(record), objectStrings);

            objectStrings.forEach(object -> {
                nodes.add(new BlankNode(object));
            });
        } else {
            nodes.add(new BlankNode("" + Executor.getNewBlankNodeID()));
        }

        return nodes;
    }
}

package be.ugent.rml.termgenerator;

import be.ugent.rml.functions.FunctionUtils;
import be.ugent.rml.functions.SingleRecordFunctionExecutor;
import be.ugent.rml.records.Record;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;

import java.util.ArrayList;
import java.util.List;

public class NamedNodeGenerator extends TermGenerator {

    public NamedNodeGenerator(SingleRecordFunctionExecutor functionExecutor) {
        super(functionExecutor);
    }

    @Override
    public List<Term> generate(Record record) throws Exception {
        List<String> objectStrings = new ArrayList<>();
        FunctionUtils.functionObjectToList(functionExecutor.execute(record), objectStrings);
        ArrayList<Term> objects = new ArrayList<>();

        if (objectStrings.size() > 0) {
            for (String object : objectStrings) {
                //todo check valid IRI
                objects.add(new NamedNode(object));
            }
        }

        return objects;
    }
}

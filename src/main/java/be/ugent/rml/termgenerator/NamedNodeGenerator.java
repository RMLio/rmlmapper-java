package be.ugent.rml.termgenerator;

import be.ugent.rml.functions.Function;
import be.ugent.rml.records.Record;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;

import java.util.ArrayList;
import java.util.List;

public class NamedNodeGenerator extends TermGenerator {

    public NamedNodeGenerator(Function fn) {
        super(fn);
    }

    @Override
    public List<Term> generate(Record record) {
        ArrayList<Term> objects = new ArrayList<>();
        List<String> objectStrings = (List<String>) this.fn.execute(record);

        if (objectStrings.size() > 0) {
            for (String object : objectStrings) {
                //todo check valid IRI
                objects.add(new NamedNode(object));
            }
        }

        return objects;
    }
}

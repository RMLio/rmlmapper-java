package be.ugent.rml.termgenerator;

import be.ugent.rml.Executor;
import be.ugent.rml.functions.Function;
import be.ugent.rml.records.Record;
import be.ugent.rml.term.BlankNode;
import be.ugent.rml.term.Term;

import java.util.ArrayList;
import java.util.List;

public class BlankNodeGenerator extends TermGenerator {

    public BlankNodeGenerator() {
        this(null);
    }

    public BlankNodeGenerator(Function fn) {
        super(fn);
    }

    @Override
    public List<Term> generate(Record record) {
        ArrayList<Term> nodes = new ArrayList<>();

        if (this.fn != null) {
            List<String> objectStrings = (List<String>) this.fn.execute(record);

            objectStrings.forEach(object -> {
                nodes.add(new BlankNode(object));
            });
        } else {
            nodes.add(new BlankNode("" + Executor.getNewBlankNodeID()));
        }

        return nodes;
    }
}

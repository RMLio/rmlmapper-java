package be.ugent.rml.termgenerator;

import be.ugent.idlab.knows.dataio.record.Record;
import be.ugent.rml.Executor;
import be.ugent.rml.functions.FunctionUtils;
import be.ugent.rml.functions.SingleRecordFunctionExecutor;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.ArrayList;
import java.util.List;

public class BlankNodeGenerator extends TermGenerator {

    private static final ValueFactory valueFactory = SimpleValueFactory.getInstance();


    public BlankNodeGenerator() {
        this(null);
    }

    public BlankNodeGenerator(SingleRecordFunctionExecutor functionExecutor) {
        super(functionExecutor);
    }

    @Override
    public List<Value> generate(Record record) throws Exception {
        ArrayList<Value> nodes = new ArrayList<>();

        if (this.functionExecutor != null) {
            List<String> objectStrings = FunctionUtils.functionObjectToList(functionExecutor.execute(record));

            objectStrings.forEach(object -> {
                nodes.add(valueFactory.createBNode(object));
            });
        } else {
            nodes.add(valueFactory.createBNode( Executor.getNewBlankNodeID()));
        }

        return nodes;
    }
}

package be.ugent.rml.termgenerator;

import be.ugent.idlab.knows.dataio.record.Record;
import be.ugent.rml.StrictMode;
import be.ugent.rml.functions.FunctionUtils;
import be.ugent.rml.functions.SingleRecordFunctionExecutor;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import org.eclipse.rdf4j.common.net.ParsedIRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class NamedNodeGenerator extends TermGenerator {
    private static final Logger logger = LoggerFactory.getLogger(NamedNodeGenerator.class);

    // Base IRI to prepend to a relative IRI to make it absolute.
    private final String baseIRI;

    // StrictMode determines RMLMapper's behaviour when an IRI for a NamedNode is invalid.
    // If set to BEST_EFFORT, RMLMapper will not generate a NamedNode and go on.
    // If set to STRICT, RMLMapper will stop execution with an exception.
    private final StrictMode strictMode;

    public NamedNodeGenerator(final SingleRecordFunctionExecutor functionExecutor, final String baseIRI, final StrictMode strictMode) {
        super(functionExecutor);
        this.strictMode = strictMode;

        if (baseIRI == null) {
            this.baseIRI = "";
        } else {
            this.baseIRI = baseIRI;
        }
    }

    @Override
    public List<Term> generate(Record record) throws Exception {
        List<String> objectStrings = FunctionUtils.functionObjectToList(functionExecutor.execute(record));
        List<Term> objects = new ArrayList<>();

        if (!objectStrings.isEmpty()) {
            for (String object : objectStrings) {
                String iri = object;

                /* Detect relative IRIs and append base IRI if needed */
                if (object.indexOf(':') < 0)
                    iri = baseIRI + object;

                /* Validate IRIs only in STRICT mode as this is a very expensive operation */
                if (strictMode.equals(StrictMode.STRICT)) {
                    try {
                        new ParsedIRI(iri);
                    } catch (Exception e) {
                        logger.error("'" + iri + "' is not a valid IRI");
                        throw new Exception("'" + iri + "' is not a valid IRI");
                    }
                } else {
                    /* Basic IRI validation */
                    if (iri.contains(" ")) {
                        continue;
                    }
                }

                objects.add(new NamedNode(iri));
            }
        }

        return objects;
    }
}

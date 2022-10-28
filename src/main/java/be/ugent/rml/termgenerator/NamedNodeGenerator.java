package be.ugent.rml.termgenerator;

import be.ugent.rml.StrictMode;
import be.ugent.rml.functions.FunctionUtils;
import be.ugent.rml.functions.SingleRecordFunctionExecutor;
import be.ugent.rml.records.Record;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import org.eclipse.rdf4j.common.net.ParsedIRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
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
        this.baseIRI = baseIRI;
        this.strictMode = strictMode;
    }

    @Override
    public List<Term> generate(Record record) throws Exception {
        List<String> objectStrings = FunctionUtils.functionObjectToList(functionExecutor.execute(record));
        ArrayList<Term> objects = new ArrayList<>();

        if (objectStrings.size() > 0) {
            for (String object : objectStrings) {
                try {
                    // check if IRI is valid
                    ParsedIRI parsedIRI = new ParsedIRI(object);
                    if (!parsedIRI.isAbsolute()) {
                        final String iriWithBase = baseIRI + object;
                        try {
                            new ParsedIRI(iriWithBase);
                            objects.add(new NamedNode(iriWithBase));
                        } catch (URISyntaxException ue) {
                            if (strictMode.equals(StrictMode.STRICT)) {
                                throw new Exception("The base IRI is not valid, so relative IRI '" + object + "' cannot be turned in to absolute IRI.");
                            }
                            logger.error("IRI '{}' Is not valid. Skipped.", iriWithBase);
                        }
                    } else {
                        objects.add(new NamedNode(object));
                    }
                } catch (URISyntaxException e) {
                    if (strictMode.equals(StrictMode.STRICT)) {
                        throw new Exception("IRI " + object + " is not valid");
                    }
                    logger.error("IRI '{}' is not valid. Skipped.", object);
                }
            }
        }

        return objects;
    }
}

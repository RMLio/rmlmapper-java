package be.ugent.rml.termgenerator;

import be.ugent.knows.idlabFunctions.IDLabFunctions;
import be.ugent.rml.StrictMode;
import be.ugent.rml.functions.FunctionUtils;
import be.ugent.rml.functions.SingleRecordFunctionExecutor;
import be.ugent.rml.records.Record;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import com.github.jsonldjava.core.RDFDataset;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIException;
import org.eclipse.rdf4j.common.net.ParsedIRI;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
                String iri = object;

                /* Detect relative IRIs and append base IRI if needed */
                if (object.indexOf(':') < 0)
                    iri = baseIRI + object;

                /* Validate IRIs only in STRICT mode as this is a very expensive operation */
                if (strictMode.equals(StrictMode.STRICT)) {
                    try {
                        new ParsedIRI(iri);
                    } catch (IRIException e) {
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

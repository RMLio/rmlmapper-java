package be.ugent.rml;

import be.ugent.rml.extractor.ConstantExtractor;
import be.ugent.rml.extractor.ReferenceExtractor;
import be.ugent.rml.functions.ConcatFunction;
import be.ugent.rml.functions.SingleRecordFunctionExecutor;
import be.ugent.rml.store.QuadStore;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.List;

public class RecordFunctionExecutorFactory {

    private static final ValueFactory valueFactory = SimpleValueFactory.getInstance();


    public static SingleRecordFunctionExecutor generate(QuadStore store, Value termMap, boolean encodeURI, boolean ignoreDoubleQuotes) {
        List<Value> references = Utils.getObjectsFromQuads(store.getQuads(termMap, valueFactory.createIRI(NAMESPACES.RML + "reference"), null));
        List<Value> templates = Utils.getObjectsFromQuads(store.getQuads(termMap, valueFactory.createIRI(NAMESPACES.RR + "template"), null));
        List<Value> constants = Utils.getObjectsFromQuads(store.getQuads(termMap, valueFactory.createIRI(NAMESPACES.RR + "constant"), null));

        if (!references.isEmpty()) {
            return new ReferenceExtractor(references.get(0).stringValue(), ignoreDoubleQuotes);
        } else if (!templates.isEmpty()) {
            return new ConcatFunction(Utils.parseTemplate(templates.get(0).stringValue(), ignoreDoubleQuotes), encodeURI);
        } else if (!constants.isEmpty()) {
            return new ConstantExtractor(constants.get(0).stringValue());
        } else {
            return null;
        }
    }
}

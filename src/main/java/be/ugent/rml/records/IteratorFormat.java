package be.ugent.rml.records;

import be.ugent.rml.NAMESPACES;
import be.ugent.rml.Utils;
import be.ugent.rml.access.Access;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

public abstract class IteratorFormat<DocumentClass> implements ReferenceFormulationRecordFactory {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    private HashMap<Access, DocumentClass> documentMap = new HashMap<>();

    @Override
    public List<Record> getRecords(Access access, Term logicalSource, QuadStore rmlStore) throws IOException {
        if (! documentMap.containsKey(access)) {
            logger.debug("No document found for {}. Creating new one", access);
            InputStream stream = access.getInputStream();
            documentMap.put(access, getDocumentFromStream(stream));
        }

        List<Term> iterators = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML + "iterator"), null));

        if (!iterators.isEmpty()) {
            String iterator = iterators.get(0).getValue();
            return getRecordsFromDocument(documentMap.get(access), iterator);
        } else {
            // TODO better message
            throw new Error("An iterator is missing.");
        }
    }

    abstract List<Record> getRecordsFromDocument(DocumentClass document, String iterator) throws IOException;

    abstract DocumentClass getDocumentFromStream(InputStream stream) throws IOException;

//    abstract String getContentType();
}

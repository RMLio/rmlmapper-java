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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

/**
 * This an abstract class for reference formulation-specific record factories that use iterators.
 * @param <DocumentClass>: the class used to represent a format-specific document that can be reused.
 */
public abstract class IteratorFormat<DocumentClass> implements ReferenceFormulationRecordFactory {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    private HashMap<Access, DocumentClass> documentMap = new HashMap<>();

    /**
     * This method returns a list of records for a data source.
     * @param access the access from which records need to be fetched.
     * @param logicalSource the used Logical Source.
     * @param rmlStore the QuadStore with the RML rules.
     * @return a list of records.
     * @throws IOException
     */
    @Override
    public List<Record> getRecords(Access access, Term logicalSource, QuadStore rmlStore) throws IOException, SQLException, ClassNotFoundException {
        // Check if the needed document is already in the cache.
        // If not, a new one is created, based on the InputStream from the access.
        if (! documentMap.containsKey(access)) {
            logger.debug("No document found for {}. Creating new one", access);
            InputStream stream = access.getInputStream();
            documentMap.put(access, getDocumentFromStream(stream, access.getContentType()));
        }

        List<Term> iterators = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML + "iterator"), null));

        if (!iterators.isEmpty()) {
            String iterator = iterators.get(0).getValue();
            return getRecordsFromDocument(documentMap.get(access), iterator);
        } else {
            /* 
             * PostgresSQL builds its XML on the fly and can be accessed 
             * with a fixed XPath iterator '/Results/row'
             */
            String iterator = "/Results/row";
            return getRecordsFromDocument(documentMap.get(access), iterator);
        }
    }

    /**
     * This method returns the records from a document based on an iterator.
     * @param document the document from which records need to get.
     * @param iterator the used iterator.
     * @return a list of records.
     * @throws IOException
     */
    abstract List<Record> getRecordsFromDocument(DocumentClass document, String iterator) throws IOException;

    /**
     * This method returns a document from an InputStream.
     * @param stream the used InputStream.
     * @return a format-specific document.
     * @throws IOException
     */
    abstract DocumentClass getDocumentFromStream(InputStream stream) throws IOException;

    DocumentClass getDocumentFromStream(InputStream stream, String contentType) throws IOException {
        return getDocumentFromStream(stream);
    }
}

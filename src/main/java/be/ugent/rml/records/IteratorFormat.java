package be.ugent.rml.records;

import be.ugent.rml.Utils;
import be.ugent.rml.access.Access;
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
    public List<Record> getRecords(Access access, String iterator) throws IOException {
        if (! documentMap.containsKey(access)) {
            logger.debug("No document found for {}. Creating new one", access);
            InputStream stream = access.getInputStream();
            documentMap.put(access, getDocumentFromStream(stream));
        }

        return getRecordsFromDocument(documentMap.get(access), iterator);
    }

    abstract List<Record> getRecordsFromDocument(DocumentClass document, String iterator) throws IOException;

    abstract DocumentClass getDocumentFromStream(InputStream stream) throws IOException;

    abstract String getContentType();
}

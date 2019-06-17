package be.ugent.rml.records;

import be.ugent.rml.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

public abstract class IteratorFormat<DocumentClass> {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    private HashMap<String, DocumentClass> documentMap = new HashMap<>();

    public List<Record> get(String location, String iterator) throws IOException {
        return get(location, iterator, System.getProperty("user.dir"));
    }

    public List<Record> get(String location, String iterator, String cwd) throws IOException {
        if (! documentMap.containsKey(location)) {
            logger.debug("No document found for {}. Creating new one", location);
            InputStream stream = Utils.getInputStreamFromLocation(location, new File(cwd), getContentType());
            documentMap.put(location, getDocumentFromStream(stream));
        }

        return getRecordsFromDocument(documentMap.get(location), iterator);
    }

    abstract List<Record> getRecordsFromDocument(DocumentClass document, String iterator) throws IOException;

    abstract DocumentClass getDocumentFromStream(InputStream stream) throws IOException;

    abstract String getContentType();
}

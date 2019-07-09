package be.ugent.rml.records;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a record factory that creates JSON records.
 */
public class JSONRecordFactory extends IteratorFormat<Object> implements ReferenceFormulationRecordFactory {

    /**
     * This method returns the records from a JSON document based on an iterator.
     * @param document the document from which records need to get.
     * @param iterator the used iterator.
     * @return a list of records.
     * @throws IOException
     */
    @Override
    List<Record> getRecordsFromDocument(Object document, String iterator) throws IOException {
        List<Record> records = new ArrayList<>();

        Configuration conf = Configuration.builder()
                .options(Option.AS_PATH_LIST).build();

        try {
            List<String> pathList = JsonPath.using(conf).parse(document).read(iterator);

            for(String p :pathList) {
                records.add(new JSONRecord(document, p));
            }
        } catch(PathNotFoundException e) {
            logger.warn(e.getMessage(), e);
        }

        return records;
    }

    /**
     * This method returns a JSON document from an InputStream.
     * @param stream the used InputStream.
     * @return a JSON document.
     * @throws IOException
     */
    @Override
    Object getDocumentFromStream(InputStream stream) throws IOException {
        return Configuration.defaultConfiguration().jsonProvider().parse(stream, "utf-8");
    }
}

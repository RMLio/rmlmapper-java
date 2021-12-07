package be.ugent.rml.records;

import com.jayway.jsonpath.*;
import com.jayway.jsonpath.spi.json.JsonProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a record factory that creates JSON records.
 */
public class JSONRecordFactory extends IteratorFormat<Object> {

    /**
     * This method returns the records from a JSON document based on an iterator.
     * @param document the document from which records need to get.
     * @param iterator the used iterator.
     * @return a list of records.
     */
    @Override
    List<Record> getRecordsFromDocument(Object document, String iterator) {
        List<Record> records = new ArrayList<>();

        Configuration conf = Configuration.builder()
                .options(Option.AS_PATH_LIST).build();

        // This JSONPath library specifically cannot handle keys with commas, so we need to escape it
        String escapedIterator = iterator.replaceAll(",", "\\\\,");

        try {
            List<String> pathList = JsonPath.using(conf).parse(document).read(escapedIterator);

            for(String p :pathList) {
                records.add(new JSONRecord(document, p));
            }
        } catch (JsonPathException e) {
            logger.warn(e.getMessage() + " for iterator " + iterator, e);
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

    @Override
    Object getDocumentFromStream(InputStream stream, String contentType) throws IOException {
        if(contentType.toLowerCase().equals("jsonl")){
            JsonProvider provider = Configuration.defaultConfiguration().jsonProvider();
            BufferedReader lineReader = new BufferedReader(new InputStreamReader(stream));
            Object items = provider.createArray();
            int index = 0;
            while (lineReader.ready()){
                provider.setArrayIndex(items, index, provider.parse(lineReader.readLine()));
                index += 1;
            }
            return items;
        } else {
            return getDocumentFromStream(stream);
        }
    }
}

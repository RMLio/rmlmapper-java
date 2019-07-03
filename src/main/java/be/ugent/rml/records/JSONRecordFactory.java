package be.ugent.rml.records;

import com.jayway.jsonpath.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JSONRecordFactory extends IteratorFormat<Object> implements ReferenceFormulationRecordFactory {

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

    @Override
    Object getDocumentFromStream(InputStream stream) throws IOException {
        return Configuration.defaultConfiguration().jsonProvider().parse(stream, "utf-8");
    }

    protected String getContentType() {
        return "application/json";
    }
}

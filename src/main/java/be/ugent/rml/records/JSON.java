package be.ugent.rml.records;

import com.jayway.jsonpath.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JSON extends IteratorFormat {

    protected String getContentType() {
        return "application/json";
    }

    protected List<Record> _get(InputStream stream, String iterator) throws IOException {
        List<Record> records = new ArrayList<>();

        Object document = Configuration.defaultConfiguration().jsonProvider().parse(stream, "utf-8");

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
}

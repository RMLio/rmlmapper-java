package be.ugent.rml.records;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import org.apache.jena.query.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SPARQL extends IteratorFormat {

    @Override
    public List<Record> get(String endpoint, String qs, String iterator) {
        List<Record> records = new ArrayList<>();
        // Query the endpoint
        Query query = QueryFactory.create(qs);
        System.out.println(qs);
        QueryExecution exec = QueryExecutionFactory.sparqlService( endpoint, qs);
        ResultSet results = exec.execSelect();

        // Convert resultset to JSON
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsJSON(outputStream, results);

        Object document = Configuration.defaultConfiguration().jsonProvider().parse(new ByteArrayInputStream(outputStream.toByteArray()), "utf-8");

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
    List<Record> _get(InputStream stream, String iterator) throws IOException {
        return null;
    }
}

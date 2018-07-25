package be.ugent.rml.records;

import org.apache.jena.query.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SPARQL extends IteratorFormat {

    // Not important here
    @Override
    String getContentType() {
        return null;
    }

    @Override
    public List<Record> get(String endpoint, String qs, String iterator) {
        List<Record> records = new ArrayList<>();
        // Query the endpoint
        try {
            Query query = QueryFactory.create(qs);
        } catch (Exception ex) {
            throw new Error("Could not parse the following SPARQL query: " + qs);
        }
        QueryExecution exec = QueryExecutionFactory.sparqlService(endpoint, qs);

        ResultSet results = exec.execSelect();
        ResultSet resultsC = ResultSetFactory.copyResults( exec.execSelect() );


        // Convert ResultSet to JSON
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ResultSetFormatter.outputAsJSON(outputStream, results);

        // todo: remove this before merging to develop
        System.out.println("---------------------------------------");
        System.out.println("QUERY: " + qs);
        System.out.println("RESULTS: ");
        ResultSetFormatter.outputAsJSON(resultsC);
        System.out.println("---------------------------------------");

        try {
            records = _get(new ByteArrayInputStream(outputStream.toByteArray()), iterator);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return records;
    }

    @Override
    List<Record> _get(InputStream stream, String iterator) throws IOException {
        JSON json = new JSON();
        return json._get(stream, iterator);
    }
}

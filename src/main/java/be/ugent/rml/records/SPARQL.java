package be.ugent.rml.records;

import org.apache.jena.query.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

public class SPARQL extends IteratorFormat {

    @Override
    public List<Record> get(String endpoint, String qs, String iterator) {
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

        System.out.println("---------------------------------------");
        System.out.println("QUERY: " + qs);
        System.out.println("RESULTS: ");
        ResultSetFormatter.outputAsJSON(resultsC);
        System.out.println("---------------------------------------");

        return _get(new ByteArrayInputStream(outputStream.toByteArray()), iterator);
    }

    @Override
    List<Record> _get(InputStream stream, String iterator) {
        JSON json = new JSON();
        return json._get(stream, iterator);
    }
}

package be.ugent.rml.records;

import be.ugent.rml.Utils;
import org.apache.jena.query.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SPARQL {

    public enum ResultFormat {
        NOT_SPECIFIED ("Not specified", "", "", ""),
        XML ("XML", "http://www.w3.org/ns/formats/SPARQL_Results_XML", "application/sparql-results+xml", "http://semweb.mmlab.be/ns/ql#XPath"),
        JSON ("JSON", "http://www.w3.org/ns/formats/SPARQL_Results_JSON", "application/sparql-results+json", "http://semweb.mmlab.be/ns/ql#JSONPath"),
        CSV ("CSV", "http://www.w3.org/ns/formats/SPARQL_Results_CSV", "text/csv", "http://semweb.mmlab.be/ns/ql#CSV");

        private final String name;
        private final String uri;
        private final String mediaType;
        private final String referenceFormulation;


        private ResultFormat(String name, String uri, String mediaType, String referenceFormulation) {
            this.name = name;
            this.uri = uri;
            this.mediaType = mediaType;
            this.referenceFormulation = referenceFormulation;
        }

        public String getUri() { return uri; }

        public String getMediaType() { return mediaType; }

        public String getReferenceFormulation() { return referenceFormulation; }

        public String toString() {
            return this.name;
        }
    }

    public List<Record> get(String endpoint, String qs, String iterator, ResultFormat resultFormat) {
        switch(resultFormat) {
            case XML:
                return getXMLRecords(endpoint, qs, iterator);
            case JSON:
                return getJSONRecords(endpoint, qs, iterator);
            case CSV:
                return getCSVRecords(endpoint, qs, iterator);
            default:
                return getNotSpecifiedRecords(endpoint, qs, iterator);
        }
    }

    private List<Record> getXMLRecords(String endpoint, String qs, String iterator) {
        String content = getSPARQLResults(endpoint, qs, ResultFormat.XML);
        XML xml = new XML();
        try {
            return xml._get(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), iterator);
        } catch (IOException ex) {
            throw new Error("Could not convert XML into XMLRecords. Error message: " + ex.getMessage());
        }
    }

    private List<Record> getJSONRecords(String endpoint, String qs, String iterator) {
        String content = getSPARQLResults(endpoint, qs, ResultFormat.JSON);
        JSON json = new JSON();
        try {
            return json._get(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), iterator);
        } catch (IOException ex) {
            throw new Error("Could not convert XML into XMLRecords. Error message: " + ex.getMessage());
        }
    }

    private List<Record> getCSVRecords(String endpoint, String qs, String iterator) {
        String content = getSPARQLResults(endpoint, qs, ResultFormat.CSV);
        CSV csv = new CSV();
        try {
            return csv._get(new StringReader(content));
        } catch (IOException ex) {
            throw new Error("Could not convert XML into XMLRecords. Error message: " + ex.getMessage());
        }
    }

    // NS = Not Specified
    private List<Record> getNotSpecifiedRecords(String endpoint, String qs, String iterator) {
        List<Record> records = new ArrayList<>();
        try {
            Query query = QueryFactory.create(qs);
            QueryExecution exec = QueryExecutionFactory.sparqlService(endpoint, query);

            ResultSet results = exec.execSelect();

            // Convert ResultSet to JSON
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ResultSetFormatter.outputAsJSON(outputStream, results);

            try {
                JSON json = new JSON();
                records = json._get(new ByteArrayInputStream(outputStream.toByteArray()), iterator);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            return records;
        } catch (Exception ex) {
            throw new Error("Could not get SPARQL records. Error message: " + ex.getMessage());
        }

    }

    private String getSPARQLResults(String endpoint, String qs, ResultFormat resultFormat) {
        // Query the endpoint
        try {
            URL url = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Set 'Accept' header
            connection.setRequestProperty("Accept", resultFormat.getMediaType());

            // Set 'query' parameter
            Map<String,String> urlParams = new HashMap<String, String>(){{
                put("query", qs);
            }};
            connection.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.writeBytes(Utils.getURLParamsString(urlParams));
            out.flush();
            out.close();

            int status = connection.getResponseCode();

            StringBuffer stringBuffer = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            int i;
            while ((i = reader.read()) != -1) {
                char c = (char) i;
                if(c == '\n') {
                    stringBuffer.append("\n");
                }else {
                    stringBuffer.append(String.valueOf(c));
                }
            }
            reader.close();

            return stringBuffer.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Error(ex.getMessage());
        }
    }
}

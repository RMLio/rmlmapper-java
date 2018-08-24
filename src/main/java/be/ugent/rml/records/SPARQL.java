package be.ugent.rml.records;

import be.ugent.rml.Utils;
import org.eclipse.rdf4j.query.resultio.UnsupportedQueryResultFormatException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SPARQL {

    // The first referenceFormulation is seen as default
    public enum ResultFormat {
        XML ("XML", "http://www.w3.org/ns/formats/SPARQL_Results_XML", "application/sparql-results+xml",
                // referenceFormulations:
                "http://semweb.mmlab.be/ns/ql#XPath"
        ),
        JSON ("JSON", "http://www.w3.org/ns/formats/SPARQL_Results_JSON", "application/sparql-results+json",
                // referenceFormulations:
                "http://semweb.mmlab.be/ns/ql#JSONPath"
        ),
        CSV ("CSV", "http://www.w3.org/ns/formats/SPARQL_Results_CSV", "text/csv",
                // referenceFormulations:
                "http://semweb.mmlab.be/ns/ql#CSV"
        );

        private final String name;
        private final String uri;
        private final String mediaType;
        private final Set<String> referenceFormulations;


        ResultFormat(String name, String uri, String mediaType, String... referenceFormulations) {
            this.name = name;
            this.uri = uri;
            this.mediaType = mediaType;
            this.referenceFormulations = new HashSet<>(Arrays.asList(referenceFormulations));
        }

        public String getUri() { return uri; }

        public String getMediaType() { return mediaType; }

        public Set<String> getReferenceFormulations() { return referenceFormulations; }

        public String toString() {
            return this.name;
        }
    }

    /**
     *
     * @param endpoint SPARQL endpoint
     * @param qs QueryString
     * @param iterator result iterator
     * @param resultFormat result format
     * @param referenceFormulation given because we might it them later for referenceFormulation specific implementations
     * @return records
     */
    public List<Record> get(String endpoint, String qs, String iterator, ResultFormat resultFormat, String referenceFormulation) {
        switch(resultFormat) {
            case XML:
                return getXMLRecords(endpoint, qs, iterator, referenceFormulation);
            case JSON:
                return getJSONRecords(endpoint, qs, iterator, referenceFormulation);
            case CSV:
                return getCSVRecords(endpoint, qs, iterator, referenceFormulation);
            default:
                throw new UnsupportedQueryResultFormatException("Format with URI: " + resultFormat.getUri() + " not recognised.");
        }
    }

    private List<Record> getXMLRecords(String endpoint, String qs, String iterator, String referenceFormulation) {
        String content = getSPARQLResults(endpoint, qs, ResultFormat.XML);
        XML xml = new XML();
        try {
            return xml._get(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), iterator);
        } catch (IOException ex) {
            throw new Error("Could not convert XML into XMLRecords. Error message: " + ex.getMessage());
        }
    }

    private List<Record> getJSONRecords(String endpoint, String qs, String iterator, String referenceFormulation) {
        String content = getSPARQLResults(endpoint, qs, ResultFormat.JSON);
        JSON json = new JSON();
        try {
            return json._get(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), iterator);
        } catch (IOException ex) {
            throw new Error("Could not convert XML into XMLRecords. Error message: " + ex.getMessage());
        }
    }

    private List<Record> getCSVRecords(String endpoint, String qs, String iterator, String referenceFormulation) {
        String content = getSPARQLResults(endpoint, qs, ResultFormat.CSV);
        CSV csv = new CSV();
        try {
            return csv._get(new StringReader(content));
        } catch (IOException ex) {
            throw new Error("Could not convert XML into XMLRecords. Error message: " + ex.getMessage());
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

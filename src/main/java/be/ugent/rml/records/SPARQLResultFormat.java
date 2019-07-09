package be.ugent.rml.records;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This enum represents the different SPARQL result formats.
 */
public enum SPARQLResultFormat {

    XML("XMLRecordFactory", "http://www.w3.org/ns/formats/SPARQL_Results_XML", "application/sparql-results+xml",
            // referenceFormulations:
            "http://semweb.mmlab.be/ns/ql#XPath"
    ),
    JSON("JSONRecordFactory", "http://www.w3.org/ns/formats/SPARQL_Results_JSON", "application/sparql-results+json",
            // referenceFormulations:
            "http://semweb.mmlab.be/ns/ql#JSONPath"
    ),
    CSV("CSV", "http://www.w3.org/ns/formats/SPARQL_Results_CSV", "text/csv",
            // referenceFormulations:
            "http://semweb.mmlab.be/ns/ql#CSV"
    );

    private final String name;
    private final String uri;
    private final String contentType;
    private final Set<String> referenceFormulations;

    SPARQLResultFormat(String name, String uri, String contentType, String... referenceFormulations) {
        this.name = name;
        this.uri = uri;
        this.contentType = contentType;
        this.referenceFormulations = new HashSet<>(Arrays.asList(referenceFormulations));
    }

    /**
     * This method returns the uri of the format.
     * @return the uri of the format.
     */
    public String getUri() {
        return uri;
    }

    /**
     * This method returns the content type of the format.
     * @return the content type of the format.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * This method returns the reference formulation of the format.
     * @return the reference formulation of the format.
     */
    public Set<String> getReferenceFormulations() {
        return referenceFormulations;
    }

    /**
     * This method returns a String representation of the format, based on the format's name.
     * @return String representation of the format.
     */
    public String toString() {
        return this.name;
    }
}
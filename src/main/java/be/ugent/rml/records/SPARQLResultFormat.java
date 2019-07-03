package be.ugent.rml.records;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
    private final String mediaType;
    private final Set<String> referenceFormulations;


    SPARQLResultFormat(String name, String uri, String mediaType, String... referenceFormulations) {
        this.name = name;
        this.uri = uri;
        this.mediaType = mediaType;
        this.referenceFormulations = new HashSet<>(Arrays.asList(referenceFormulations));
    }

    public String getUri() {
        return uri;
    }

    public String getMediaType() {
        return mediaType;
    }

    public Set<String> getReferenceFormulations() {
        return referenceFormulations;
    }

    public String toString() {
        return this.name;
    }

}

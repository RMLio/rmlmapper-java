package be.ugent.rml.metadata;

import org.eclipse.rdf4j.model.Value;

/**
 * Holds the source triplesMap and Subject-, Object- or PredicateMap for a specific (provenanced) term.
 */
public class Metadata {

    private Value triplesMap;
    private Value sourceMap;

    public Metadata() {
    }

    public Metadata(Value triplesMap) {
        this(triplesMap, null);
    }

    public Metadata(Value triplesMap, Value sourceMap) {
        this.triplesMap = triplesMap;
        this.sourceMap = sourceMap;
    }

    Value getTriplesMap() {
        return triplesMap;
    }

    Value getSourceMap() {
        return sourceMap;
    }

    public void setSourceMap(Value sourceMap) {
        this.sourceMap = sourceMap;
    }
}

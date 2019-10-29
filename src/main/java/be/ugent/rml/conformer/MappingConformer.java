package be.ugent.rml.conformer;

import be.ugent.rml.Utils;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static be.ugent.rml.NAMESPACES.*;

/**
 * Only validates by checking for at least one TriplesMap.
 * Converts mapping files to RML. Currently only R2RML converter is implemented.
 * InputStream of mapping file is used to create a store. TriplesMaps in store that need conversion
 * are detected by applying the converters detection methods and saved. convert tries to convert these
 * to RML. Exceptions can be raised during validation and conversion, which the caller has to handle.
 * Output of detect() informs if convert() should be used to convert to valid RML.
 * The validated RML can be returned as a QuadStore with getStore().
 */
public class MappingConformer {

    private QuadStore store;
    private List<Term> unconvertedTriplesMaps = new ArrayList<>();
    private Map<String, String> mappingOptions;

    /**
     * Create MappingConformer from InputStream of mapping file in RDF.
     * @param store A QuadStore with the mapping rules.
     * @throws FileNotFoundException
     */
    public MappingConformer(QuadStore store) throws Exception {
        this(store, null);
    }

    /**
     * Create MappingConformer from InputStream of mapping file in RDF.
     * @param store A QuadStore with the mapping rules.
     * @throws FileNotFoundException
     */
    public MappingConformer(QuadStore store, Map<String, String> mappingOptions) throws Exception {
        this.store = store;
        this.mappingOptions = mappingOptions;
    }

    /**
     * This method makes the QuadStore conformant to the RML spec.
     * @return True if the store had to be updated, else false.
     * @throws Exception if something goes wrong during detection or conversion.
     */
    public boolean conform() throws Exception {
        boolean conversionNeeded = this.detect();

        if (conversionNeeded) {
            this.convert();
        }

        return conversionNeeded;
    }

    /**
     * Detect if mapping file is valid RML.
     * @return true if valid RML, false if conversion is needed
     * @throws Exception if invalid or unconvertable
     */
    private boolean detect() throws Exception {
        // TODO generalise for multiple converters
        Converter converter = new R2RMLConverter(store);

        List<Term> triplesMaps = Utils.getSubjectsFromQuads(store
                .getQuads(
                        null,
                        new NamedNode(RDF + "type"),
                        new NamedNode(RR + "TriplesMap")));

        if (triplesMaps.isEmpty()) {
            throw new Exception("Mapping requires at least one TriplesMap");
        }

        // Find all triples maps
        // This could be more efficient with a while loop,
        // but these TriplesMaps are needed in any case when calling convert().
        for (Term triplesMap : triplesMaps) {
            if (converter.detect(triplesMap)) {
                unconvertedTriplesMaps.add(triplesMap);
            }
        }

        return ! unconvertedTriplesMaps.isEmpty();
    }

    /**
     * Tries to convert to RML. Model should still be valid on failure
     * @throws Exception conversion failed
     */
    private void convert() throws Exception {
        // TODO generalise for multiple converters
        Converter converter = new R2RMLConverter(store);

        for (Term unconvertedTriplesMap : unconvertedTriplesMaps) {
            converter.convert(unconvertedTriplesMap, mappingOptions);
        }
    }

    /**
     * Debugging helper function to check difference of models
     * @param store QuadStore which subtracts
     * @return boolean this.store isSubset of  given store
     */
    boolean differenceInConformer(QuadStore store) {
        return this.store.isSubset(store);
    }

    /**
     * Debugging helper function to check difference of models
     * @param store QuadStore which subtracts
     * @return boolean given store isSubset of store
     */
    boolean differenceInGivenStore(QuadStore store) {
        return store.isSubset(this.store);
    }

    /**
     * Get a valid QuadStore
     * @return a valid QuadStore
     */
    public QuadStore getStore() {
        return store;
    }
}

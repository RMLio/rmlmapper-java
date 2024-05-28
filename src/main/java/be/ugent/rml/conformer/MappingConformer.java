package be.ugent.rml.conformer;

import be.ugent.rml.Utils;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import static be.ugent.rml.NAMESPACES.*;

/**
 * Only validates by checking for at least one TriplesMap.
 * Converts mapping files to W3C's Knowledge Graph Community Group RML from RML and R2RML.
 * InputStream of mapping file is used to create a store. TriplesMaps in store that need conversion
 * are detected by applying the converters detection methods and saved. convert tries to convert these
 * to W3C RML. Exceptions can be raised during validation and conversion, which the caller has to handle.
 * Output of detect() informs if convert() should be used to convert to valid W3C RML.
 * The validated RML can be returned as a QuadStore with getStore().
 */
public class MappingConformer {

    public enum Dialect {
        RML, // Old RML
        R2RML, // W3C's R2RML
        RML2 // W3C's Knowledge Graph Construction Community Group RML
    }

    private QuadStore store;
    private Map<String, String> mappingOptions;

    /**
     * Create MappingConformer from InputStream of mapping file in RDF.
     *
     * @param store A QuadStore with the mapping rules.
     * @throws FileNotFoundException
     */
    public MappingConformer(QuadStore store) throws Exception {
        this(store, null);
    }

    /**
     * Create MappingConformer from InputStream of mapping file in RDF.
     *
     * @param store A QuadStore with the mapping rules.
     * @throws FileNotFoundException
     */
    public MappingConformer(QuadStore store, Map<String, String> mappingOptions) throws Exception {
        this.store = store;
        this.mappingOptions = mappingOptions;
    }

    /**
     * This method makes the QuadStore conformant to the RML spec.
     *
     * @return True if the store had to be updated, else false.
     * @throws Exception if something goes wrong during detection or conversion.
     */
    public boolean conform() throws Exception {
        this.detect();
        return false;
    }

    /**
     * Detect if mapping file is valid W3C RML.
     *
     * @return Dialect of the mapping file. Null if invalid.
     * @throws Exception if invalid or unconvertable
     */
    private void detect() throws Exception {
        // convert rml
        RMLConverterNew converter = new RMLConverterNew(store);
        converter.convert(mappingOptions);

        // Check if we have a valid TriplesMap.
        List<Term> triplesMaps = Utils.getSubjectsFromQuads(store.getQuads(null, new NamedNode(RML2 + "logicalSource"), null));
        if (triplesMaps.isEmpty()) {
            throw new Exception("Mapping requires at least one TriplesMap");
        }

        // Triples Maps need a subject Map
        List<Term> triplesMaps2 = Utils.getSubjectsFromQuads(store.getQuads(null, null, new NamedNode(RML2 + "TriplesMap")));
        for (Term triplesMap : triplesMaps2) {
            if (!store.contains(triplesMap, new NamedNode(RML2 + "subjectMap"), null)) {
                throw new Exception("TriplesMap requires a subject map");
            }
        }
    }


    /**
     * Debugging helper function to check difference of models
     *
     * @param store QuadStore which subtracts
     * @return boolean this.store isSubset of  given store
     */
    boolean differenceInConformer(QuadStore store) {
        return this.store.isSubset(store);
    }

    /**
     * Debugging helper function to check difference of models
     *
     * @param store QuadStore which subtracts
     * @return boolean given store isSubset of store
     */
    boolean differenceInGivenStore(QuadStore store) {
        return store.isSubset(this.store);
    }

    /**
     * Get a valid QuadStore
     *
     * @return a valid QuadStore
     */
    public QuadStore getStore() {
        return store;
    }
}

package be.ugent.rml.records;

import be.ugent.idlab.knows.dataio.record.Record;
import be.ugent.rml.NAMESPACES;
import be.ugent.rml.Utils;
import be.ugent.rml.access.AccessFactory;
import be.ugent.rml.store.Quad;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.ugent.idlab.knows.dataio.access.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class creates records based on RML rules.
 */
public class RecordsFactory {
    private Map<Access, Map<String, Map<String, List<Record>>>> recordsCache;
    private AccessFactory accessFactory;
    private Map<String, ReferenceFormulationRecordFactory> referenceFormulationRecordFactoryMap;
    private static final Logger logger = LoggerFactory.getLogger(RecordsFactory.class);

    public RecordsFactory(String basePath, String mappingPath) {
        accessFactory = new AccessFactory(basePath, mappingPath);
        recordsCache = new HashMap<>();

        referenceFormulationRecordFactoryMap = new HashMap<>();
        referenceFormulationRecordFactoryMap.put(ReferenceFormulation.XPath, new XMLRecordFactory());
        referenceFormulationRecordFactoryMap.put(ReferenceFormulation.SPARQLResultsXML, new XMLRecordFactory());
        referenceFormulationRecordFactoryMap.put(ReferenceFormulation.JSONPath, new JSONRecordFactory());
        referenceFormulationRecordFactoryMap.put(ReferenceFormulation.SPARQLResultsJSON, new JSONRecordFactory());
        referenceFormulationRecordFactoryMap.put(ReferenceFormulation.CSV, new TabularSourceFactory());
        referenceFormulationRecordFactoryMap.put(ReferenceFormulation.RDBTable, new TabularSourceFactory());
        referenceFormulationRecordFactoryMap.put(ReferenceFormulation.RDBQuery, new TabularSourceFactory());
        referenceFormulationRecordFactoryMap.put(ReferenceFormulation.SPARQLResultsCSV, new TabularSourceFactory());
        referenceFormulationRecordFactoryMap.put(ReferenceFormulation.CSS3, new HTMLRecordFactory());
    }

    /**
     * This method creates and returns records for a given Triples Map and set of RML rules.
     * @param triplesMap the Triples Map for which the record need to be created.
     * @param rmlStore the QuadStore with the RML rules.
     * @return a list of records.
     * @throws IOException
     */
    public List<Record> createRecords(Term triplesMap, QuadStore rmlStore) throws Exception {
        // Get Logical Sources.
        List<Term> logicalSources = Utils.getObjectsFromQuads(rmlStore.getQuads(triplesMap, new NamedNode(NAMESPACES.RML2 + "logicalSource"), null));

        // Check if there is at least one Logical Source.
        if (!logicalSources.isEmpty()) {
            Term logicalSource = logicalSources.get(0);

            Access access = accessFactory.getAccess(logicalSource, rmlStore);

            // Get Logical Source information
            List<Term> referenceFormulations = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML2 + "referenceFormulation"), null));

            // If no rml:referenceFormulation is given, but a table is given --> CSV
            if (referenceFormulations.isEmpty()) {
                referenceFormulations = List.of(new NamedNode(ReferenceFormulation.RDBTable));
            }

            String referenceFormulation = referenceFormulations.get(0).getValue();

            return getRecords(access, logicalSource, referenceFormulation, rmlStore);
        } else {
            throw new Error("No Logical Source is found for " + triplesMap + ". Exactly one Logical Source is required per Triples Map.");
        }
    }

    /**
     * This method returns records if they can be found in the cache of the factory.
     * @param access the access from which records need to come.
     * @param referenceFormulation the used reference formulation.
     * @param hash the hash used for the cache. Currently, this hash is based on the Logical Source (see hashLogicalSource()).
     * @return
     */
    private List<Record> getRecordsFromCache(Access access, String referenceFormulation, String hash) {
        if (recordsCache.containsKey(access)
                && recordsCache.get(access).containsKey(referenceFormulation)
                && recordsCache.get(access).get(referenceFormulation).containsKey(hash)
        ) {
            return recordsCache.get(access).get(referenceFormulation).get(hash);
        } else {
            return null;
        }
    }

    /**
     * This method puts a list of records in the cache.
     * @param access the access from which the records where fetched.
     * @param referenceFormulation the used reference formulation.
     * @param hash the used hash for the cache. Currently, this hash is based on the Logical Source (see hashLogicalSource()).
     * @param records the records that needs to be put into the cache.
     */
    private void putRecordsIntoCache(Access access, String referenceFormulation, String hash, List<Record> records) {
        if (!recordsCache.containsKey(access)) {
            recordsCache.put(access, new HashMap<>());
        }

        if (!recordsCache.get(access).containsKey(referenceFormulation)) {
            recordsCache.get(access).put(referenceFormulation, new HashMap<>());
        }

        recordsCache.get(access).get(referenceFormulation).put(hash, records);

    }

    /**
     * This method returns the records either from the cache or by fetching them for the data sources.
     * @param access the access from which the records needs to be fetched.
     * @param logicalSource the used Logical Source.
     * @param referenceFormulation the used reference formulation.
     * @param rmlStore the QuadStore with the RML rules.
     * @return a list of records.
     * @throws IOException
     */
    private List<Record> getRecords(Access access, Term logicalSource, String referenceFormulation, QuadStore rmlStore) throws Exception {
        String logicalSourceHash = hashLogicalSource(logicalSource, rmlStore);

        // Try to get the records from the cache.
        List<Record> records = getRecordsFromCache(access, referenceFormulation, logicalSourceHash);

        // If there are no records in the cache.
        // fetch from the data source.
        if (records == null) {
            try {
                // Select the Record Factory based on the reference formulation.
                if (!referenceFormulationRecordFactoryMap.containsKey(referenceFormulation)) {
                    logger.error("Referenceformulation {} is unsupported!", referenceFormulation);
                }
                ReferenceFormulationRecordFactory factory = referenceFormulationRecordFactoryMap.get(referenceFormulation);
                records = factory.getRecords(access, logicalSource, rmlStore);

                // Store the records in the cache for later.
                putRecordsIntoCache(access, referenceFormulation, logicalSourceHash, records);

                return records;
            } catch (Exception e) {
                throw e;
            }
        }

        return records;
    }

    /**
     * This method returns a hash for a Logical Source.
     * @param logicalSource the Logical Source for which a hash is wanted.
     * @param rmlStore the QuadStore of the RML rules.
     * @return a hash for the Logical Source.
     */
    private String hashLogicalSource(Term logicalSource, QuadStore rmlStore) {
        List<Quad> quads = rmlStore.getQuads(logicalSource, null, null);
        final String[] hash = {""};

        quads.forEach(quad -> {
            if (!quad.getPredicate().getValue().equals(NAMESPACES.RML2 + "source")
                    && !quad.getPredicate().getValue().equals(NAMESPACES.RML2 + "referenceFormulation") ) {
                hash[0] += quad.getObject().getValue();
            }
        });

        return hash[0];
    }
}

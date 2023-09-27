package be.ugent.rml.records;

import be.ugent.idlab.knows.dataio.source.Source;
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
public class SourcesFactory {

    private Map<Access, Map<String, Map<String, List<Source>>>> sourcesCache;
    private AccessFactory accessFactory;
    private Map<String, ReferenceFormulationRecordFactory> referenceFormulationRecordFactoryMap;
    private static final Logger logger = LoggerFactory.getLogger(SourcesFactory.class);

    public SourcesFactory(String basePath) {
        accessFactory = new AccessFactory(basePath);
        sourcesCache = new HashMap<>();

        referenceFormulationRecordFactoryMap = new HashMap<>();
        referenceFormulationRecordFactoryMap.put(ReferenceFormulation.XPath, new XMLRecordFactory());
        referenceFormulationRecordFactoryMap.put(ReferenceFormulation.JSONPath, new JSONRecordFactory2());
        referenceFormulationRecordFactoryMap.put(ReferenceFormulation.CSV, new CSVRecordFactory());
        referenceFormulationRecordFactoryMap.put(ReferenceFormulation.RDB, new CSVRecordFactory());
        referenceFormulationRecordFactoryMap.put(ReferenceFormulation.CSS3, new HTMLRecordFactory());
    }

    /**
     * This method creates and returns records for a given Triples Map and set of RML rules.
     * @param triplesMap the Triples Map for which the record need to be created.
     * @param rmlStore the QuadStore with the RML rules.
     * @return a list of records.
     * @throws IOException
     */
    public List<Source> createSources(Term triplesMap, QuadStore rmlStore) throws Exception {
        // Get Logical Sources.
        List<Term> logicalSources = Utils.getObjectsFromQuads(rmlStore.getQuads(triplesMap, new NamedNode(NAMESPACES.RML + "logicalSource"), null));

        // Check if there is at least one Logical Source.
        if (!logicalSources.isEmpty()) {
            Term logicalSource = logicalSources.get(0);

            Access access = accessFactory.getAccess(logicalSource, rmlStore);

            // Get Logical Source information
            List<Term> referenceFormulations = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML + "referenceFormulation"), null));
            List<Term> tables = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RR + "tableName"), null));

            // If no rml:referenceFormulation is given, but a table is given --> CSV
            if (referenceFormulations.isEmpty() && !tables.isEmpty()) {
                referenceFormulations = new ArrayList<>();
                referenceFormulations.add(0, new NamedNode(ReferenceFormulation.RDB));
            }

            if (referenceFormulations.isEmpty()) {
                throw new Error("The Logical Source of " + triplesMap + " does not have a reference formulation.");
            } else {
                String referenceFormulation = referenceFormulations.get(0).getValue();

                return getSources(access, logicalSource, referenceFormulation, rmlStore);
            }
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
    private List<Source> getSourcesFromCache(Access access, String referenceFormulation, String hash) {
        if (sourcesCache.containsKey(access)
                && sourcesCache.get(access).containsKey(referenceFormulation)
                && sourcesCache.get(access).get(referenceFormulation).containsKey(hash)
        ) {
            return sourcesCache.get(access).get(referenceFormulation).get(hash);
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
    private void putSourcesIntoCache(Access access, String referenceFormulation, String hash, List<Source> records) {
        if (!sourcesCache.containsKey(access)) {
            sourcesCache.put(access, new HashMap<>());
        }

        if (!sourcesCache.get(access).containsKey(referenceFormulation)) {
            sourcesCache.get(access).put(referenceFormulation, new HashMap<>());
        }

        sourcesCache.get(access).get(referenceFormulation).put(hash, records);

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
    private List<Source> getSources(Access access, Term logicalSource, String referenceFormulation, QuadStore rmlStore) throws Exception {
        String logicalSourceHash = hashLogicalSource(logicalSource, rmlStore);

        // Try to get the sources from the cache.
        List<Source> sources = getSourcesFromCache(access, referenceFormulation, logicalSourceHash);

        // If there are no sources in the cache.
        // fetch from the data source.
        if (sources == null) {
            try {
                // Select the Record Factory based on the reference formulation.
                if (!referenceFormulationRecordFactoryMap.containsKey(referenceFormulation)) {
                    logger.error("Referenceformulation {} is unsupported!", referenceFormulation);
                }
                ReferenceFormulationRecordFactory factory = referenceFormulationRecordFactoryMap.get(referenceFormulation);
                sources = factory.getRecords(access, logicalSource, rmlStore);

                // Store the sources in the cache for later.
                putSourcesIntoCache(access, referenceFormulation, logicalSourceHash, sources);

                return sources;
            } catch (Exception e) {
                throw e;
            }
        }

        return sources;
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
            if (!quad.getPredicate().getValue().equals(NAMESPACES.RML + "source")
                    && !quad.getPredicate().getValue().equals(NAMESPACES.RML + "referenceFormulation") ) {
                hash[0] += quad.getObject().getValue();
            }
        });

        return hash[0];
    }
}

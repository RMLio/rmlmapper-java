package be.ugent.rml.records;

import be.ugent.rml.DataFetcher;
import be.ugent.rml.NAMESPACES;
import be.ugent.rml.Utils;
import be.ugent.rml.access.Access;
import be.ugent.rml.access.AccessFactory;
import be.ugent.rml.store.Quad;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordsFactory {

    private Map<Access, Map<String, Map<String, List<Record>>>> recordCache;
    private AccessFactory accessFactory;
    private Map<String, ReferenceFormulationRecordFactory> referenceFormulationRecordFactoryMap;

    public RecordsFactory(DataFetcher dataFetcher) {
        accessFactory = new AccessFactory(dataFetcher.getCwd());
        recordCache = new HashMap<>();

        referenceFormulationRecordFactoryMap = new HashMap<>();
        referenceFormulationRecordFactoryMap.put(NAMESPACES.QL + "XPath", new XMLRecordFactory());
        referenceFormulationRecordFactoryMap.put(NAMESPACES.QL + "JSONPath", new JSONRecordFactory());
        referenceFormulationRecordFactoryMap.put(NAMESPACES.QL + "CSV", new CSVRecordFactory());
    }

    public List<Record> createRecords(Term triplesMap, QuadStore rmlStore) throws IOException {
        //get logical source
        List<Term> logicalSources = Utils.getObjectsFromQuads(rmlStore.getQuads(triplesMap, new NamedNode(NAMESPACES.RML + "logicalSource"), null));

        if (!logicalSources.isEmpty()) {
            Term logicalSource = logicalSources.get(0);

            Access access = accessFactory.getAccess(logicalSource, rmlStore);

            // Get logicalSource information
            List<Term> referenceFormulations = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML + "referenceFormulation"), null));
            List<Term> tables = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RR + "tableName"), null));

            // If no rml:referenceFormulation is given, but a table is given --> CSV
            if (referenceFormulations.isEmpty() && !tables.isEmpty()) {
                referenceFormulations = new ArrayList<>();
                referenceFormulations.add(0, new NamedNode(NAMESPACES.QL + "CSV"));
            }

            if (referenceFormulations.isEmpty()) {
                throw new Error("The Logical Source of " + triplesMap + " does not have a reference formulation.");
            } else {
                String referenceFormulation = referenceFormulations.get(0).getValue();

                return getRecords(access, logicalSource, referenceFormulation, rmlStore);
            }
        } else {
            throw new Error("No Logical Source is found for " + triplesMap + ". Exactly one Logical Source is required per Triples Map.");
        }
    }

    private List<Record> getRecordsFromCache(Access access, String referenceFormulation, String hash) {
        if (recordCache.containsKey(access)
                && recordCache.get(access).containsKey(referenceFormulation)
                && recordCache.get(access).get(referenceFormulation).containsKey(hash)
        ) {
            return recordCache.get(access).get(referenceFormulation).get(hash);
        } else {
            return null;
        }
    }

    private void putRecordsIntoCache(Access access, String referenceFormulation, String hash, List<Record> records) {
        if (!recordCache.containsKey(access)) {
            recordCache.put(access, new HashMap<>());
        }

        if (!recordCache.get(access).containsKey(referenceFormulation)) {
            recordCache.get(access).put(referenceFormulation, new HashMap<>());
        }

        recordCache.get(access).get(referenceFormulation).put(hash, records);

    }

    private List<Record> getRecords(Access access, Term logicalSource, String referenceFormulation, QuadStore rmlStore) throws IOException {
        String logicalSourceHash = hashLogicalSource(logicalSource, rmlStore);
        List<Record> records = getRecordsFromCache(access, referenceFormulation, logicalSourceHash);

        if (records == null) {
            try {
                ReferenceFormulationRecordFactory factory = referenceFormulationRecordFactoryMap.get(referenceFormulation);
                records = factory.getRecords(access, logicalSource, rmlStore);

                putRecordsIntoCache(access, referenceFormulation, logicalSourceHash, records);

                return records;
            } catch (IOException e) {
                throw e;
            }
        }

        return records;
    }

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

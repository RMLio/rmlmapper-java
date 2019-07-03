package be.ugent.rml.records;

import be.ugent.rml.DatabaseType;
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
import java.util.*;

public class RecordsFactory {

    private Map<String, Map<Integer, List<Record>>> allRDBsRecords;
    private Map<String, Map<Integer, List<Record>>> allSPARQLRecords;
    private Map<Access, Map<String, Map<String, List<Record>>>> recordCache;
    private XMLRecordFactory xmlRecordFactory;
    private JSONRecordFactory jsonRecordFactory;
    private AccessFactory accessFactory;
    private Map<String, ReferenceFormulationRecordFactory> referenceFormulationRecordFactoryMap;

    public RecordsFactory(DataFetcher dataFetcher) {
        allRDBsRecords = new HashMap<>();
        allSPARQLRecords = new HashMap<>();
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

    private List<Record> getRDBsRecords(QuadStore rmlStore, Term source, Term logicalSource, Term triplesMap,
                                        List<Term> table, List<Term> referenceFormulations) {
        // Retrieve database information from source object

        // - Driver URL
        List<Term> driverObject = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.D2RQ + "jdbcDriver"), null));

        if (driverObject.isEmpty()) {
            throw new Error("The database source object " + source + " does not include a driver.");
        }

        DatabaseType.Database database = DatabaseType.getDBtype(driverObject.get(0).getValue());

        // - DSN
        List<Term> dsnObject = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.D2RQ + "jdbcDSN"), null));

        if(dsnObject.isEmpty()) {
            throw new Error("The database source object " + source + " does not include a Data Source Name.");
        }

        String dsn = dsnObject.get(0).getValue();
        dsn = dsn.substring(dsn.indexOf("//") + 2);

        // - SQL query
        String query;
        List<Term> queryObject = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML + "query"), null));

        if (queryObject.isEmpty()) {
            if (table.isEmpty()) {
                throw new Error("The Logical Source of " + triplesMap + " does not include a SQL query nor a target table.");
            } else {
                query = "SELECT * FROM " + table.get(0).getValue();
            }
        } else {
            query = queryObject.get(0).getValue();
        }

        // - Username
        List<Term> usernameObject = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.D2RQ + "username"), null));

        if (usernameObject.isEmpty()) {
            throw new Error("The database source object " + source + " does not include a username.");
        }

        String username = usernameObject.get(0).getValue();

        // - Password
        List<Term> passwordObject = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.D2RQ + "password"), null));

        if (usernameObject.isEmpty()) {
            throw new Error("The database source object " + source + " does not include a password.");
        }

        String password = passwordObject.get(0).getValue();


        // Get records
        int queryHash = Utils.selectedColumnHash(query);
        // Check if already loaded in records map
        if (allRDBsRecords.containsKey(source) && allRDBsRecords.get(source).containsKey(queryHash)) {
            return allRDBsRecords.get(source).get(queryHash);
        } else {
            RDBs rdbs = new RDBs();
            return rdbs.get(dsn, database, username, password, query, referenceFormulations.get(0).getValue());
        }
    }

    private List<Record> getSPARQLRecords(QuadStore rmlStore, Term source, Term logicalSource, Term triplesMap,
                                       Term endpoint, List<Term> iterators, List<Term> referenceFormulations) {
        if (!iterators.isEmpty()) {

            // Get query
            List<Term> query = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML + "query"), null));
            if (query.isEmpty()) {
                throw new Error("No SPARQL query detected");
            }
            String qs = query.get(0).getValue().replaceAll("[\r\n]+", " ").trim();

            // Get result format
            List<Term> resultFormatObject = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.SD + "resultFormat"), null));
            SPARQL.ResultFormat resultFormat = getSPARQLResultFormat(resultFormatObject, referenceFormulations);

            // Get iterator
            String iterator = iterators.get(0).getValue();

            // Create key to save in records map
            // TODO: choose/discuss a better key
            int key = Utils.getHash(qs);

            if (allSPARQLRecords.containsKey(source.toString()) && allSPARQLRecords.get(source.toString()).containsKey(key)) {
                return allSPARQLRecords.get(source.toString()).get(key);
            } else {
                SPARQL sparql = new SPARQL(xmlRecordFactory, jsonRecordFactory);
                List<Record> records = sparql.get(endpoint.getValue(), qs, iterator, resultFormat, referenceFormulations.get(0).getValue());

                if (allSPARQLRecords.containsKey(source.toString())) {
                    allSPARQLRecords.get(source.toString()).put(key, records);
                } else {
                    Map<Integer, List<Record>> temp = new HashMap<>();
                    temp.put(key, records);
                    allSPARQLRecords.put(source.toString(), temp);
                }
                return records;
            }
        } else {
            throw new Error("The Logical Source of " + triplesMap + " does not have iterator, while this is expected for SPARQL.");
        }
    }

    private SPARQL.ResultFormat getSPARQLResultFormat(List<Term> resultFormat, List<Term> referenceFormulation) {
        if (resultFormat.isEmpty() && referenceFormulation.isEmpty()) {     // This will never be called atm but may come in handy later
            throw new Error("Please specify the sd:resultFormat of the SPARQL endpoint or a rml:referenceFormulation.");
        } else if (referenceFormulation.isEmpty()) {
            for (SPARQL.ResultFormat format: SPARQL.ResultFormat.values()) {
                if (resultFormat.get(0).getValue().equals(format.getUri())) {
                    return format;
                }
            }
            // No matching SPARQL.ResultFormat found
            throw new Error("Unsupported sd:resultFormat: " + resultFormat.get(0));

        } else if (resultFormat.isEmpty()) {
            for (SPARQL.ResultFormat format: SPARQL.ResultFormat.values()) {
                if (format.getReferenceFormulations().contains(referenceFormulation.get(0).getValue())) {
                    return format;
                }
            }
            // No matching SPARQL.ResultFormat found
            throw new Error("Unsupported rml:referenceFormulation for a SPARQL source.");

        } else {
            for (SPARQL.ResultFormat format : SPARQL.ResultFormat.values()) {
                if (resultFormat.get(0).getValue().equals(format.getUri())
                        && format.getReferenceFormulations().contains(referenceFormulation.get(0).getValue())) {
                    return format;
                }
            }
            throw new Error("Format specified in sd:resultFormat doesn't match the format specified in rml:referenceFormulation.");
        }
    }
}

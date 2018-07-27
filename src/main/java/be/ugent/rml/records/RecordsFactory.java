package be.ugent.rml.records;

import be.ugent.rml.DatabaseType;
import be.ugent.rml.DataFetcher;
import be.ugent.rml.NAMESPACES;
import be.ugent.rml.Utils;
import be.ugent.rml.store.QuadStore;
import org.apache.commons.lang.NotImplementedException;

import java.io.IOException;
import java.util.*;

public class RecordsFactory {

    private DataFetcher dataFetcher;
    private Map<String, List<Record>> allCSVRecords;
    private Map<String, Map<String, List<Record>>> allJSONRecords;
    private Map<String, Map<String, List<Record>>> allXMLRecords;
    private Map<String, Map<Integer, List<Record>>> allRDBsRecords;
    private Map<String, Map<Integer, List<Record>>> allSPARQLRecords;

    public RecordsFactory(DataFetcher dataFetcher) {
        this.dataFetcher = dataFetcher;
        allCSVRecords = new HashMap<>();
        allJSONRecords = new HashMap<>();
        allXMLRecords = new HashMap<>();
        allRDBsRecords = new HashMap<>();
        allSPARQLRecords = new HashMap<>();
    }

    public List<Record> createRecords(String triplesMap, QuadStore rmlStore) throws IOException {
        //get logical source
        List<String> logicalSources = Utils.getObjectsFromQuads(rmlStore.getQuads(triplesMap, NAMESPACES.RML + "logicalSource", null));

        if (!logicalSources.isEmpty()) {
            String logicalSource = logicalSources.get(0);

            // Get logicalSource information
            List<String> referenceFormulations = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, NAMESPACES.RML + "referenceFormulation", null));
            List<String> sources = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, NAMESPACES.RML + "source", null));
            List<String> iterators = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, NAMESPACES.RML + "iterator", null));
            List<String> table = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, NAMESPACES.RR + "tableName", null));

            SPARQL.ResultFormat sparqlResultFormat = null;
            if (sources.isEmpty()) {
                throw new Error("The Logical Source of " + triplesMap + " does not have a source.");
            } else if (referenceFormulations.isEmpty()) {
                if (!table.isEmpty()) {  // If no rml:referenceFormulation is given, but a table is given --> CSV
                    referenceFormulations = new ArrayList<>();
                    referenceFormulations.add(0, NAMESPACES.QL + "CSV");
                } else {
                    throw new Error("The Logical Source of " + triplesMap + " does not have a reference formulation.");
                }
            }

            String source;
            if (Utils.isLiteral(sources.get(0))) {
                source = Utils.getLiteral(sources.get(0));

                switch(referenceFormulations.get(0)) {
                    case NAMESPACES.QL + "CSV":
                        return getCSVRecords(source);
                    case NAMESPACES.QL + "XPath":
                        return getXMLRecords(source, iterators, triplesMap);
                    case NAMESPACES.QL + "JSONPath":
                        return getJSONRecords(source, iterators, triplesMap);
                    default:
                        throw new NotImplementedException();

                }

            } else {
                source = sources.get(0);

                List<String> sourceType = Utils.getObjectsFromQuads(rmlStore.getQuads(source, NAMESPACES.RDF + "type", null));
                switch(sourceType.get(0)) {
                    case NAMESPACES.D2RQ + "Database":  // RDBs
                        // Check if SQL version is given
                        List<String> sqlVersion = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, NAMESPACES.RR + "sqlVersion", null));
                        if (sqlVersion.isEmpty()) {
                            throw new Error("No SQL version identifier detected.");
                        }
                        return getRDBsRecords(rmlStore, source, logicalSource, triplesMap, table);
                    case NAMESPACES.SD + "Service":  // SPARQL
                        // Check if SPARQL Endpoint is given
                        List<String> endpoint = Utils.getObjectsFromQuads(rmlStore.getQuads(source, NAMESPACES.SD + "endpoint", null));
                        if (endpoint.isEmpty()) {
                            throw new Error("No SPARQL endpoint detected.");
                        }
                        return sparqlRecords(rmlStore, source, logicalSource, triplesMap, endpoint.get(0), iterators, referenceFormulations, sparqlResultFormat);
                    default:
                        throw new NotImplementedException();

                }
            }

        } else {
            throw new Error("No Logical Source is found for " + triplesMap + ". Exact one Logical Source is required per Triples Map.");
        }
    }

    private List<Record> getCSVRecords(String source) throws IOException {
        if (allCSVRecords.containsKey(source)){
            return allCSVRecords.get(source);
        } else {
            try {
                CSV csv = new CSV();
                allCSVRecords.put(source, csv.get(source, dataFetcher.getCwd()));
            } catch (IOException e) {
                throw e;
            }

            return allCSVRecords.get(source);
        }
    }

    private List<Record> getXMLRecords(String source, List<String> iterators, String triplesMap) throws IOException {
        if (!iterators.isEmpty()) {
            String iterator = Utils.getLiteral(iterators.get(0));

            if (allXMLRecords.containsKey(source) && allXMLRecords.get(source).containsKey(iterator)) {
                return allXMLRecords.get(source).get(iterator);
            } else {
                try {
                    XML xml = new XML();
                    List<Record> records = xml.get(source, iterator, dataFetcher.getCwd());

                    if (allXMLRecords.containsKey(source)) {
                        allXMLRecords.get(source).put(iterator, records);
                    } else {
                        Map<String, List<Record>> temp = new HashMap<>();
                        temp.put(iterator, records);
                        allXMLRecords.put(source, temp);
                    }

                    return records;
                } catch (IOException e) {
                    throw e;
                }
            }
        } else {
            throw new Error("The Logical Source of " + triplesMap + "does not have iterator, while this is expected for XPath.");
        }
    }

    private List<Record> getJSONRecords(String source, List<String> iterators, String triplesMap) throws IOException {
        if (!iterators.isEmpty()) {
            String iterator = Utils.getLiteral(iterators.get(0));

            if (allJSONRecords.containsKey(source) && allJSONRecords.get(source).containsKey(iterator)) {
                return allJSONRecords.get(source).get(iterator);
            } else {
                JSON json = new JSON();
                List<Record> records = json.get(source, iterator, dataFetcher.getCwd());

                if (allJSONRecords.containsKey(source)) {
                    allJSONRecords.get(source).put(iterator, records);
                } else {
                    Map<String, List<Record>> temp = new HashMap<>();
                    temp.put(iterator, records);
                    allJSONRecords.put(source, temp);
                }

                return records;
            }
        } else {
            throw new Error("The Logical Source of " + triplesMap + "does not have iterator, while this is expected for JSONPath.");
        }
    }

    private List<Record> getRDBsRecords(QuadStore rmlStore, String source, String logicalSource, String triplesMap, List<String> table) {
        // Retrieve database information from source object

        // - Driver URL
        List<String> driverObject = Utils.getObjectsFromQuads(rmlStore.getQuads(source, NAMESPACES.D2RQ + "jdbcDriver", null));
        if (driverObject.isEmpty()) {
            throw new Error("The database source object " + source + " does not include a driver.");
        }
        DatabaseType.Database database = DatabaseType.getDBtype(driverObject.get(0));

        // - DSN
        List<String> dsnObject = Utils.getObjectsFromQuads(rmlStore.getQuads(source, NAMESPACES.D2RQ + "jdbcDSN", null));
        if(dsnObject.isEmpty()) {
            throw new Error("The database source object " + source + " does not include a Data Source Name.");
        }
        String dsn = Utils.getLiteral(dsnObject.get(0));
        dsn = dsn.substring(dsn.indexOf("//") + 2);

        // - SQL query
        String query;
        List<String> queryObject = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, NAMESPACES.RR + "query", null));
        if (queryObject.isEmpty()) {
            if (table.isEmpty()) {
                throw new Error("The Logical Source of " + triplesMap + " does not include a SQL query nor a target table.");
            } else {
                query = "SELECT * FROM " + Utils.getLiteral(table.get(0));
            }
        } else {
            query = Utils.getLiteral(queryObject.get(0));
        }

        // - Username
        List<String> usernameObject = Utils.getObjectsFromQuads(rmlStore.getQuads(source, NAMESPACES.D2RQ + "username", null));
        if (usernameObject.isEmpty()) {
            throw new Error("The database source object " + source + " does not include a username.");
        }
        String username = Utils.getLiteral(usernameObject.get(0));

        // - Password
        List<String> passwordObject = Utils.getObjectsFromQuads(rmlStore.getQuads(source, NAMESPACES.D2RQ + "password", null));
        if (usernameObject.isEmpty()) {
            throw new Error("The database source object " + source + " does not include a password.");
        }
        String password = Utils.getLiteral(passwordObject.get(0));


        // Get records
        int queryHash = Utils.selectedColumnHash(query);
        // Check if already loaded in records map
        if (allRDBsRecords.containsKey(source) && allRDBsRecords.get(source).containsKey(queryHash)) {
            return allRDBsRecords.get(source).get(queryHash);
        } else {
            RDBs rdbs = new RDBs();
            return rdbs.get(dsn, database, username, password, query);
        }
    }

    private List<Record> sparqlRecords(QuadStore rmlStore, String source, String logicalSource, String triplesMap,
                                       String endpoint, List<String> iterators, List<String> referenceFormulations, SPARQL.ResultFormat resultFormat) {
        if (!iterators.isEmpty()) {

            // Get query
            List<String> query = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, NAMESPACES.RML + "query", null));
            if (query.isEmpty()) {
                throw new Error("No SPARQL query detected");
            }
            String qs = query.get(0).replaceAll("\n", " ").trim();
            qs = Utils.getLiteral(qs);

            // Get result format if not received
            if (resultFormat == null) {
                List<String> resultFormatObject = Utils.getObjectsFromQuads(rmlStore.getQuads(source, NAMESPACES.SD + "resultFormat", null));
                resultFormat = getSPARQLResultFormat(resultFormatObject, referenceFormulations);
            }

            // Get iterator
            String iterator = Utils.getLiteral(iterators.get(0));

            // Create key to save in records map
            // TODO: choose/discuss a better key
            int key = Utils.getHash(qs);

            if (allSPARQLRecords.containsKey(source) && allSPARQLRecords.get(source).containsKey(key)) {
                return allSPARQLRecords.get(source).get(key);
            } else {
                SPARQL sparql = new SPARQL();
                List<Record> records = sparql.get(endpoint, qs, iterator, resultFormat, referenceFormulations.get(0));

                if (allSPARQLRecords.containsKey(source)) {
                    allSPARQLRecords.get(source).put(key, records);
                } else {
                    Map<Integer, List<Record>> temp = new HashMap<>();
                    temp.put(key, records);
                    allSPARQLRecords.put(source, temp);
                }
                return records;
            }
        } else {
            throw new Error("The Logical Source of " + triplesMap + " does not have iterator, while this is expected for SPARQL.");
        }
    }

    private SPARQL.ResultFormat getSPARQLResultFormat(List<String> resultFormat, List<String> referenceFormulation) {
        if (resultFormat.isEmpty() && referenceFormulation.isEmpty()) {     // This will never be called atm but may come in handy later
            throw new Error("Please specify the sd:resultFormat of the SPARQL endpoint or a rml:referenceFormulation.");
        } else if (referenceFormulation.isEmpty()) {
            for (SPARQL.ResultFormat format: SPARQL.ResultFormat.values()) {
                if (resultFormat.get(0).equals(format.getUri())) {
                    return format;
                }
            }
            // No matching SPARQL.ResultFormat found
            throw new Error("Unsupported sd:resultFormat: " + resultFormat.get(0));

        } else if (resultFormat.isEmpty()) {
            for (SPARQL.ResultFormat format: SPARQL.ResultFormat.values()) {
                if (format.getReferenceFormulations().contains(referenceFormulation.get(0))) {
                    return format;
                }
            }
            // No matching SPARQL.ResultFormat found
            throw new Error("Unsupported rml:referenceFormulation for a SPARQL source.");

        } else {
            for (SPARQL.ResultFormat format : SPARQL.ResultFormat.values()) {
                if (resultFormat.get(0).equals(format.getUri())
                        && format.getReferenceFormulations().contains(referenceFormulation.get(0))) {
                    return format;
                }
            }
            throw new Error("Format specified in sd:resultFormat doesn't match the format specified in rml:referenceFormulation.");
        }
    }
}

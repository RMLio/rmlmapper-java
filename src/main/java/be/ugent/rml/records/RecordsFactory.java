package be.ugent.rml.records;

import be.ugent.rml.DatabaseType;
import be.ugent.rml.DataFetcher;
import be.ugent.rml.NAMESPACES;
import be.ugent.rml.Utils;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.Literal;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
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

    public List<Record> createRecords(Term triplesMap, QuadStore rmlStore) throws IOException {
        //get logical source
        List<Term> logicalSources = Utils.getObjectsFromQuads(rmlStore.getQuads(triplesMap, new NamedNode(NAMESPACES.RML + "logicalSource"), null));

        if (!logicalSources.isEmpty()) {
            Term logicalSource = logicalSources.get(0);

            // Get logicalSource information
            List<Term> referenceFormulations = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML + "referenceFormulation"), null));
            List<Term> sources = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML + "source"), null));
            List<Term> iterators = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML + "iterator"), null));
            List<Term> tables = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RR + "tableName"), null));

            // If no rml:referenceFormulation is given, but a table is given --> CSV
            if (referenceFormulations.isEmpty() && !tables.isEmpty()) {
                referenceFormulations = new ArrayList<>();
                referenceFormulations.add(0, new NamedNode(NAMESPACES.QL + "CSV"));
            }

            if (referenceFormulations.isEmpty()) {
                throw new Error("The Logical Source of " + triplesMap + " does not have a reference formulation.");
            } else if (sources.isEmpty()) {
                throw new Error("The Logical Source of " + triplesMap + " does not have a source.");
            } else {
                if (sources.get(0) instanceof Literal) {
                    String source = sources.get(0).getValue();
                    switch (referenceFormulations.get(0).getValue()) {
                        case NAMESPACES.QL + "CSV":
                            return getCSVRecords(source, iterators, triplesMap);
                        case NAMESPACES.QL + "XPath":
                            return getXMLRecords(source, iterators, triplesMap);
                        case NAMESPACES.QL + "JSONPath":
                            return getJSONRecords(source, iterators, triplesMap);
                        default:
                            throw new NotImplementedException();
                    }

                } else {
                    Term source = sources.get(0);

                    List<Term> sourceType = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.RDF + "type"), null));

                    switch(sourceType.get(0).getValue()) {
                        case NAMESPACES.D2RQ + "Database":  // RDBs
                            // Check if SQL version is given
                            List<Term> sqlVersion = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RR + "sqlVersion"), null));

                            if (sqlVersion.isEmpty()) {
                                throw new Error("No SQL version identifier detected.");
                            }

                            return getRDBsRecords(rmlStore, source, logicalSource, triplesMap, tables, referenceFormulations);
                        case NAMESPACES.SD + "Service":  // SPARQL
                            // Check if SPARQL Endpoint is given
                            List<Term> endpoint = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.SD + "endpoint"),
                                    null));
                            if (endpoint.isEmpty()) {
                                throw new Error("No SPARQL endpoint detected.");
                            }
                            return getSPARQLRecords(rmlStore, source, logicalSource, triplesMap,
                                    endpoint.get(0), iterators, referenceFormulations);
                        case NAMESPACES.CSVW + "Table": // CSVW
                            return getCSVRecords(rmlStore, source, iterators, triplesMap);
                        default:
                            throw new NotImplementedException();

                    }
                }
            }
        } else {
            throw new Error("No Logical Source is found for " + triplesMap + ". Exactly one Logical Source is required per Triples Map.");
        }
    }

    // TODO refactor getCSVRecords functions
    // CSV for filepath
    private List<Record> getCSVRecords(String source, List<Term> iterators, Term triplesMap) throws IOException {
        if (!iterators.isEmpty()) {
            throw new NotImplementedException();
        } else {
            return getCSVRecords(source);
        }
    }

    private List<Record> getCSVRecords(String source) throws IOException {
        if (allCSVRecords.containsKey(source)){
            return allCSVRecords.get(source);
        } else {
            try {
                CSVW CSVW = new CSVW(source, dataFetcher.getCwd());
                allCSVRecords.put(source, CSVW.getRecords());
            } catch (IOException e) {
                throw e;
            }
            return allCSVRecords.get(source);
        }
    }

    // CSVW
    private List<Record> getCSVRecords(QuadStore rmlStore, Term source, List<Term> iterators, Term triplesMap) throws IOException {
        List<Term> url = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.CSVW + "url"), null));
        if (!iterators.isEmpty()) {
            // TODO implement CSV iterator
            throw new NotImplementedException();
//            String iterator = iterators.get(0).getValue();
//
//            if (allCSVRecords.containsKey(source) && allCSVRecords.get(source).containsKey(iterator)) {
//                return allCSVRecords.get(source).get(iterator);
//            } else {
//                try {
//                    XML xml = new XML();
//                    List<Record> records = xml.get(source, iterator, dataFetcher.getCwd());
//
//                    if (allCSVRecords.containsKey(source)) {
//                        allCSVRecords.get(source).put(iterator, records);
//                    } else {
//                        Map<String, List<Record>> temp = new HashMap<>();
//                        temp.put(iterator, records);
//                        allCSVRecords.put(source, temp);
//                    }
//                    return records;
//                } catch (IOException e) {
//                    throw e;
//                }
//            }
        } else {
            String path = url.get(0).getValue();


            if (allCSVRecords.containsKey(path)){
                return allCSVRecords.get(path);
            } else {
                try {
                    CSVW CSVW = new CSVW(path, dataFetcher.getCwd());
                    CSVW.setOptions(rmlStore, source, iterators, triplesMap);
                    List<Record> records = CSVW.getRecords();
                    allCSVRecords.put(path, records);
                } catch (IOException e) {
                    throw e;
                }
                return allCSVRecords.get(path);
            }
        }
    }

    private List<Record> getXMLRecords(String source, List<Term> iterators, Term triplesMap) throws IOException {
        if (!iterators.isEmpty()) {
            String iterator = iterators.get(0).getValue();

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
            throw new Error("The Logical Source of " + triplesMap + " does not have iterator, while this is expected for XPath.");
        }
    }

    private List<Record> getJSONRecords(String source, List<Term> iterators, Term triplesMap) throws IOException {
        if (!iterators.isEmpty()) {
            String iterator = iterators.get(0).getValue();

            if (allJSONRecords.containsKey(source) && allJSONRecords.get(source).containsKey(iterator)) {
                return allJSONRecords.get(source).get(iterator);
            } else {
                try {
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
                } catch (IOException e) {
                    throw e;
                }
            }
        } else {
            throw new Error("The Logical Source of " + triplesMap + "does not have iterator, while this is expected for JSONPath.");
        }
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
                SPARQL sparql = new SPARQL();
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

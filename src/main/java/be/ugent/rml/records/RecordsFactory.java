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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordsFactory {

    private DataFetcher dataFetcher;
    private Map<String, List<Record>> allCSVRecords;
    private Map<String, Map<String, List<Record>>> allJSONRecords;
    private Map<String, Map<String, List<Record>>> allXMLRecords;
    private Map<String, Map<Integer, List<Record>>> allRDBsRecords;

    public RecordsFactory(DataFetcher dataFetcher) {
        this.dataFetcher = dataFetcher;
        allCSVRecords = new HashMap<>();
        allJSONRecords = new HashMap<>();
        allXMLRecords = new HashMap<>();
        allRDBsRecords = new HashMap<>();
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

                    switch(referenceFormulations.get(0).getValue()) {
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
                        default:
                            throw new NotImplementedException();

                    }
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
            throw new Error("The Logical Source of " + triplesMap + "does not have iterator, while this is expected for XPath.");
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
        List<Term> queryObject = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RR + "query"), null));

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
}

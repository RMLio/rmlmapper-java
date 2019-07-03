package be.ugent.rml.access;

import be.ugent.rml.DatabaseType;
import be.ugent.rml.NAMESPACES;
import be.ugent.rml.Utils;
import be.ugent.rml.records.SPARQLResultFormat;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.Literal;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import org.apache.commons.lang.NotImplementedException;

import java.util.List;

import static be.ugent.rml.Utils.isRemoteFile;

public class AccessFactory {

    private String basePath;

    public AccessFactory(String basePath) {
        this.basePath = basePath;
    }

    public Access getAccess(Term logicalSource, QuadStore rmlStore) {
        List<Term> sources = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML + "source"), null));
        Access access = null;

        if (!sources.isEmpty()) {
            Term source = sources.get(0);

            if (sources.get(0) instanceof Literal) {
                String value = sources.get(0).getValue();

                if (isRemoteFile(value)) {
                    access = new RemoteFileAccess(value);
                } else {
                    access = new LocalFileAccess(value, this.basePath);
                }
            } else {
                List<Term> sourceType = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.RDF + "type"), null));

                switch(sourceType.get(0).getValue()) {
                    case NAMESPACES.D2RQ + "Database":  // RDBs
                        // Check if SQL version is given
                        List<Term> sqlVersion = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RR + "sqlVersion"), null));

                        if (sqlVersion.isEmpty()) {
                            throw new Error("No SQL version identifier detected.");
                        }

                        access = getRDBAccess(rmlStore, source, logicalSource);

                        break;
                    case NAMESPACES.SD + "Service":  // SPARQL
                        // Check if SPARQL Endpoint is given
                        List<Term> endpoint = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.SD + "endpoint"),
                                null));

                        if (endpoint.isEmpty()) {
                            throw new Error("No SPARQL endpoint detected.");
                        }

                        // Get query
                        List<Term> query = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML + "query"), null));
                        if (query.isEmpty()) {
                            throw new Error("No SPARQL query detected");
                        }

                        List<Term> referenceFormulations = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML + "referenceFormulation"), null));

                        // Get result format
                        List<Term> resultFormatObject = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.SD + "resultFormat"), null));
                        SPARQLResultFormat resultFormat = getSPARQLResultFormat(resultFormatObject, referenceFormulations);

                        String queryString = query.get(0).getValue().replaceAll("[\r\n]+", " ").trim();

                        access = new SPARQLEndpointAccess(resultFormat.getMediaType(), endpoint.get(0).getValue(), queryString);;

                        break;
                    case NAMESPACES.CSVW + "Table": // CSVW
                        List<Term> urls = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.CSVW + "url"), null));

                        //TODO check whether there are actually urls or not
                        String value = urls.get(0).getValue();

                        if (isRemoteFile(value)) {
                            access = new RemoteFileAccess(value);
                        } else {
                            access = new LocalFileAccess(value, this.basePath);
                        }

                        break;
                    default:
                        throw new NotImplementedException();
                }
            }

            return access;
        } else {
            throw new Error("The Logical Source does not have a source.");
        }
    }

    private RDBAccess getRDBAccess(QuadStore rmlStore, Term source, Term logicalSource) {

        List<Term> table = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RR + "tableName"), null));

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
                // TODO better message (include Triples Map somewhere)

                throw new Error("The Logical Source does not include a SQL query nor a target table.");
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

        return new RDBAccess(dsn, database, username, password, query, "text/csv");
    }

    private SPARQLResultFormat getSPARQLResultFormat(List<Term> resultFormat, List<Term> referenceFormulation) {
        if (resultFormat.isEmpty() && referenceFormulation.isEmpty()) {     // This will never be called atm but may come in handy later
            throw new Error("Please specify the sd:resultFormat of the SPARQL endpoint or a rml:referenceFormulation.");
        } else if (referenceFormulation.isEmpty()) {
            for (SPARQLResultFormat format: SPARQLResultFormat.values()) {
                if (resultFormat.get(0).getValue().equals(format.getUri())) {
                    return format;
                }
            }
            // No matching SPARQLResultFormat found
            throw new Error("Unsupported sd:resultFormat: " + resultFormat.get(0));

        } else if (resultFormat.isEmpty()) {
            for (SPARQLResultFormat format: SPARQLResultFormat.values()) {
                if (format.getReferenceFormulations().contains(referenceFormulation.get(0).getValue())) {
                    return format;
                }
            }
            // No matching SPARQLResultFormat found
            throw new Error("Unsupported rml:referenceFormulation for a SPARQL source.");

        } else {
            for (SPARQLResultFormat format : SPARQLResultFormat.values()) {
                if (resultFormat.get(0).getValue().equals(format.getUri())
                        && format.getReferenceFormulations().contains(referenceFormulation.get(0).getValue())) {
                    return format;
                }
            }
            throw new Error("Format specified in sd:resultFormat doesn't match the format specified in rml:referenceFormulation.");
        }
    }
}

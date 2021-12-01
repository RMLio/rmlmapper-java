package be.ugent.rml.access;

import be.ugent.rml.NAMESPACES;
import be.ugent.rml.Utils;
import be.ugent.rml.records.SPARQLResultFormat;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.Literal;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

import static be.ugent.rml.Utils.isRemoteFile;

/**
 * This class creates Access instances.
 */
public class AccessFactory {

    // The path used when local paths are not absolute.
    private String basePath;
    private static final Logger logger = LoggerFactory.getLogger(AccessFactory.class);

    /**
     * The constructor of the AccessFactory.
     * @param basePath the base path for the local file system.
     */
    public AccessFactory(String basePath) {
        this.basePath = basePath;
    }

    /**
     * This method returns an Access instance based on the RML rules in rmlStore.
     * @param logicalSource the Logical Source for which the Access needs to be created.
     * @param rmlStore a QuadStore with RML rules.
     * @return an Access instance based on the RML rules in rmlStore.
     */
    public Access getAccess(Term logicalSource, QuadStore rmlStore) {
        List<Term> sources = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML + "source"), null));
        Access access = null;

        // check if at least one source is available.
        if (!sources.isEmpty()) {
            Term source = sources.get(0);

            // if we are dealing with a literal,
            // then it's either a local or remote file.
            if (sources.get(0) instanceof Literal) {
                String value = sources.get(0).getValue();

                if (isRemoteFile(value)) {
                    access = new RemoteFileAccess(value);
                } else {
                    access = new LocalFileAccess(value, this.basePath);
                }
            } else {
                // if not a literal, then we are dealing with a more complex description.
                List<Term> sourceType = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.RDF + "type"), null));

                switch(sourceType.get(0).getValue()) {
                    case NAMESPACES.D2RQ + "Database":  // RDBs
                        access = getRDBAccess(rmlStore, source, logicalSource);

                        break;
                    case NAMESPACES.SD + "Service":  // SPARQL
                        // Check if SPARQL Endpoint is given
                        List<Term> endpoint = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.SD + "endpoint"),
                                null));

                        if (endpoint.isEmpty()) {
                            throw new Error("No SPARQL endpoint found.");
                        }

                        // Get query
                        List<Term> query = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML + "query"), null));
                        if (query.isEmpty()) {
                            throw new Error("No SPARQL query found");
                        }

                        List<Term> referenceFormulations = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML + "referenceFormulation"), null));

                        // Get result format
                        List<Term> resultFormatObject = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.SD + "resultFormat"), null));
                        SPARQLResultFormat resultFormat = getSPARQLResultFormat(resultFormatObject, referenceFormulations);

                        access = new SPARQLEndpointAccess(resultFormat.getContentType(), endpoint.get(0).getValue(), query.get(0).getValue());;

                        break;
                    case NAMESPACES.CSVW + "Table": // CSVW
                        List<Term> urls = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.CSVW + "url"), null));

                        if (urls.isEmpty()) {
                            throw new Error("No url found for the CSVW Table");
                        }

                        String value = urls.get(0).getValue();

                        if (isRemoteFile(value)) {
                            access = new RemoteFileAccess(value);
                        } else {
                            access = new LocalFileAccess(value, this.basePath);
                        }

                        break;
                    case NAMESPACES.TD + "PropertyAffordance":
                        HashMap<String, String> headers = new HashMap<String, String>();

                        List<Term> form = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.TD + "hasForm"), null));
                        List<Term> targets = Utils.getObjectsFromQuads(rmlStore.getQuads(form.get(0), new NamedNode(NAMESPACES.HCTL + "hasTarget"), null));
                        List<Term> contentTypes = Utils.getObjectsFromQuads(rmlStore.getQuads(form.get(0), new NamedNode(NAMESPACES.HCTL + "forContentType"), null));
                        List<Term> headerList = Utils.getObjectsFromQuads(rmlStore.getQuads(form.get(0), new NamedNode(NAMESPACES.HTV + "headers"), null));

                        // Security schema & data
                        try {
                            Term thing = Utils.getSubjectsFromQuads(rmlStore.getQuads(null, new NamedNode(NAMESPACES.TD + "hasPropertyAffordance"), source)).get(0);
                            List<Term> securityConfiguration = Utils.getObjectsFromQuads(rmlStore.getQuads(thing, new NamedNode(NAMESPACES.TD + "hasSecurityConfiguration"), null));
                            logger.debug("Security config: " + securityConfiguration.toString());
                            for (Term sc : securityConfiguration) {
                                // TODO: support more security configurations
                                List<Term> securityIn = Utils.getObjectsFromQuads(rmlStore.getQuads(sc, new NamedNode(NAMESPACES.WOTSEC + "in"), null));
                                List<Term> securityName = Utils.getObjectsFromQuads(rmlStore.getQuads(sc, new NamedNode(NAMESPACES.WOTSEC + "name"), null));
                                List<Term> securityValue = Utils.getObjectsFromQuads(rmlStore.getQuads(sc, new NamedNode(NAMESPACES.IDSA + "tokenValue"), null));
                                // BearerSecurityScheme
                                List<Term> securityScheme = Utils.getObjectsFromQuads(rmlStore.getQuads(sc, new NamedNode(NAMESPACES.RDF + "type"), new NamedNode(NAMESPACES.WOTSEC + "BearerSecurityScheme")));
                                if (securityScheme.size() != 0) {
                                    Term bearerToken = new Literal("Bearer " + securityValue.get(0).getValue());
                                    securityValue.set(0, bearerToken);
                                }

                                try {
                                    switch (securityIn.get(0).getValue()) {
                                        case "header": {
                                            logger.info("Applying security configuration of " + sc.getValue() + "in header");
                                            logger.debug("Name: " + securityName.get(0).getValue());
                                            logger.debug("Value: " + securityValue.get(0).getValue());
                                            headers.put(securityName.get(0).getValue(), securityValue.get(0).getValue());
                                            break;
                                        }
                                        case "query":
                                        case "body":
                                        case "cookie":
                                        default:
                                            throw new NotImplementedException();
                                    }
                                } catch (IndexOutOfBoundsException e) {
                                    logger.warn("Unable to apply security configuration for " + sc.getValue());
                                }
                            }
                        }
                        catch (IndexOutOfBoundsException e) {
                            logger.warn("No td:Thing description, unable to determine security configurations, assuming no security policies apply");
                        }

                        if (targets.isEmpty()) {
                            throw new Error("No target found for TD Thing");
                        }

                        // TODO: determine which protocol is used to know which vocabulary is needed for the protocol specific part.
                        String target = targets.get(0).getValue();
                        String contentType = contentTypes.isEmpty()? null: contentTypes.get(0).getValue();

                        // Retrieve HTTP headers
                        for (Term headerListItem: headerList) {
                            try {
                                List<Term> header = Utils.getList(rmlStore, headerListItem);
                                for(Term h: header) {
                                    String headerName = Utils.getObjectsFromQuads(rmlStore.getQuads(h, new NamedNode(NAMESPACES.HTV + "fieldName"), null)).get(0).getValue();
                                    String headerValue = Utils.getObjectsFromQuads(rmlStore.getQuads(h, new NamedNode(NAMESPACES.HTV + "fieldValue"), null)).get(0).getValue();
                                    logger.debug("Retrieved HTTP header: '" + headerName + "','" + headerValue + "'");
                                    headers.put(headerName, headerValue);
                                }
                            }
                            catch(IndexOutOfBoundsException e) {
                                logger.warn("Unable to retrieve header name and value for " + headerListItem.getValue());
                            }
                        };
                        access = new WoTAccess(target, contentType, headers);
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

    /**
     * This method returns an RDB Access instance for the RML rules in rmlStore.
     * @param rmlStore a QuadStore with RML rules.
     * @param source the object of rml:source, dependent on the Logical Source.
     * @param logicalSource the Logical Source for which the Access instance need to be created.
     * @return an RDB Access instance for the RML rules in rmlStore.
     */
    private RDBAccess getRDBAccess(QuadStore rmlStore, Term source, Term logicalSource) {

        // - Table
        List<Term> tables = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RR + "tableName"), null));

        // Retrieve database information from source object

        // - Driver URL
        List<Term> driverObject = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.D2RQ + "jdbcDriver"), null));

        if (driverObject.isEmpty()) {
            throw new Error("The database source object " + source + " does not include a driver.");
        }

        DatabaseType database = DatabaseType.getDBtype(driverObject.get(0).getValue());

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
            if (tables.isEmpty()) {
                // TODO better message (include Triples Map somewhere)

                throw new Error("The Logical Source does not include a SQL query nor a target table.");
            } else if (tables.get(0).getValue().equals("") || tables.get(0).getValue().equals("\"\"")) {
                throw new Error("The table name of a database should not be empty.");
            } else {
                query = "SELECT * FROM " + tables.get(0).getValue();
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
        String password = ""; // No password is the default.
        List<Term> passwordObject = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.D2RQ + "password"), null));

        if (!passwordObject.isEmpty()) {
            password = passwordObject.get(0).getValue();
        }

        // - ContentType
        List<Term> contentType = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML + "referenceFormulation"), null));

        return new RDBAccess(dsn, database, username, password, query, (contentType.isEmpty()? "text/csv" : contentType.get(0).getValue()));
    }

    /**
     * This method returns a SPARQLResultFormat based on the result formats and reference formulations.
     * @param resultFormats the result formats used to determine the SPARQLResultFormat.
     * @param referenceFormulations the reference formulations used to to determine the SPARQLResultFormat.
     * @return a SPARQLResultFormat.
     */
    private SPARQLResultFormat getSPARQLResultFormat(List<Term> resultFormats, List<Term> referenceFormulations) {
        if (resultFormats.isEmpty() && referenceFormulations.isEmpty()) {     // This will never be called atm but may come in handy later
            throw new Error("Please specify the sd:resultFormat of the SPARQL endpoint or a rml:referenceFormulation.");
        } else if (referenceFormulations.isEmpty()) {

            for (SPARQLResultFormat format: SPARQLResultFormat.values()) {
                if (resultFormats.get(0).getValue().equals(format.getUri())) {
                    return format;
                }
            }

            // No matching SPARQLResultFormat found
            throw new Error("Unsupported sd:resultFormat: " + resultFormats.get(0));
        } else if (resultFormats.isEmpty()) {

            for (SPARQLResultFormat format: SPARQLResultFormat.values()) {
                if (format.getReferenceFormulations().contains(referenceFormulations.get(0).getValue())) {
                    return format;
                }
            }

            // No matching SPARQLResultFormat found
            throw new Error("Unsupported rml:referenceFormulation for a SPARQL source.");
        } else {
            for (SPARQLResultFormat format : SPARQLResultFormat.values()) {

                if (resultFormats.get(0).getValue().equals(format.getUri())
                        && format.getReferenceFormulations().contains(referenceFormulations.get(0).getValue())) {
                    return format;
                }
            }

            throw new Error("Format specified in sd:resultFormat doesn't match the format specified in rml:referenceFormulation.");
        }
    }
}

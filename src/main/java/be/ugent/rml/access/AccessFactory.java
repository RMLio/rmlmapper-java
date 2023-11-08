package be.ugent.rml.access;

import be.ugent.idlab.knows.dataio.access.*;
import be.ugent.rml.NAMESPACES;
import be.ugent.rml.Utils;
import be.ugent.rml.records.SPARQLResultFormat;
import be.ugent.rml.store.QuadStore;
import org.apache.commons.lang3.NotImplementedException;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static be.ugent.rml.Utils.isRemoteFile;

/**
 * This class creates Access instances.
 */
public class AccessFactory {

    private static final ValueFactory valueFactory = SimpleValueFactory.getInstance();
    // The path used when local paths are not absolute.
    private final String basePath;
    final Logger logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

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
    public Access getAccess(Value logicalSource, QuadStore rmlStore) {
        List<Value> sources = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, valueFactory.createIRI(NAMESPACES.RML + "source"), null));
        Access access;

        // check if at least one source is available.
        if (!sources.isEmpty()) {
            Value source = sources.get(0);

            // if we are dealing with a literal,
            // then it's either a local or remote file.
            if (sources.get(0).isLiteral()) {
                String value = sources.get(0).stringValue();
                if (isRemoteFile(value)) {
                    access = new RemoteFileAccess(value);
                } else {
                    String datatype = ((Literal) sources.get(0)).getDatatype()  == null ? null :((Literal) sources.get(0)).getDatatype().stringValue();
                    access = new LocalFileAccess(value, this.basePath, datatype);

                }
            } else {
                // if not a literal, then we are dealing with a more complex description.
                List<Value> sourceType = Utils.getObjectsFromQuads(rmlStore.getQuads(source, valueFactory.createIRI(NAMESPACES.RDF + "type"), null));

                switch(sourceType.get(0).stringValue()) {
                    case NAMESPACES.D2RQ + "Database":  // RDBs
                        access = getRDBAccess(rmlStore, source, logicalSource);

                        break;
                    case NAMESPACES.SD + "Service":  // SPARQL
                        // Check if SPARQL Endpoint is given
                        List<Value> endpoint = Utils.getObjectsFromQuads(rmlStore.getQuads(source, valueFactory.createIRI(NAMESPACES.SD + "endpoint"),
                                null));

                        if (endpoint.isEmpty()) {
                            throw new Error("No SPARQL endpoint found.");
                        }

                        // Get query
                        List<Value> query = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, valueFactory.createIRI(NAMESPACES.RML + "query"), null));
                        if (query.isEmpty()) {
                            throw new Error("No SPARQL query found");
                        }

                        List<Value> referenceFormulations = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, valueFactory.createIRI(NAMESPACES.RML + "referenceFormulation"), null));

                        // Get result format
                        List<Value> resultFormatObject = Utils.getObjectsFromQuads(rmlStore.getQuads(source, valueFactory.createIRI(NAMESPACES.SD + "resultFormat"), null));
                        SPARQLResultFormat resultFormat = getSPARQLResultFormat(resultFormatObject, referenceFormulations);

                        access = new SPARQLEndpointAccess(resultFormat.getContentType(), endpoint.get(0).stringValue(), query.get(0).stringValue());

                        break;
                    case NAMESPACES.CSVW + "Table": // CSVW
                        List<Value> urls = Utils.getObjectsFromQuads(rmlStore.getQuads(source, valueFactory.createIRI(NAMESPACES.CSVW + "url"), null));

                        if (urls.isEmpty()) {
                            throw new Error("No url found for the CSVW Table");
                        }

                        String value = urls.get(0).stringValue();

                        if (isRemoteFile(value)) {
                            access = new RemoteFileAccess(value);
                        } else {
                            access = new LocalFileAccess(value, this.basePath, "CSVW");
                        }

                        break;
                    case NAMESPACES.TD + "PropertyAffordance":
                        Map<String, String> headers = new HashMap<>();
                        Map<String, Map<String, String>> auth = new HashMap<>();
                        auth.put("data", new HashMap<>());
                        auth.put("info", new HashMap<>());

                        List<Value> form = Utils.getObjectsFromQuads(rmlStore.getQuads(source, valueFactory.createIRI(NAMESPACES.TD + "hasForm"), null));
                        List<Value> targets = Utils.getObjectsFromQuads(rmlStore.getQuads(form.get(0), valueFactory.createIRI(NAMESPACES.HCTL + "hasTarget"), null));
                        List<Value> contentTypes = Utils.getObjectsFromQuads(rmlStore.getQuads(form.get(0), valueFactory.createIRI(NAMESPACES.HCTL + "forContentType"), null));
                        List<Value> headerList = Utils.getObjectsFromQuads(rmlStore.getQuads(form.get(0), valueFactory.createIRI(NAMESPACES.HTV + "headers"), null));
                        // Security schema & data
                        try {
                            Value thing = Utils.getSubjectsFromQuads(rmlStore.getQuads(null, valueFactory.createIRI(NAMESPACES.TD + "hasPropertyAffordance"), source)).get(0);
                            List<Value> securityConfiguration = Utils.getObjectsFromQuads(rmlStore.getQuads(thing, valueFactory.createIRI(NAMESPACES.TD + "hasSecurityConfiguration"), null));
                            logger.debug("Security config: {}", Arrays.toString(securityConfiguration.toArray()));

                            for (Value sc : securityConfiguration) {
                                boolean isOAuth = !Utils.getObjectsFromQuads(rmlStore.getQuads(sc, valueFactory.createIRI(NAMESPACES.RDF + "type"),
                                        valueFactory.createIRI(NAMESPACES.WOTSEC + "OAuth2SecurityScheme"))).isEmpty();
                                boolean isBearer = !Utils.getObjectsFromQuads(rmlStore.getQuads(sc, valueFactory.createIRI(NAMESPACES.RDF + "type"),
                                        valueFactory.createIRI(NAMESPACES.WOTSEC + "BearerSecurityScheme"))).isEmpty();
                                List<Value> securityIn = Utils.getObjectsFromQuads(rmlStore.getQuads(sc, valueFactory.createIRI(NAMESPACES.WOTSEC + "in"), null));
                                List<Value> securityName = Utils.getObjectsFromQuads(rmlStore.getQuads(sc, valueFactory.createIRI(NAMESPACES.WOTSEC + "name"), null));
                                List<Value> securityValue = Utils.getObjectsFromQuads(rmlStore.getQuads(sc, valueFactory.createIRI(NAMESPACES.IDSA + "tokenValue"), null));
                                if (isOAuth || isBearer) {
                                    // BearerSecurityScheme
                                    // OAuth2 specific
                                    if (isOAuth) {
                                        logger.debug("OAuth2 is used");
                                        Value securityAuth = Utils.getObjectsFromQuads(rmlStore.getQuads(sc, valueFactory.createIRI(NAMESPACES.WOTSEC + "authorization"), null)).get(0);
                                        auth.get("info").put("authorization", securityAuth.stringValue());
                                        auth.get("info").put("name", securityName.get(0).stringValue());

                                        Value securityRefresh = Utils.getObjectsFromQuads(rmlStore.getQuads(sc, valueFactory.createIRI(NAMESPACES.IDSA + "refreshValue"), null)).get(0);
                                        Value securityClientID = Utils.getObjectsFromQuads(rmlStore.getQuads(sc, valueFactory.createIRI(NAMESPACES.IDSA + "clientID"), null)).get(0);
                                        Value securityClientSecret = Utils.getObjectsFromQuads(rmlStore.getQuads(sc, valueFactory.createIRI(NAMESPACES.IDSA + "clientSecret"), null)).get(0);
//                                        Value securityGrantType = Utils.getObjectsFromQuads(rmlStore.getQuads(sc, valueFactory.createIRI(NAMESPACES.WOTSEC + "grant_type"), null)).get(0);

                                        auth.get("data").put("refresh", securityRefresh.stringValue());
                                        auth.get("data").put("client_id", securityClientID.stringValue());
                                        auth.get("data").put("client_secret", securityClientSecret.stringValue());
                                        logger.debug("Refresh token: {}", securityRefresh.stringValue());
                                        logger.debug("Client ID: {}", securityClientID.stringValue());
                                        logger.debug("Client Secret: {}", securityClientSecret.stringValue());
//                                      //can this not be set default?
//                                        auth.get("data").put("grant_type", securityGrantType.stringValue());
                                    }
                                    // both oath and bearer
                                    Value bearerToken = valueFactory.createLiteral("Bearer " + securityValue.get(0).stringValue());
                                    securityValue.set(0, bearerToken);
                                }
                                try {
                                    if (securityIn.get(0).stringValue().equals("header")) {
                                        logger.info("Applying security configuration of {} in header", sc.stringValue());
                                        logger.debug("Name: {}", securityName.get(0).stringValue());
                                        logger.debug("Value: {}", securityValue.get(0).stringValue());
                                        headers.put(securityName.get(0).stringValue(), securityValue.get(0).stringValue());
                                    } else {
                                        throw new NotImplementedException();
                                    }
                                } catch (IndexOutOfBoundsException e) {
                                    logger.warn("Unable to apply security configuration for {}", sc.stringValue());
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
                        String target = targets.get(0).stringValue();
                        String contentType = contentTypes.isEmpty()? null: contentTypes.get(0).stringValue();

                        // Retrieve HTTP headers
                        for (Value headerListItem: headerList) {
                            try {
                                List<Value> header = Utils.getList(rmlStore, headerListItem);
                                for(Value h: header) {
                                    String headerName = Utils.getObjectsFromQuads(rmlStore.getQuads(h, valueFactory.createIRI(NAMESPACES.HTV + "fieldName"), null)).get(0).stringValue();
                                    String headerValue = Utils.getObjectsFromQuads(rmlStore.getQuads(h, valueFactory.createIRI(NAMESPACES.HTV + "fieldValue"), null)).get(0).stringValue();
                                    logger.debug("Retrieved HTTP header: '{}','{}'", headerName, headerValue);
                                    headers.put(headerName, headerValue);
                                }
                            }
                            catch(IndexOutOfBoundsException e) {
                                logger.warn("Unable to retrieve header name and value for {}", headerListItem.stringValue());
                            }
                        }
                        access = new WoTAccess(target, contentType, headers, auth);
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
    private RDBAccess getRDBAccess(QuadStore rmlStore, Value source, Value logicalSource) {

        // - Table
        List<Value> tables = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, valueFactory.createIRI(NAMESPACES.RR + "tableName"), null));

        // Retrieve database information from source object

        // - Driver URL
        List<Value> driverObject = Utils.getObjectsFromQuads(rmlStore.getQuads(source, valueFactory.createIRI(NAMESPACES.D2RQ + "jdbcDriver"), null));

        if (driverObject.isEmpty()) {
            throw new Error("The database source object " + source + " does not include a driver.");
        }

        DatabaseType database = DatabaseType.getDBtype(driverObject.get(0).stringValue());

        // - DSN
        List<Value> dsnObject = Utils.getObjectsFromQuads(rmlStore.getQuads(source, valueFactory.createIRI(NAMESPACES.D2RQ + "jdbcDSN"), null));

        if(dsnObject.isEmpty()) {
            throw new Error("The database source object " + source + " does not include a Data Source Name.");
        }

        String dsn = dsnObject.get(0).stringValue();

        // - SQL query
        String query;
        List<Value> queryObject = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, valueFactory.createIRI(NAMESPACES.RML + "query"), null));

        if (queryObject.isEmpty()) {
            if (tables.isEmpty()) {
                // TODO better message (include Triples Map somewhere)

                throw new Error("The Logical Source does not include a SQL query nor a target table.");
            } else if (tables.get(0).stringValue().isEmpty() || tables.get(0).stringValue().equals("\"\"")) {
                throw new Error("The table name of a database should not be empty.");
            } else {
                query = "SELECT * FROM " + tables.get(0).stringValue();
            }
        } else {
            query = queryObject.get(0).stringValue();
        }

        // - Username
        List<Value> usernameObject = Utils.getObjectsFromQuads(rmlStore.getQuads(source, valueFactory.createIRI(NAMESPACES.D2RQ + "username"), null));

        if (usernameObject.isEmpty()) {
            throw new Error("The database source object " + source + " does not include a username.");
        }

        String username = usernameObject.get(0).stringValue();

        // - Password
        String password = ""; // No password is the default.
        List<Value> passwordObject = Utils.getObjectsFromQuads(rmlStore.getQuads(source, valueFactory.createIRI(NAMESPACES.D2RQ + "password"), null));

        if (!passwordObject.isEmpty()) {
            password = passwordObject.get(0).stringValue();
        }

        // - ContentType
        List<Value> contentType = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, valueFactory.createIRI(NAMESPACES.RML + "referenceFormulation"), null));

        return new RDBAccess(dsn, database, username, password, query, (contentType.isEmpty() ? "text/csv" : contentType.get(0).stringValue()));
    }

    /**
     * This method returns a SPARQLResultFormat based on the result formats and reference formulations.
     * @param resultFormats the result formats used to determine the SPARQLResultFormat.
     * @param referenceFormulations the reference formulations used to determine the SPARQLResultFormat.
     * @return a SPARQLResultFormat.
     */
    private SPARQLResultFormat getSPARQLResultFormat(List<Value> resultFormats, List<Value> referenceFormulations) {
        if (resultFormats.isEmpty() && referenceFormulations.isEmpty()) {     // This will never be called atm but may come in handy later
            throw new Error("Please specify the sd:resultFormat of the SPARQL endpoint or a rml:referenceFormulation.");
        } else if (referenceFormulations.isEmpty()) {

            for (SPARQLResultFormat format: SPARQLResultFormat.values()) {
                if (resultFormats.get(0).stringValue().equals(format.getUri())) {
                    return format;
                }
            }

            // No matching SPARQLResultFormat found
            throw new Error("Unsupported sd:resultFormat: " + resultFormats.get(0));
        } else if (resultFormats.isEmpty()) {

            for (SPARQLResultFormat format: SPARQLResultFormat.values()) {
                if (format.getReferenceFormulations().contains(referenceFormulations.get(0).stringValue())) {
                    return format;
                }
            }

            // No matching SPARQLResultFormat found
            throw new Error("Unsupported rml:referenceFormulation for a SPARQL source.");
        } else {
            for (SPARQLResultFormat format : SPARQLResultFormat.values()) {

                if (resultFormats.get(0).stringValue().equals(format.getUri())
                        && format.getReferenceFormulations().contains(referenceFormulations.get(0).stringValue())) {
                    return format;
                }
            }

            throw new Error("Format specified in sd:resultFormat doesn't match the format specified in rml:referenceFormulation.");
        }
    }
}

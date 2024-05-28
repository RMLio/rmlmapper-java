package be.ugent.rml.access;

import be.ugent.idlab.knows.dataio.access.*;
import be.ugent.rml.NAMESPACES;
import be.ugent.rml.Utils;
import be.ugent.rml.records.ReferenceFormulation;
import be.ugent.rml.records.SPARQLResultFormat;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.Literal;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static be.ugent.rml.Utils.isRemoteFile;

/**
 * This class creates Access instances.
 */
public class AccessFactory {

    // The path used when local paths are not absolute.
    private final String basePath;
    private final String mappingPath;
    final Logger logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    private static final Map<String, String> REF_FORM_MIMETYPE = Map.of(
            NAMESPACES.RML2 + "CSV", "text/csv"
    );

    /**
     * The constructor of the AccessFactory.
     *
     * @param basePath the base path for the local file system.
     * @param mappingPath the path to the used mapping file.
     */
    public AccessFactory(String basePath, String mappingPath) {
        this.basePath = basePath;
        this.mappingPath = mappingPath;
    }

    /**
     * This method returns an Access instance based on the RML rules in rmlStore.
     * @param logicalSource the Logical Source for which the Access needs to be created.
     * @param rmlStore a QuadStore with RML rules.
     * @return an Access instance based on the RML rules in rmlStore.
     */
    public Access getAccess(Term logicalSource, QuadStore rmlStore) {
        List<Term> sources = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML2 + "source"), null));
        Access access;

        // check if at least one source is available.
        if (!sources.isEmpty()) {
            Term source = sources.get(0);

            // if we are dealing with a literal,
            // then it's either a local or remote file.
            if (sources.get(0) instanceof Literal literal) {
                String value = literal.getValue();
                if (isRemoteFile(value)) {
                    Term refForm = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML2 + "referenceFormulation"), null)).get(0);
                    String mimeType = REF_FORM_MIMETYPE.get(refForm.toString());
                    access = new RemoteFileAccess(value, mimeType);
                } else {
                    access = new LocalFileAccess(value, this.basePath);
                }
            } else {
                // if not a literal, then we are dealing with a more complex description.
                List<Term> sourceType = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.RDF + "type"), null));
                sourceType.remove(new NamedNode(NAMESPACES.RML2 + "Source"));

                switch(sourceType.get(0).getValue()) {
                    case NAMESPACES.RML2 + "RelativePathSource":
                        String path = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.RML2 + "path"), null)).get(0).getValue();
                        String root = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.RML2 + "root"), null)).get(0).getValue();
                        if (root.equals(NAMESPACES.RML2 + "MappingDirectory")) {
                            access = new LocalFileAccess(path, this.mappingPath);
                        } else {
                            access = new LocalFileAccess(path, this.basePath);
                        }
                        break;

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
                        List<Term> query = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML2 + "iterator"), null));
                        if (query.isEmpty()) {
                            throw new Error("No SPARQL query found");
                        }

                        List<Term> referenceFormulations = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML2 + "referenceFormulation"), null));

                        // Get result format
                        List<Term> resultFormatObject = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.SD + "resultFormat"), null));
                        SPARQLResultFormat resultFormat = getSPARQLResultFormat(resultFormatObject, referenceFormulations);

                        access = new SPARQLEndpointAccess(resultFormat.getContentType(), endpoint.get(0).getValue(), query.get(0).getValue());

                        break;
                    case NAMESPACES.CSVW + "Table": // CSVW
                        List<Term> urls = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.CSVW + "url"), null));

                        if (urls.isEmpty()) {
                            throw new Error("No url found for the CSVW Table");
                        }

                        String value = urls.get(0).getValue();

                        if (isRemoteFile(value)) {
                            access = new RemoteFileAccess(value, "text/csvw");
                        } else {
                            access = new LocalFileAccess(value, this.basePath, "text/csvw");
                        }

                        break;
                    case NAMESPACES.TD + "Thing":
                        Map<String, Map<String, String>> auth2 = new HashMap<>();
                        auth2.put("data", new HashMap<>());
                        auth2.put("info", new HashMap<>());

                        try {
                            Term propertyAffordance = rmlStore.getQuad(source, new NamedNode(NAMESPACES.TD + "hasPropertyAffordance"), null).getObject();
                            List<Term> form = Utils.getObjectsFromQuads(rmlStore.getQuads(propertyAffordance, new NamedNode(NAMESPACES.TD + "hasForm"), null));
                            List<Term> targets = Utils.getObjectsFromQuads(rmlStore.getQuads(form.get(0), new NamedNode(NAMESPACES.HCTL + "hasTarget"), null));
                            List<Term> contentTypes = Utils.getObjectsFromQuads(rmlStore.getQuads(form.get(0), new NamedNode(NAMESPACES.HCTL + "forContentType"), null));

                            // TODO: determine which protocol is used to know which vocabulary is needed for the protocol specific part.
                            String target = targets.get(0).getValue();
                            String contentType = contentTypes.isEmpty() ? null : contentTypes.get(0).getValue();

                            access = new WoTAccess(target, contentType, new HashMap<>(), auth2);
                        } catch (Exception e) {
                            logger.error("Cannot create WoT TD:Thing access");
                            access = null;
                        }
                        break;
                    case NAMESPACES.TD + "PropertyAffordance":
                        Map<String, String> headers = new HashMap<>();
                        Map<String, Map<String, String>> auth = new HashMap<>();
                        auth.put("data", new HashMap<>());
                        auth.put("info", new HashMap<>());

                        List<Term> form = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.TD + "hasForm"), null));
                        List<Term> targets = Utils.getObjectsFromQuads(rmlStore.getQuads(form.get(0), new NamedNode(NAMESPACES.HCTL + "hasTarget"), null));
                        List<Term> contentTypes = Utils.getObjectsFromQuads(rmlStore.getQuads(form.get(0), new NamedNode(NAMESPACES.HCTL + "forContentType"), null));
                        List<Term> headerList = Utils.getObjectsFromQuads(rmlStore.getQuads(form.get(0), new NamedNode(NAMESPACES.HTV + "headers"), null));
                        // Security schema & data
                        try {
                            Term thing = Utils.getSubjectsFromQuads(rmlStore.getQuads(null, new NamedNode(NAMESPACES.TD + "hasPropertyAffordance"), source)).get(0);
                            List<Term> securityConfiguration = Utils.getObjectsFromQuads(rmlStore.getQuads(thing, new NamedNode(NAMESPACES.TD + "hasSecurityConfiguration"), null));
                            logger.debug("Security config: {}", Arrays.toString(securityConfiguration.toArray()));

                            for (Term sc : securityConfiguration) {
                                boolean isOAuth = !Utils.getObjectsFromQuads(rmlStore.getQuads(sc, new NamedNode(NAMESPACES.RDF + "type"),
                                        new NamedNode(NAMESPACES.WOTSEC + "OAuth2SecurityScheme"))).isEmpty();
                                boolean isBearer = !Utils.getObjectsFromQuads(rmlStore.getQuads(sc, new NamedNode(NAMESPACES.RDF + "type"),
                                        new NamedNode(NAMESPACES.WOTSEC + "BearerSecurityScheme"))).isEmpty();
                                List<Term> securityIn = Utils.getObjectsFromQuads(rmlStore.getQuads(sc, new NamedNode(NAMESPACES.WOTSEC + "in"), null));
                                List<Term> securityName = Utils.getObjectsFromQuads(rmlStore.getQuads(sc, new NamedNode(NAMESPACES.WOTSEC + "name"), null));
                                List<Term> securityValue = Utils.getObjectsFromQuads(rmlStore.getQuads(sc, new NamedNode(NAMESPACES.IDSA + "tokenValue"), null));
                                if (isOAuth || isBearer) {
                                    // BearerSecurityScheme
                                    // OAuth2 specific
                                    if (isOAuth) {
                                        logger.debug("OAuth2 is used");
                                        Term securityAuth = Utils.getObjectsFromQuads(rmlStore.getQuads(sc, new NamedNode(NAMESPACES.WOTSEC + "authorization"), null)).get(0);
                                        auth.get("info").put("authorization", securityAuth.getValue());
                                        auth.get("info").put("name", securityName.get(0).getValue());

                                        Term securityRefresh = Utils.getObjectsFromQuads(rmlStore.getQuads(sc, new NamedNode(NAMESPACES.IDSA + "refreshValue"), null)).get(0);
                                        Term securityClientID = Utils.getObjectsFromQuads(rmlStore.getQuads(sc, new NamedNode(NAMESPACES.IDSA + "clientID"), null)).get(0);
                                        Term securityClientSecret = Utils.getObjectsFromQuads(rmlStore.getQuads(sc, new NamedNode(NAMESPACES.IDSA + "clientSecret"), null)).get(0);
//                                        Term securityGrantType = Utils.getObjectsFromQuads(rmlStore.getQuads(sc, new NamedNode(NAMESPACES.WOTSEC + "grant_type"), null)).get(0);

                                        auth.get("data").put("refresh", securityRefresh.getValue());
                                        auth.get("data").put("client_id", securityClientID.getValue());
                                        auth.get("data").put("client_secret", securityClientSecret.getValue());
                                        logger.debug("Refresh token: {}", securityRefresh.getValue());
                                        logger.debug("Client ID: {}", securityClientID.getValue());
                                        logger.debug("Client Secret: {}", securityClientSecret.getValue());
//                                      //can this not be set default?
//                                        auth.get("data").put("grant_type", securityGrantType.getValue());
                                    }
                                    // both oath and bearer
                                    Term bearerToken = new Literal("Bearer " + securityValue.get(0).getValue());
                                    securityValue.set(0, bearerToken);
                                }
                                try {
                                    if (securityIn.get(0).getValue().equals("header")) {
                                        logger.info("Applying security configuration of {} in header", sc.getValue());
                                        logger.debug("Name: {}", securityName.get(0).getValue());
                                        logger.debug("Value: {}", securityValue.get(0).getValue());
                                        headers.put(securityName.get(0).getValue(), securityValue.get(0).getValue());
                                    } else {
                                        throw new NotImplementedException();
                                    }
                                } catch (IndexOutOfBoundsException e) {
                                    logger.warn("Unable to apply security configuration for {}", sc.getValue());
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
                                    logger.debug("Retrieved HTTP header: '{}','{}'", headerName, headerValue);
                                    headers.put(headerName, headerValue);
                                }
                            }
                            catch(IndexOutOfBoundsException e) {
                                logger.warn("Unable to retrieve header name and value for {}", headerListItem.getValue());
                            }
                        }
                        access = new WoTAccess(target, contentType, headers, auth);
                        break;
                    case NAMESPACES.DCAT + "Distribution":
                        List<Term> dcatUrls = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.DCAT + "downloadURL"), null));

                        if (dcatUrls.isEmpty()) {
                            throw new Error("No url found for the DCAT Distribution");
                        }

                        String dcatValue = dcatUrls.get(0).getValue();
                        if (isRemoteFile(dcatValue)) {

                            List<Term> refFormulationTerms = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML2 + "referenceFormulation"), null));
                            String mimetype = REF_FORM_MIMETYPE.get(refFormulationTerms.get(0).getValue());
                            if (mimetype != null) {
                                access = new RemoteFileAccess(dcatValue, mimetype);
                            } else {
                                access = new RemoteFileAccess(dcatValue);
                            }
                        } else {
                            logger.debug("Local file found `{}`, trying in basePath '{}' and mapping path '{}'", dcatValue, this.basePath, this.mappingPath);
                            File f1 = new File(this.basePath, dcatValue);
                            File f2 = new File(this.mappingPath, dcatValue);
                            File f3 = new File(dcatValue);
                            if (f1.exists() || f3.exists()) {
                                access = new LocalFileAccess(dcatValue, this.basePath);
                            } else if (f2.exists()) {
                                access = new LocalFileAccess(dcatValue, this.mappingPath);
                            }
                            else {
                                throw new Error("Cannot find " + dcatValue);
                            }
                        }
                        break;
                    default:
                        throw new NotImplementedException(sourceType.get(0).getValue());
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

        String referenceFormulation = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML2 + "referenceFormulation"), null)).get(0).getValue();

        String query;
        String iterator = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML2 + "iterator"), null)).get(0).getValue();

        if (referenceFormulation.equals(ReferenceFormulation.RDBTable)) {
            // rml:iterator contains the table name
            query = String.format("SELECT * FROM %s", iterator);
        } else {
            // rml:iterator contains the query itself
            query = iterator;
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

        return new RDBAccess(dsn, database, username, password, query, (contentType.isEmpty() ? "text/csv" : contentType.get(0).getValue()));
    }

    /**
     * This method returns a SPARQLResultFormat based on the result formats and reference formulations.
     * @param resultFormats the result formats used to determine the SPARQLResultFormat.
     * @param referenceFormulations the reference formulations used to determine the SPARQLResultFormat.
     * @return a SPARQLResultFormat.
     */
    private SPARQLResultFormat getSPARQLResultFormat(List<Term> resultFormats, List<Term> referenceFormulations) {
        logger.debug("Getting SPARQL result format for result format '{}' and reference formulations '{}'", resultFormats.toString(), referenceFormulations.toString());
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
                logger.debug(format + "   " + resultFormats.get(0).getValue().equals(format.getUri()) + "   " + format.getReferenceFormulations().contains(referenceFormulations.get(0).getValue()));
                logger.debug(format.getReferenceFormulations().toString());
                if (resultFormats.get(0).getValue().equals(format.getUri())) {
                    return format;
                }
            }

            throw new Error("Format specified in sd:resultFormat doesn't match the format specified in rml:referenceFormulation.");
        }
    }
}

package be.ugent.rml.target;

import be.ugent.rml.NAMESPACES;
import be.ugent.rml.Utils;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.Literal;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

public class TargetFactory {

    // The path used when local paths are not absolute.
    private String basePath;
    private static final Logger logger = LoggerFactory.getLogger(TargetFactory.class);

    /**
     * The constructor of the TargetFactory.
     */
    public TargetFactory(String basePath) {
        this.basePath = basePath;
    }

    /**
     * This method returns a Target instance based on the RML rules in rmlStore.
     * @param logicalTarget the Logical Target for which the Target needs to be created.
     * @param rmlStore a QuadStore with RML rules.
     */
    public Target getTarget(Term logicalTarget, QuadStore rmlStore) throws NotImplementedException, IOException {
        Target target = null;
        String serializationFormat = "nquads";
        String compression = null;

        // Old Logical Source reference is supported for Logical Targets as well for backwards compatibility
        if (logicalTarget instanceof Literal) {
            logger.warn("Legacy string output path for Target found, do not use, this is only supported for backwards compatibility reasons.");
            String location = logicalTarget.getValue();
            if (location.endsWith(".nq")) {
                serializationFormat = "nquads";
            }
            else if (location.endsWith(".nt")) {
                serializationFormat = "ntriples";
            }
            else if (location.endsWith(".ttl")) {
                serializationFormat = "turtle";
            }
            else if (location.endsWith(".jsonld")) {
                serializationFormat = "jsonld";
            }
            else {
                throw new NotImplementedException("Serialization format for " + location + " not implemented!");
            }
            target = new LocalFileTarget(location, this.basePath, serializationFormat, null);
            return target;
        }

        List<Term> targets = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalTarget,
                new NamedNode(NAMESPACES.RMLT + "target"), null));

        // Read serialization format
        try {
            String sf = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalTarget, new NamedNode(NAMESPACES.RMLT + "serialization"), null)).get(0).getValue();
            switch (sf) {
                case NAMESPACES.FORMATS + "N-Triples":
                    serializationFormat = "ntriples";
                    break;
                case NAMESPACES.FORMATS + "N-Quads":
                    serializationFormat = "nquads";
                    break;
                case NAMESPACES.FORMATS + "JSON-LD":
                    serializationFormat = "jsonld";
                    break;
                case NAMESPACES.FORMATS + "Turtle":
                    serializationFormat = "turtle";
                    break;
                default:
                    throw new NotImplementedException("Serialization format " + sf + " not implemented!");
            }
        }
        catch (IndexOutOfBoundsException e) {
            logger.debug("No serialization format specified, falling back to default N-Quads");
        }

        // Read compression
        try {
            Term comp = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalTarget,
                    new NamedNode(NAMESPACES.RMLT + "compression"), null)).get(0);
            switch (comp.getValue()) {
                case NAMESPACES.COMP + "gzip":
                    compression = "gzip";
                    break;
                case NAMESPACES.COMP + "zip":
                    compression = "zip";
                    break;
                default:
                    throw new NotImplementedException("Compression " + comp + " is not implemented!");
            }
            logger.debug("Compression: " + compression);
        }
        catch (IndexOutOfBoundsException e) {
            logger.debug("Compression disabled");
        }

        // Build target
        if (!targets.isEmpty()) {
            Term t = targets.get(0);
            logger.debug("getTarget() for " + t.toString());

            // If not a literal, then we are dealing with a more complex description.
            String targetType = Utils.getObjectsFromQuads(rmlStore.getQuads(t,
                    new NamedNode(NAMESPACES.RDF + "type"), null)).get(0).getValue();
            logger.debug("Target is IRI, target type: " + targetType);

            switch(targetType) {
                case NAMESPACES.VOID + "Dataset": { // VoID Dataset
                    logger.debug("Target is a VoID Dataset");
                    String location = Utils.getObjectsFromQuads(rmlStore.getQuads(t,
                            new NamedNode(NAMESPACES.VOID + "dataDump"), null)).get(0).getValue();
                    location = location.replace("file://", ""); // Local file starts with file://
                    logger.debug("VoID datadump location: " + location);
                    target = new LocalFileTarget(location, this.basePath, serializationFormat, compression);
                    break;
                }
                case NAMESPACES.SD + "Service": { // SPARQL Service
                    logger.debug("Target is a SD Service");
                    String endpoint = Utils.getObjectsFromQuads(rmlStore.getQuads(t,
                            new NamedNode(NAMESPACES.SD + "endpoint"), null)).get(0).getValue();
                    String supportedLanguage = Utils.getObjectsFromQuads(rmlStore.getQuads(t,
                            new NamedNode(NAMESPACES.SD + "supportedLanguage"), null)).get(0).getValue();
                    logger.debug("SPARQL Service endpoint: " + endpoint);
                    logger.debug("SPARQL Service supported language: " + supportedLanguage);

                    // Check SPARQL UPDATE compatibility
                    if (!supportedLanguage.equals(NAMESPACES.SD + "SPARQL11Update")) {
                        throw new IllegalArgumentException("SPARQL Service target must support SPARQL Update!");
                    }

                    // Try to instantiate a SPARQL endpoint
                    target = new SPARQLEndpointTarget(endpoint);
                    break;
                }
                default: {
                    throw new NotImplementedException("Not implemented");
                }
            }
            logger.debug("Target created: " + target);
            return target;
        }
        else {
            throw new Error("The Target does not have target.");
        }
    }
}
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
        Charset encoding = Charset.defaultCharset();
        String serializationFormat = "ntriples";
        String compression = null;
        List<Term> targets = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalTarget,
                new NamedNode(NAMESPACES.RML + "target"), null));

        // Read serialization format
        try {
            String sf = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalTarget, new NamedNode(NAMESPACES.RML + "serialization"), null)).get(0).getValue();
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
            logger.debug("No serialization format specified, falling back to default N-triples");
        }

        // Read encoding
        try {
            String enc = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalTarget,
                    new NamedNode(NAMESPACES.RML + "encoding"), null)).get(0).getValue().toLowerCase(Locale.ROOT);
            switch (enc) {
                case "utf-8":
                    encoding = StandardCharsets.UTF_8;
                    break;
                case "utf-16":
                    encoding = StandardCharsets.UTF_16;
                    break;
                case "iso-8859-1":
                    encoding = StandardCharsets.ISO_8859_1;
                    break;
                default:
                    throw new NotImplementedException("Character encoding " + enc + " not implemented!");
            }
            logger.debug("Encoding: " + encoding);
        }
        catch (IndexOutOfBoundsException e) {
            logger.debug("No encoding specified, falling back to default UTF-8");
        }

        // Read encoding
        try {
            compression = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalTarget,
                    new NamedNode(NAMESPACES.RML + "compression"), null)).get(0).getValue();
            logger.debug("Compression: " + compression);
        }
        catch (IndexOutOfBoundsException e) {
            logger.debug("Compression disabled");
        }

        // Build target
        if (!targets.isEmpty()) {
            Term t = targets.get(0);
            logger.debug("getTarget() for " + t.toString());

            // Old Logical Source reference is supported for Logical Targets as well for backwards compatibility
            if (targets.get(0) instanceof Literal) {
                logger.debug("Logical Target is Literal");
                String location = targets.get(0).getValue();
                target = new LocalFileTarget(location, this.basePath, serializationFormat, encoding, compression);
            }
            else {
                // If not a literal, then we are dealing with a more complex description.
                String targetType = Utils.getObjectsFromQuads(rmlStore.getQuads(t,
                        new NamedNode(NAMESPACES.RDF + "type"), null)).get(0).getValue();
                logger.debug("Logical Target is IRI, target type: " + targetType);

                switch(targetType) {
                    case NAMESPACES.VOID + "Dataset": { // VoID Dataset
                        logger.debug("Logical Target is a VoID Dataset");
                        String location = Utils.getObjectsFromQuads(rmlStore.getQuads(t,
                                new NamedNode(NAMESPACES.VOID + "dataDump"), null)).get(0).getValue();
                        location = location.replace("file://", ""); // Local file starts with file://
                        logger.debug("VoID datadump location: " + location);
                        target = new LocalFileTarget(location, this.basePath, serializationFormat, encoding, compression);
                        break;
                    }
                    case NAMESPACES.SD + "Service": { // SPARQL Service
                        logger.debug("Logical Target is a SD Service");
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
            }
            logger.debug("Target created: " + target);
            return target;
        }
        else {
            throw new Error("The Logical Target does not have target.");
        }
    }
}
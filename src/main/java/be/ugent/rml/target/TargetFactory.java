package be.ugent.rml.target;

import be.ugent.rml.NAMESPACES;
import be.ugent.rml.Utils;
import be.ugent.rml.store.Quad;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.Literal;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.poi.ss.formula.functions.Na;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
     * @param outputStore a QuadStore with the RDF triples to write to the target.
     */
    public Target getTarget(Term logicalTarget, QuadStore rmlStore, QuadStore outputStore) throws NotImplementedException, IOException {
        Target target = null;
        String serializationFormat = "nquads";
        String compression = null;
        boolean isLDES = false;
        List<Quad> metadata = new ArrayList<>();

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
            target = new LocalFileTarget(location, this.basePath, serializationFormat, null, metadata);
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

        // Detect LDES EventStreamTarget
        List<Term> types = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalTarget,
                new NamedNode(NAMESPACES.RDF + "type"), null));
        for (Term type: types) {
            // Target has LDES features, read them
            if (type.getValue().equals(NAMESPACES.LDES + "EventStreamTarget")) {
                logger.debug("Found LDES EventStreamTarget");
                isLDES = true;
                Term iri;
                Term retention_iri;
                Term eventstream_iri;
                Term versionOfPathObj = null;
                Term timestampPathObj = null;
                Term retentionPolicyType;
                Term retentionPolicyAmount;

                // Required LDES IRI
                try {
                    iri = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalTarget,
                            new NamedNode(NAMESPACES.LDES + "baseIRI"), null)).get(0);
                    retention_iri = new NamedNode(iri.getValue() + "#retention");
                    eventstream_iri = new NamedNode(iri.getValue() + "#eventstream");
                    logger.debug("LDES base IRI: " + iri.getValue());
                }
                catch (IndexOutOfBoundsException e) {
                    throw new IllegalArgumentException("No base IRI specified for LDES!");
                }

                // Required SHACL shape
                try {
                    Term shape = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalTarget,
                            new NamedNode(NAMESPACES.TREE + "shape"), null)).get(0);
                    logger.debug("SHACL shape: " + shape.getValue());

                    // TODO: Handle embedded SHACL shapes in RML mapping rules as well.
                    metadata.add(new Quad(eventstream_iri, new NamedNode(NAMESPACES.TREE + "shape"), shape));
                }
                catch (IndexOutOfBoundsException e) {
                    logger.error("No SHACL shape specified for LDES!");
                }

                // LDES tree:view
                metadata.add(new Quad(eventstream_iri, new NamedNode(NAMESPACES.RDF + "type"),
                        new NamedNode(NAMESPACES.LDES + "EventStream")));
                List<Term> subjects = new ArrayList<Term>(new HashSet<Term>(Utils.getSubjectsFromQuads(outputStore.getQuads(null, null, null))));
                for (Term s: subjects) {
                    metadata.add(new Quad(eventstream_iri, new NamedNode(NAMESPACES.TREE + "member"), s));
                }
                metadata.add(new Quad(eventstream_iri, new NamedNode(NAMESPACES.TREE + "view"), iri));

                // Optional versionOf path
                try {
                    versionOfPathObj = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalTarget,
                            new NamedNode(NAMESPACES.LDES + "versionOfPath"), null)).get(0);
                }
                catch (IndexOutOfBoundsException e) {
                    logger.debug("No versionOfPath found");
                }

                // Optional timestamp path
                try {
                    timestampPathObj = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalTarget,
                            new NamedNode(NAMESPACES.LDES + "timestampPath"), null)).get(0);
                }
                catch (IndexOutOfBoundsException e) {
                    logger.debug("No timestampPath found");
                }

                // Optional retention policy
                try {
                    Term retentionPolicy = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalTarget,
                            new NamedNode(NAMESPACES.LDES + "retentionPolicy"), null)).get(0);
                    // TODO parse retention policies
                    throw new NotImplementedException("Parsing LDES retention policies not implemented yet!");
                }
                catch (IndexOutOfBoundsException e) {
                    logger.debug("No retention policy specified, default to latest only");
                    retentionPolicyType = new NamedNode(NAMESPACES.LDES + "LatestVersionSubject");
                    retentionPolicyAmount = new Literal("1");
                }

                metadata.add(new Quad(iri, new NamedNode(NAMESPACES.LDES + "retentionPolicy"), retention_iri));
                metadata.add(new Quad(retention_iri, new NamedNode(NAMESPACES.RDF + "type"), retentionPolicyType));
                metadata.add(new Quad(retention_iri, new NamedNode(NAMESPACES.LDES + "amount"), retentionPolicyAmount));
                if (versionOfPathObj != null) {
                    metadata.add(new Quad(retention_iri, new NamedNode(NAMESPACES.LDES + "versionOfPath"), versionOfPathObj));
                }
                if (timestampPathObj != null) {
                    metadata.add(new Quad(retention_iri, new NamedNode(NAMESPACES.LDES + "timestampPath"), timestampPathObj));
                }

                break;
            }
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
                    target = new LocalFileTarget(location, this.basePath, serializationFormat, compression, metadata);
                    break;
                }
                case NAMESPACES.DCAT + "Dataset": { // DCAT Dataset
                    logger.debug("Target is a DCAT Dataset");
                    String location = Utils.getObjectsFromQuads(rmlStore.getQuads(t,
                            new NamedNode(NAMESPACES.DCAT + "dataDump"), null)).get(0).getValue();
                    location = location.replace("file://", ""); // Local file starts with file://
                    logger.debug("DCAT datadump location: " + location);
                    target = new LocalFileTarget(location, this.basePath, serializationFormat, compression, metadata);
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
                    target = new SPARQLEndpointTarget(endpoint, metadata);
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
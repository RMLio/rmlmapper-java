package be.ugent.rml.target;

import be.ugent.rml.NAMESPACES;
import be.ugent.rml.Utils;
import be.ugent.rml.store.Quad;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.Literal;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TargetFactory {

    // The path used when local paths are not absolute.
    private final String basePath;
    private static final Logger logger = LoggerFactory.getLogger(TargetFactory.class);

    /**
     * The constructor of the TargetFactory.
     */
    public TargetFactory(String basePath) {
        this.basePath = basePath;
    }

    private void detectLDESEventStreamTarget(Term logicalTarget, List<Quad> metadata, QuadStore rmlStore, QuadStore outputStore) {
        List<Term> types = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalTarget,
                new NamedNode(NAMESPACES.RDF + "type"), null));
        for (Term type: types) {
            // Target has LDES features, read them
            if (type.getValue().equals(NAMESPACES.LDES + "EventStreamTarget")) {
                logger.error("'{}EventStreamTarget' is not supported anymore. Use '{}/EventStreamTarget'. Not generating LDES metadata!", NAMESPACES.LDES, NAMESPACES.RMLT);
                return;
            } else if (type.getValue().equals(NAMESPACES.RMLT + "EventStreamTarget")) {
                logger.debug("Found RMLT EventStreamTarget");
                Term iri;
                Term ldes_iri = null;
                Term ldes = null;
                Term versionOfPathObj = null;
                Term timestampPathObj = null;
                Term memberTargetClass = null;
                boolean ldesGenerateImmutableIRI = false;

                try {
                    // Check if LDES IRI is given
                    iri = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalTarget,
                            new NamedNode(NAMESPACES.RMLT + "ldesBaseIRI"), null)).get(0);
                    ldes_iri = new NamedNode(iri.getValue());
                    logger.debug("LDES base IRI: {}", iri.getValue());

                    // LDES RDF type EventStream
                    metadata.add(new Quad(ldes_iri, new NamedNode(NAMESPACES.RDF + "type"),
                        new NamedNode(NAMESPACES.LDES + "EventStream")));
                }
                catch (IndexOutOfBoundsException e) {
                    logger.debug("No LDES metadata will be generated since no LDES base IRI was specified");
                }

                try {
                    // LDES Member configuration properties
                    ldes = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalTarget,
                            new NamedNode(NAMESPACES.RMLT + "ldes"), null)).get(0);
                } catch (IndexOutOfBoundsException e) {
                    logger.debug("No LDES member metadata found.");
                }

                if (ldes != null) {
                    // Optional SHACL shape
                    if (ldes_iri != null) {
                        try {
                            Term shape = Utils.getObjectsFromQuads(rmlStore.getQuads(ldes,
                                new NamedNode(NAMESPACES.TREE + "shape"), null)).get(0);
                            logger.debug("SHACL shape: {}", shape.getValue());
                            // TODO: Handle embedded SHACL shapes in RML mapping rules as well.
                            metadata.add(new Quad(ldes_iri, new NamedNode(NAMESPACES.TREE + "shape"), shape));
                        } catch (IndexOutOfBoundsException e) {
                            logger.debug("No TREE SHACL shape specified for LDES.");
                        }
                    }
                    
                    // Optional versionOf path
                    try {
                        versionOfPathObj = Utils.getObjectsFromQuads(rmlStore.getQuads(ldes,
                                new NamedNode(NAMESPACES.LDES + "versionOfPath"), null)).get(0);
                        logger.debug("VersionOf path: {}", versionOfPathObj.getValue());
                        if (ldes_iri != null) {
                            metadata.add(new Quad(ldes_iri, new NamedNode(NAMESPACES.LDES + "versionOfPath"), versionOfPathObj));
                        }
                    } catch (IndexOutOfBoundsException e) {
                        logger.debug("No LDES versionOf path found");
                    }

                    // Optional timestamp path
                    try {
                        timestampPathObj = Utils.getObjectsFromQuads(rmlStore.getQuads(ldes,
                                new NamedNode(NAMESPACES.LDES + "timestampPath"), null)).get(0);
                        if (ldes_iri != null) {
                            metadata.add(new Quad(ldes_iri, new NamedNode(NAMESPACES.LDES + "timestampPath"), timestampPathObj));
                        }
                    } catch (IndexOutOfBoundsException e) {
                        logger.debug("No LDES timestamp path found");
                    }
                }

                // Optional unique IRI generation for mutable object IRIs
                try {
                    Term generateImmutableIRIObj = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalTarget,
                            new NamedNode(NAMESPACES.RMLT + "ldesGenerateImmutableIRI"), null)).get(0);
                    ldesGenerateImmutableIRI = generateImmutableIRIObj.getValue().equals("true");
                    logger.debug("LDES Immutable IRI generation: {}", ldesGenerateImmutableIRI? "yes": "no");
                } catch (IndexOutOfBoundsException e) {
                    logger.debug("No LDES generateImmutableIRI found");
                }

                /*
                 * If a member path is provided, use the subjects with a specific target class like SHACL targetClass.
                 * Otherwise, use the subjects of all generated triples as LDES members.
                 */
                try {
                    memberTargetClass = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalTarget,
                            new NamedNode(NAMESPACES.RMLT + "ldesMemberTargetClass"), null)).get(0);
                    logger.debug("LDES member target class: {}", memberTargetClass);
                } catch (IndexOutOfBoundsException e) {
                    logger.debug("No LDES member target class found");
                }

                List<Term> ldesMembers;
                if (memberTargetClass != null)
                    ldesMembers = Utils.getSubjectsFromQuads(outputStore.getQuads(null, new NamedNode(NAMESPACES.RDF + "type"), memberTargetClass));
                else
                    ldesMembers = outputStore.getSubjects();

                /*
                 * Add LDES member IRIs to the output. If needed, the member IRIs are made immutable if they aren't yet
                 * as required by the LDES specification.
                 */
                long currentTime = System.currentTimeMillis();
                long seed = (long)(Math.random() * 1000);
                long index = 0;
                Set<Term> processedMembers = new HashSet<>();
                for (Term m: ldesMembers) {
                    Term memberIRI = m;
                    if (processedMembers.contains(memberIRI))
                        continue;

                    processedMembers.add(memberIRI);
                    index++;

                    if (ldesGenerateImmutableIRI) {
                        /* avoid collisions by combining current time with a seed and a specific index for each member */
                        memberIRI = new NamedNode(m.getValue() + "#" + (currentTime + seed + index));

                        /*
                         * Add member versionOf if versionOf path is specified. If the mapping already provided one,
                         * use that instead. This is necessary because the provided IRI from the mapping is the IRI of
                         * the object while the immutable IRI is a version of the object.
                         */
                        if (versionOfPathObj != null) {
                            List<Term> versionOfObj = Utils.getObjectsFromQuads(outputStore.getQuads(m, versionOfPathObj, null));
                            if (versionOfObj.isEmpty()) {
                                outputStore.addQuad(new Quad(memberIRI, versionOfPathObj, m));
                            } else {
                                for (Term v : versionOfObj) {
                                    outputStore.addQuad(new Quad(memberIRI, versionOfPathObj, v));
                                }
                            }
                        }

                        /*
                         * Add member timestamp if timestamp path is specified. If the mapping already provided one,
                         * use that instead. This is necessary because the provided IRI from the mapping is the IRI of
                         * the object while the immutable IRI is a version of the object.
                         */
                        if (timestampPathObj != null) {
                            List<Term> timestampObj = Utils.getObjectsFromQuads(outputStore.getQuads(m, timestampPathObj, null));
                            if (timestampObj.isEmpty()) {
                                outputStore.addQuad(new Quad(memberIRI, timestampPathObj,
                                    new Literal(Instant.ofEpochMilli(currentTime).toString(), new NamedNode(NAMESPACES.XSD + "dateTime"))));
                            } else {
                                for (Term v : timestampObj) {
                                    outputStore.addQuad(new Quad(memberIRI, timestampPathObj, v));
                                }
                            }
                        }

                        /* Add all other member properties as well */
                        List<Quad> memberProperties = outputStore.getQuads(m, null, null);
                        for (Quad property: memberProperties) {
                            outputStore.addQuad(memberIRI, property.getPredicate(), property.getObject(), property.getGraph());
                            outputStore.removeQuads(property);
                        }
                    }

                    // Only materialize if LDES IRI was defined
                    if (ldes_iri != null) {
                        Quad q = new Quad(ldes_iri, new NamedNode(NAMESPACES.TREE + "member"), memberIRI);
                        metadata.add(q);
                    }
                }

                break;
            }
        }
    }

    /**
     * This method returns a Target instance based on the RML rules in rmlStore.
     * @param logicalTarget the Logical Target for which the Target needs to be created.
     * @param rmlStore a QuadStore with RML rules.
     * @param outputStore a QuadStore with the RDF triples to write to the target.
     */
    public Target getTarget(Term logicalTarget, QuadStore rmlStore, QuadStore outputStore) throws IOException {
        Target target;
        String serializationFormat = "nquads";
        String compression = null;
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
                throw new UnsupportedOperationException("Serialization format for " + location + " not implemented!");
            }
            return new LocalFileTarget(location, this.basePath, serializationFormat, null, metadata);
        }

        List<Term> targets = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalTarget, new NamedNode(NAMESPACES.RML2 + "target"), null));

        // Read serialization format
        try {
            String sf = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalTarget, new NamedNode(NAMESPACES.RML2 + "serialization"), null)).get(0).getValue();
            serializationFormat = switch (sf) {
                case NAMESPACES.FORMATS + "N-Triples" -> "ntriples";
                case NAMESPACES.FORMATS + "N-Quads" -> "nquads";
                case NAMESPACES.FORMATS + "JSON-LD" -> "jsonld";
                case NAMESPACES.FORMATS + "Turtle" -> "turtle";
                case NAMESPACES.FORMATS + "TriG" -> "trig";
                default -> throw new UnsupportedOperationException("Serialization format " + sf + " not implemented!");
            };
        }
        catch (IndexOutOfBoundsException e) {
            logger.debug("No serialization format specified, falling back to default N-Quads");
        }

        logger.debug("Serialization: " + serializationFormat);

        // Read compression
        try {
            Term comp = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalTarget,
                    new NamedNode(NAMESPACES.RML2 + "compression"), null)).get(0);
            compression = switch (comp.getValue()) {
                case NAMESPACES.COMP + "gzip" -> "gzip";
                case NAMESPACES.RML2 + "gzip" -> "gzip";
                case NAMESPACES.COMP + "zip" -> "zip";
                case NAMESPACES.RML2 + "zip" -> "zip";
                default -> throw new UnsupportedOperationException("Compression " + comp + " is not implemented!");
            };
            logger.debug("Compression: {}", compression);
        }
        catch (IndexOutOfBoundsException e) {
            logger.debug("Compression disabled");
        }

        // Detect LDES EventStreamTarget
        this.detectLDESEventStreamTarget(logicalTarget, metadata, rmlStore, outputStore);

        // Build target
        if (!targets.isEmpty()) {
            Term t = targets.get(0);
            logger.debug("getTarget() for {}", t.toString());

            // If not a literal, then we are dealing with a more complex description.
            List<Term> possibleTypes = Utils.getObjectsFromQuads(rmlStore.getQuads(t, new NamedNode(NAMESPACES.RDF + "type"), null));
            String targetType = null;
            for (Term p: possibleTypes) {
                // Skip any abstract rml:Target types
                if (!p.getValue().contains(NAMESPACES.RML2 + "Target")) {
                    targetType = p.getValue();
                    break;
                }
            }
            logger.debug("Target is IRI, target type: {}", targetType);

            switch(targetType) {
                case NAMESPACES.VOID + "Dataset": { // VoID Dataset
                    logger.debug("Target is a VoID Dataset");
                    String location = Utils.getObjectsFromQuads(rmlStore.getQuads(t,
                            new NamedNode(NAMESPACES.VOID + "dataDump"), null)).get(0).getValue();
                    location = location.replace("file://", ""); // Local file starts with file://
                    logger.debug("VoID datadump location: {}", location);
                    target = new LocalFileTarget(location, this.basePath, serializationFormat, compression, metadata);
                    break;
                }
                case NAMESPACES.DCAT + "Dataset": { // DCAT Dataset
                    logger.debug("Target is a DCAT Dataset");
                    String location = Utils.getObjectsFromQuads(rmlStore.getQuads(t,
                            new NamedNode(NAMESPACES.DCAT + "dataDump"), null)).get(0).getValue();
                    location = location.replace("file://", ""); // Local file starts with file://
                    logger.debug("DCAT datadump location: {}", location);
                    target = new LocalFileTarget(location, this.basePath, serializationFormat, compression, metadata);
                    break;
                }
                case NAMESPACES.DCAT + "Distribution": { // DCAT Distribution
                    logger.debug("Target is a DCAT Distribution");
                    String location = Utils.getObjectsFromQuads(rmlStore.getQuads(t,
                            new NamedNode(NAMESPACES.DCAT + "accessUrl"), null)).get(0).getValue();
                    location = location.replace("file://", ""); // Local file starts with file://
                    logger.debug("DCAT accessUrl location: {}", location);
                    target = new LocalFileTarget(location, this.basePath, serializationFormat, compression, metadata);
                    break;
                }
                case NAMESPACES.SD + "Service": { // SPARQL Service
                    logger.debug("Target is a SD Service");
                    String endpoint = Utils.getObjectsFromQuads(rmlStore.getQuads(t,
                            new NamedNode(NAMESPACES.SD + "endpoint"), null)).get(0).getValue();
                    String supportedLanguage = Utils.getObjectsFromQuads(rmlStore.getQuads(t,
                            new NamedNode(NAMESPACES.SD + "supportedLanguage"), null)).get(0).getValue();
                    logger.debug("SPARQL Service endpoint: {}", endpoint);
                    logger.debug("SPARQL Service supported language: {}", supportedLanguage);

                    // Check SPARQL UPDATE compatibility
                    if (!supportedLanguage.equals(NAMESPACES.SD + "SPARQL11Update")) {
                        throw new IllegalArgumentException("SPARQL Service target must support SPARQL Update!");
                    }

                    // Try to instantiate a SPARQL endpoint
                    target = new SPARQLEndpointTarget(endpoint, metadata);
                    break;
                }
                default: {
                    throw new UnsupportedOperationException("Not implemented");
                }
            }
            logger.debug("Target created: {}", target);
            return target;
        }
        else {
            throw new Error("The Target does not have target.");
        }
    }
}

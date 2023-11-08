package be.ugent.rml.target;

import be.ugent.rml.NAMESPACES;
import be.ugent.rml.Utils;
import be.ugent.rml.store.Quad;
import be.ugent.rml.store.QuadStore;
import org.apache.commons.lang3.NotImplementedException;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TargetFactory {

    private static final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    // The path used when local paths are not absolute.
    private final String basePath;
    private static final Logger logger = LoggerFactory.getLogger(TargetFactory.class);

    /**
     * The constructor of the TargetFactory.
     */
    public TargetFactory(String basePath) {
        this.basePath = basePath;
    }

    private void detectLDESEventStreamTarget(Value logicalTarget, List<Quad> metadata, QuadStore rmlStore, QuadStore outputStore) {
        List<Value> types = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalTarget,
                valueFactory.createIRI(NAMESPACES.RDF + "type"), null));
        for (Value type: types) {
            // Target has LDES features, read them
            if (type.stringValue().equals(NAMESPACES.LDES + "EventStreamTarget")) {
                logger.error("'{}EventStreamTarget' is not supported anymore. Use '{}/EventStreamTarget'. Not generating LDES metadata!", NAMESPACES.LDES, NAMESPACES.RMLT);
                return;
            } else if (type.stringValue().equals(NAMESPACES.RMLT + "EventStreamTarget")) {
                logger.debug("Found RMLT EventStreamTarget");
                Value iri;
                Value ldes_iri = null;
                Value ldes = null;
                Value versionOfPathObj = null;
                Value timestampPathObj = null;
                Value memberTargetClass = null;
                boolean ldesGenerateImmutableIRI = false;

                try {
                    // Check if LDES IRI is given
                    iri = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalTarget,
                            valueFactory.createIRI(NAMESPACES.RMLT + "ldesBaseIRI"), null)).get(0);
                    ldes_iri = valueFactory.createIRI(iri.stringValue());
                    logger.debug("LDES base IRI: {}", iri.stringValue());

                    // LDES RDF type EventStream
                    metadata.add(new Quad(ldes_iri, valueFactory.createIRI(NAMESPACES.RDF + "type"),
                        valueFactory.createIRI(NAMESPACES.LDES + "EventStream")));
                }
                catch (IndexOutOfBoundsException e) {
                    logger.debug("No LDES metadata will be generated since no LDES base IRI was specified");
                }

                try {
                    // LDES Member configuration properties
                    ldes = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalTarget,
                            valueFactory.createIRI(NAMESPACES.RMLT + "ldes"), null)).get(0);
                } catch (IndexOutOfBoundsException e) {
                    logger.debug("No LDES member metadata found.");
                }

                if (ldes != null) {
                    // Optional SHACL shape
                    if (ldes_iri != null) {
                        try {
                            Value shape = Utils.getObjectsFromQuads(rmlStore.getQuads(ldes,
                                valueFactory.createIRI(NAMESPACES.TREE + "shape"), null)).get(0);
                            logger.debug("SHACL shape: {}", shape.stringValue());
                            // TODO: Handle embedded SHACL shapes in RML mapping rules as well.
                            metadata.add(new Quad(ldes_iri, valueFactory.createIRI(NAMESPACES.TREE + "shape"), shape));
                        } catch (IndexOutOfBoundsException e) {
                            logger.debug("No TREE SHACL shape specified for LDES.");
                        }
                    }
                    
                    // Optional versionOf path
                    try {
                        versionOfPathObj = Utils.getObjectsFromQuads(rmlStore.getQuads(ldes,
                                valueFactory.createIRI(NAMESPACES.LDES + "versionOfPath"), null)).get(0);
                        logger.debug("VersionOf path: {}", versionOfPathObj.stringValue());
                        if (ldes_iri != null) {
                            metadata.add(new Quad(ldes_iri, valueFactory.createIRI(NAMESPACES.LDES + "versionOfPath"), versionOfPathObj));
                        }
                    } catch (IndexOutOfBoundsException e) {
                        logger.debug("No LDES versionOf path found");
                    }

                    // Optional timestamp path
                    try {
                        timestampPathObj = Utils.getObjectsFromQuads(rmlStore.getQuads(ldes,
                                valueFactory.createIRI(NAMESPACES.LDES + "timestampPath"), null)).get(0);
                        if (ldes_iri != null) {
                            metadata.add(new Quad(ldes_iri, valueFactory.createIRI(NAMESPACES.LDES + "timestampPath"), timestampPathObj));
                        }
                    } catch (IndexOutOfBoundsException e) {
                        logger.debug("No LDES timestamp path found");
                    }
                }

                // Optional unique IRI generation for mutable object IRIs
                try {
                    Value generateImmutableIRIObj = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalTarget,
                            valueFactory.createIRI(NAMESPACES.RMLT + "ldesGenerateImmutableIRI"), null)).get(0);
                    ldesGenerateImmutableIRI = generateImmutableIRIObj.stringValue().equals("true");
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
                            valueFactory.createIRI(NAMESPACES.RMLT + "ldesMemberTargetClass"), null)).get(0);
                    logger.debug("LDES member target class: {}", memberTargetClass);
                } catch (IndexOutOfBoundsException e) {
                    logger.debug("No LDES member target class found");
                }

                List<Value> ldesMembers;
                if (memberTargetClass != null)
                    ldesMembers = Utils.getSubjectsFromQuads(outputStore.getQuads(null, valueFactory.createIRI(NAMESPACES.RDF + "type"), memberTargetClass));
                else
                    ldesMembers = outputStore.getSubjects();

                /*
                 * Add LDES member IRIs to the output. If needed, the member IRIs are made immutable if they aren't yet
                 * as required by the LDES specification.
                 */
                long currentTime = System.currentTimeMillis();
                long seed = (long)(Math.random() * 1000);
                long index = 0;
                HashSet<Value> processedMembers = new HashSet<>();
                for (Value m: ldesMembers) {
                    Value memberIRI = m;
                    if (processedMembers.contains(memberIRI))
                        continue;

                    processedMembers.add(memberIRI);
                    index++;

                    if (ldesGenerateImmutableIRI) {
                        /* avoid collisions by combining current time with a seed and a specific index for each member */
                        memberIRI = valueFactory.createIRI(m.stringValue() + "#" + (currentTime + seed + index));

                        /*
                         * Add member versionOf if versionOf path is specified. If the mapping already provided one,
                         * use that instead. This is necessary because the provided IRI from the mapping is the IRI of
                         * the object while the immutable IRI is a version of the object.
                         */
                        if (versionOfPathObj != null) {
                            List<Value> versionOfObj = Utils.getObjectsFromQuads(outputStore.getQuads(m, versionOfPathObj, null));
                            if (versionOfObj.isEmpty()) {
                                outputStore.addQuad(new Quad(memberIRI, versionOfPathObj, m));
                            } else {
                                for (Value v : versionOfObj) {
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
                            List<Value> timestampObj = Utils.getObjectsFromQuads(outputStore.getQuads(m, timestampPathObj, null));
                            if (timestampObj.isEmpty()) {
                                outputStore.addQuad(new Quad(memberIRI, timestampPathObj,
                                    valueFactory.createLiteral(Instant.ofEpochMilli(currentTime).toString(), valueFactory.createIRI(NAMESPACES.XSD + "dateTime"))));
                            } else {
                                for (Value v : timestampObj) {
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
                        Quad q = new Quad(ldes_iri, valueFactory.createIRI(NAMESPACES.TREE + "member"), memberIRI);
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
    public Target getTarget(Value logicalTarget, QuadStore rmlStore, QuadStore outputStore) throws NotImplementedException, IOException {
        Target target = null;
        String serializationFormat = "nquads";
        String compression = null;
        List<Quad> metadata = new ArrayList<>();

        // Old Logical Source reference is supported for Logical Targets as well for backwards compatibility
        if (logicalTarget.isLiteral()) {
            logger.warn("Legacy string output path for Target found, do not use, this is only supported for backwards compatibility reasons.");
            String location = logicalTarget.stringValue();
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

        List<Value> targets = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalTarget,
                valueFactory.createIRI(NAMESPACES.RMLT + "target"), null));

        // Read serialization format
        try {
            String sf = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalTarget, valueFactory.createIRI(NAMESPACES.RMLT + "serialization"), null)).get(0).stringValue();
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

        logger.debug("Serialization: " + serializationFormat);

        // Read compression
        try {
            Value comp = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalTarget,
                    valueFactory.createIRI(NAMESPACES.RMLT + "compression"), null)).get(0);
            switch (comp.stringValue()) {
                case NAMESPACES.COMP + "gzip":
                    compression = "gzip";
                    break;
                case NAMESPACES.COMP + "zip":
                    compression = "zip";
                    break;
                default:
                    throw new NotImplementedException("Compression " + comp + " is not implemented!");
            }
            logger.debug("Compression: {}", compression);
        }
        catch (IndexOutOfBoundsException e) {
            logger.debug("Compression disabled");
        }

        // Detect LDES EventStreamTarget
        this.detectLDESEventStreamTarget(logicalTarget, metadata, rmlStore, outputStore);

        // Build target
        if (!targets.isEmpty()) {
            Value t = targets.get(0);
            logger.debug("getTarget() for {}", t.toString());

            // If not a literal, then we are dealing with a more complex description.
            String targetType = Utils.getObjectsFromQuads(rmlStore.getQuads(t,
                    valueFactory.createIRI(NAMESPACES.RDF + "type"), null)).get(0).stringValue();
            logger.debug("Target is IRI, target type: {}", targetType);

            switch(targetType) {
                case NAMESPACES.VOID + "Dataset": { // VoID Dataset
                    logger.debug("Target is a VoID Dataset");
                    String location = Utils.getObjectsFromQuads(rmlStore.getQuads(t,
                            valueFactory.createIRI(NAMESPACES.VOID + "dataDump"), null)).get(0).stringValue();
                    location = location.replace("file://", ""); // Local file starts with file://
                    logger.debug("VoID datadump location: {}", location);
                    target = new LocalFileTarget(location, this.basePath, serializationFormat, compression, metadata);
                    break;
                }
                case NAMESPACES.DCAT + "Dataset": { // DCAT Dataset
                    logger.debug("Target is a DCAT Dataset");
                    String location = Utils.getObjectsFromQuads(rmlStore.getQuads(t,
                            valueFactory.createIRI(NAMESPACES.DCAT + "dataDump"), null)).get(0).stringValue();
                    location = location.replace("file://", ""); // Local file starts with file://
                    logger.debug("DCAT datadump location: {}", location);
                    target = new LocalFileTarget(location, this.basePath, serializationFormat, compression, metadata);
                    break;
                }
                case NAMESPACES.SD + "Service": { // SPARQL Service
                    logger.debug("Target is a SD Service");
                    String endpoint = Utils.getObjectsFromQuads(rmlStore.getQuads(t,
                            valueFactory.createIRI(NAMESPACES.SD + "endpoint"), null)).get(0).stringValue();
                    String supportedLanguage = Utils.getObjectsFromQuads(rmlStore.getQuads(t,
                            valueFactory.createIRI(NAMESPACES.SD + "supportedLanguage"), null)).get(0).stringValue();
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
                    throw new NotImplementedException("Not implemented");
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

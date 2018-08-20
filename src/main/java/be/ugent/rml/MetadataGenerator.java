package be.ugent.rml;

import be.ugent.rml.store.*;
import be.ugent.rml.term.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Class that encapsulates the generation of metadata.
 * (Does everything for metadata generation)
 */
public class MetadataGenerator {

    // Higher level --> more detailed
    public enum DETAIL_LEVEL {
        PREVENT(-1), DATASET(0), TRIPLE(1), TERM(2);

        private int level;
        DETAIL_LEVEL(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }

    private QuadStore mdStore;
    private DETAIL_LEVEL detailLevel;
    private String outputFile;
    private QuadStore inputData;
    private String mappingFile;
    private List<BiConsumer<Term, ProvenancedQuad>> generationFunctions;
    private List<Term> logicalSources;
    private Set<String> distinctSubjects;    // Used for counting number of distinct subjects
    private Set<String> distinctObjects;     // Used for counting number of distinct objects
    private Set<String> distinctClasses;     // Used for counting number of distinct classes
    private Set<String> distinctProperties;  // Used for counting number of distinct properties

    Term rdfDataset;
    Term rdfDatasetGeneration;
    Term rmlProcessor;

    public MetadataGenerator(DETAIL_LEVEL detailLevel, String outputFile, String mappingFile, QuadStore inputData) {
        mdStore = new SimpleQuadStore();
        this.detailLevel = detailLevel;
        this.outputFile = outputFile;
        this.inputData = inputData;
        this.mappingFile = mappingFile;

        distinctSubjects = new HashSet<>();
        distinctObjects = new HashSet<>();
        distinctClasses = new HashSet<>();
        distinctProperties = new HashSet<>();

        generationFunctions = new ArrayList<>();

        if (detailLevel.getLevel() >= DETAIL_LEVEL.TRIPLE.getLevel()) {
            // Add source triplesMap info
            generationFunctions.add((node, pquad) -> {
                // Get triplesMaps (Subject always has one, object does not always have one)
                Term subjectTM = pquad.getSubject().getMetdata().getTriplesMap();
                mdStore.addTriple(node, new NamedNode(NAMESPACES.PROV + "wasDerivedFrom"), subjectTM);

                Term objectTM = null;
                if (pquad.getObject().getMetdata() != null && pquad.getObject().getMetdata().getTriplesMap() != null) {
                    objectTM = pquad.getObject().getMetdata().getTriplesMap();
                    mdStore.addTriple(node, new NamedNode(NAMESPACES.PROV + "wasDerivedFrom"), objectTM);
                }

                if (detailLevel.getLevel() >= DETAIL_LEVEL.TERM.getLevel()) {
                    mdStore.addTriple(pquad.getSubject().getTerm(), new NamedNode(NAMESPACES.PROV + "wasDerivedFrom"), subjectTM);
                    if (objectTM != null) {
                        mdStore.addTriple(pquad.getObject().getTerm(), new NamedNode(NAMESPACES.PROV + "wasDerivedFrom"), objectTM);
                    }
                }
            });
            // Add generation time info
            generationFunctions.add((node, pquad) -> {
                mdStore.addTriple(node, new NamedNode(NAMESPACES.PROV + "generatedAtTime"),
                        new Literal(Instant.now().toString(), new AbstractTerm(NAMESPACES.XSD + "dateTime")));
            });
            // Add counters
            generationFunctions.add((node, pquad) -> {
                distinctSubjects.add(pquad.getSubject().getTerm().getValue());
                distinctObjects.add(pquad.getObject().getTerm().getValue());
                distinctProperties.add(pquad.getPredicate().getTerm().getValue());
                if (pquad.getPredicate().getTerm().getValue().equals(NAMESPACES.RDF + "type")) {
                    distinctClasses.add(pquad.getObject().getTerm().getValue());
                }
            });
        }
    }

    public void insertQuad(ProvenancedQuad provenancedQuad) {
        // Value: hash of subject + predicate + object
        Term node = new BlankNode(Utils.hashCode(provenancedQuad.getSubject().getTerm().getValue() +
                                                             provenancedQuad.getPredicate().getTerm().getValue() +
                                                             provenancedQuad.getObject().getTerm().getValue()));

        mdStore.addTriple(node, new NamedNode(NAMESPACES.RDF + "type"), new NamedNode(NAMESPACES.RDF + "Statement"));
        mdStore.addTriple(node, new NamedNode(NAMESPACES.RDF + "subject"), provenancedQuad.getSubject().getTerm());
        mdStore.addTriple(node, new NamedNode(NAMESPACES.RDF + "predicate"), provenancedQuad.getPredicate().getTerm());
        mdStore.addTriple(node, new NamedNode(NAMESPACES.RDF + "object"), provenancedQuad.getObject().getTerm());

        for (BiConsumer<Term, ProvenancedQuad> function: generationFunctions) {
            function.accept(node, provenancedQuad);
        }
    }

    public void postMappingGeneration(String startTimestamp, String stopTimestamp, List<Term> triplesMaps, QuadStore result) {
        if (detailLevel.getLevel() >= DETAIL_LEVEL.DATASET.getLevel()) {
            DatasetLevelMetadataGenerator.createMetadata(getRdfDataset(), getRdfDatasetGeneration(), getRmlProcessor(),
                    mdStore, getLogicalSources(triplesMaps, inputData), startTimestamp, stopTimestamp, mappingFile);
            if (detailLevel.getLevel() > DETAIL_LEVEL.TRIPLE.getLevel()) {
                generateTripleLevelDetailMetadata(triplesMaps, result);
            }
        }
    }

    private void generateTripleLevelDetailMetadata(List<Term> triplesMaps, QuadStore result) {
        if (detailLevel == DETAIL_LEVEL.PREVENT) {
            return;
        }

        // Describe triplesMaps
        for (Term triplesMap: triplesMaps) {
            mdStore.addTriple(triplesMap, new NamedNode(NAMESPACES.RDF + "type"), new NamedNode(NAMESPACES.PROV + "Entity"));
            mdStore.addTriple(triplesMap, new NamedNode(NAMESPACES.RDF + "type"), new NamedNode(NAMESPACES.VOID + "Dataset"));
            mdStore.addTriple(triplesMap, new NamedNode(NAMESPACES.VOID + "dataDump"), new NamedNode(outputFile));
        }

        // Describe result
        Term resultNode = getRdfDataset();
        mdStore.addTriple(resultNode, new NamedNode(NAMESPACES.RDF + "type"), new NamedNode(NAMESPACES.PROV + "Entity"));
        mdStore.addTriple(resultNode, new NamedNode(NAMESPACES.RDF + "type"), new NamedNode(NAMESPACES.VOID + "Dataset"));

        getLogicalSources(triplesMaps, inputData).forEach(inputSource -> {
            mdStore.addTriple(resultNode, new NamedNode(NAMESPACES.PROV + "wasDerivedFrom"), inputSource);
        });

        mdStore.addTriple(resultNode, new NamedNode(NAMESPACES.PROV + "wasGeneratedBy"), getRdfDatasetGeneration());

        mdStore.addTriple(resultNode, new NamedNode(NAMESPACES.VOID + "triples"),
                new Literal(Integer.toString(result.getQuads(null, null, null, null).size())
                        , new AbstractTerm(NAMESPACES.XSD + "integer")));
        mdStore.addTriple(resultNode, new NamedNode(NAMESPACES.VOID + "distinctSubjects"),
                new Literal(Integer.toString(distinctSubjects.size())
                        , new AbstractTerm(NAMESPACES.XSD + "integer")));
        mdStore.addTriple(resultNode, new NamedNode(NAMESPACES.VOID + "distinctObjects"),
                new Literal(Integer.toString(distinctObjects.size())
                        , new AbstractTerm(NAMESPACES.XSD + "integer")));
        mdStore.addTriple(resultNode, new NamedNode(NAMESPACES.VOID + "classes"),
                new Literal(Integer.toString(distinctClasses.size())
                        , new AbstractTerm(NAMESPACES.XSD + "integer")));
        mdStore.addTriple(resultNode, new NamedNode(NAMESPACES.VOID + "properties"),
                new Literal(Integer.toString(distinctProperties.size())
                        , new AbstractTerm(NAMESPACES.XSD + "integer")));

        mdStore.addTriple(resultNode, new NamedNode(NAMESPACES.VOID + "documents"),
                new Literal("1"     // todo: change this when multiple output files are possible
                        , new AbstractTerm(NAMESPACES.XSD + "integer")));

        mdStore.addTriple(resultNode, new NamedNode(NAMESPACES.VOID + "feature"),
                new NamedNode("http://www.w3.org/ns/formats/N-Quads")); // todo: change this when output file format changes
    }

    public List<Term> getLogicalSources(List<Term> triplesMaps, QuadStore rmlStore) {
        if (logicalSources == null) {
            logicalSources = new ArrayList<>();
            for(Term triplesMap: triplesMaps) {
                List<Term> logicalSourcesObjects = Utils.getObjectsFromQuads(rmlStore.getQuads(triplesMap,
                        new NamedNode(NAMESPACES.RML + "logicalSource"), null));

                if (logicalSourcesObjects.isEmpty()) {
                    throw new Error("No Logical Source is found for " + triplesMap + ". Exactly one Logical Source is required per Triples Map.");
                }

                Term logicalSource = logicalSourcesObjects.get(0);

                if (Utils.isBlankNode(logicalSource.toString())) {
                    List<Term> sourceObjects = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource,
                            new NamedNode(NAMESPACES.RML + "source"), null));

                    if (sourceObjects.isEmpty()) {
                        throw new Error("No Source is found for " + triplesMap + ". Exactly one Source is required per Logical Source.");
                    }

                    Term source = sourceObjects.get(0);
                    Term sourceNode;

                    // Literal -- encapsulate source in blank node
                    if (Utils.isLiteral(source.toString())) {
                        sourceNode = new NamedNode(String.format("file:%s",sourceObjects.get(0).getValue()));
                    } else {    // todo: what with blank nodes?
                        sourceNode = source;
                    }
                    logicalSources.add(sourceNode);
                } else {
                    logicalSources.add(logicalSource);
                }
            }
        }
        return logicalSources;
    }

    public void writeMetadata() {
        if (detailLevel != DETAIL_LEVEL.PREVENT) {
            mdStore.removeDuplicates();
            TriplesQuads tq = Utils.getTriplesAndQuads(mdStore.toSimpleSortedQuadStore().getQuads(null, null, null, null));
            Utils.writeOutput("triple", tq.getTriples(), "nq", outputFile);
        }
    }

    public Term getRdfDataset() {
        if (rdfDataset == null) {
            rdfDataset = new NamedNode(String.format("file:%s", outputFile));
        }
        return rdfDataset;
    }

    public Term getRdfDatasetGeneration() {
        if (rdfDatasetGeneration == null) {
            // Value: hash of output file path
            rdfDatasetGeneration = new BlankNode(Utils.hashCode(outputFile));
        }
        return rdfDatasetGeneration;
    }

    public Term getRmlProcessor() {
        if (rmlProcessor == null) {
            rmlProcessor = new BlankNode("RMLProcessor");
        }
        return rmlProcessor;
    }

    public DETAIL_LEVEL getDetailLevel() {
        return detailLevel;
    }
}

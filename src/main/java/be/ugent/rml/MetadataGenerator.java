package be.ugent.rml;

import be.ugent.rml.store.ProvenancedQuad;
import be.ugent.rml.store.Quad;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.SimpleQuadStore;
import be.ugent.rml.term.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Class that encapsulates the generation of metadata.
 * (Does everything for metadata generation)
 */
public class MetadataGenerator {

    public enum DETAIL_LEVEL {
        DATASET, TRIPLE, TERM;
    }

    private QuadStore mdStore;
    private Set<DETAIL_LEVEL> detailLevels;
    private String outputFile;
    private QuadStore inputData;
    private String mappingFile;
    private List<BiConsumer<Term, ProvenancedQuad>> generationFunctions;
    private List<Term> logicalSources;
    private Set<String> distinctSubjects;    // Used for counting number of distinct subjects
    private Set<String> distinctObjects;     // Used for counting number of distinct objects
    private Set<String> distinctClasses;     // Used for counting number of distinct classes
    private Set<String> distinctProperties;  // Used for counting number of distinct properties

    public MetadataGenerator(Set<DETAIL_LEVEL> detailLevels, String outputFile, String mappingFile, QuadStore inputData) {
        mdStore = new SimpleQuadStore();
        this.detailLevels = detailLevels;
        this.outputFile = outputFile;
        this.inputData = inputData;
        this.mappingFile = mappingFile;
        distinctSubjects = new HashSet<>();
        distinctObjects = new HashSet<>();
        distinctClasses = new HashSet<>();
        distinctProperties = new HashSet<>();

        generationFunctions = new ArrayList<>();

        if (detailLevels.contains(DETAIL_LEVEL.TRIPLE)) {
            // Add source triplesMap info
            generationFunctions.add((node, pquad) -> {
                // Get triplesMaps (Subject always has one, object does not always have one)
                Term subjectTM = pquad.getSubject().getMetdata().getTriplesMap();
                mdStore.addTriple(node, new NamedNode(NAMESPACES.PROV + "wasGeneratedBy"), subjectTM);

                if (pquad.getObject().getMetdata() != null && pquad.getObject().getMetdata().getTriplesMap() != null) {
                    Term objectTM = pquad.getObject().getMetdata().getTriplesMap();
                    mdStore.addTriple(node, new NamedNode(NAMESPACES.PROV + "wasGeneratedBy"), objectTM);
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
        Term node = new BlankNode();

        mdStore.addTriple(node, new NamedNode(NAMESPACES.RDF + "type"), new NamedNode(NAMESPACES.RDF + "Statement"));
        mdStore.addTriple(node, new NamedNode(NAMESPACES.RDF + "subject"), provenancedQuad.getSubject().getTerm());
        mdStore.addTriple(node, new NamedNode(NAMESPACES.RDF + "predicate"), provenancedQuad.getPredicate().getTerm());
        mdStore.addTriple(node, new NamedNode(NAMESPACES.RDF + "object"), provenancedQuad.getObject().getTerm());

        for (BiConsumer<Term, ProvenancedQuad> function: generationFunctions) {
            function.accept(node, provenancedQuad);
        }
    }

    public void postMappingGeneration(String startTimestamp, String stopTimestamp, List<Term> triplesMaps, QuadStore result) {
        if (detailLevels.contains(DETAIL_LEVEL.DATASET)) {
            DatasetLevelMetadataGenerator.createMetadata(mdStore, outputFile, getLogicalSources(triplesMaps, inputData), startTimestamp, stopTimestamp,
                    mappingFile);
        }
        if (detailLevels.contains(DETAIL_LEVEL.TRIPLE)) {
            // Describe triplesMaps
            for (Term triplesMap: triplesMaps) {
                mdStore.addTriple(triplesMap, new NamedNode(NAMESPACES.RDF + "type"), new NamedNode(NAMESPACES.PROV + "Entity"));
                mdStore.addTriple(triplesMap, new NamedNode(NAMESPACES.RDF + "type"), new NamedNode(NAMESPACES.VOID + "Dataset"));
                mdStore.addTriple(triplesMap, new NamedNode(NAMESPACES.VOID + "dataDump"), new NamedNode(outputFile));
            }

            // Describe result
            Term resultNode = new NamedNode(String.format("file:%s", outputFile));
            mdStore.addTriple(resultNode, new NamedNode(NAMESPACES.RDF + "type"), new NamedNode(NAMESPACES.PROV + "Entity"));
            mdStore.addTriple(resultNode, new NamedNode(NAMESPACES.RDF + "type"), new NamedNode(NAMESPACES.VOID + "Dataset"));

            getLogicalSources(triplesMaps, inputData).forEach(inputSource -> {
                mdStore.addTriple(resultNode, new NamedNode(NAMESPACES.PROV + "wasDerivedFrom"), inputSource);
            });

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

        }
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
}

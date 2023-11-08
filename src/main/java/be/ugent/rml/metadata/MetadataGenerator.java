package be.ugent.rml.metadata;

import be.ugent.rml.NAMESPACES;
import be.ugent.rml.Utils;
import be.ugent.rml.store.*;
import be.ugent.rml.term.*;

import java.time.Instant;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * Class that encapsulates the generation of metadata.
 * (Does everything for metadata generation)
 */
public class MetadataGenerator {

    // Higher level --> more detailed
    public enum DETAIL_LEVEL {
        DATASET(0), TRIPLE(1), TERM(2);

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
    private QuadStore inputData;
    private String[] mappingFiles;
    private List<Term> triplesMaps;
    private List<BiConsumer<Term, ProvenancedQuad>> generationFunctions;    // Will contain different functions according to requested metadata detail level
    private List<Term> logicalSources;
    private Set<String> distinctSubjects;    // Used for counting number of distinct subjects
    private Set<String> distinctObjects;     // Used for counting number of distinct objects
    private Set<String> distinctClasses;     // Used for counting number of distinct classes
    private Set<String> distinctProperties;  // Used for counting number of distinct properties

    private Map<Term, Term> triplesMaptoActivityMap;
    private Map<Term, Term> termMaptoActivityMap;

    private Term rdfDataset;
    private Term rdfDatasetGeneration;
    private Term rmlMapper;

    public MetadataGenerator(DETAIL_LEVEL detailLevel, String outputFile, String[] mappingFiles, QuadStore inputData, QuadStore metadataStore) {
        mdStore = metadataStore;
        this.detailLevel = detailLevel;
        this.inputData = inputData;
        this.mappingFiles = mappingFiles;

        distinctSubjects = new HashSet<>();
        distinctObjects = new HashSet<>();
        distinctClasses = new HashSet<>();
        distinctProperties = new HashSet<>();

        generationFunctions = new ArrayList<>();

        rdfDataset = new NamedNode(String.format("file://%s", outputFile));
        rdfDatasetGeneration = new BlankNode(Utils.hashCode(outputFile));

        rmlMapper = new NamedNode("http://rml.io/tool/rmlmapper-java");


        if (detailLevel.getLevel() >= DETAIL_LEVEL.TRIPLE.getLevel()) {
            addTripleLevelFunctions();
        }
        if (detailLevel.getLevel() >= DETAIL_LEVEL.TERM.getLevel()) {
            addTermLevelFunctions();
        }
    }

    public MetadataGenerator(DETAIL_LEVEL detailLevel, String outputFile, String[] mappingFiles, QuadStore inputData) {
        this(detailLevel, outputFile, mappingFiles, inputData, new SimpleQuadStore());
    }

    /**
     * Gets called every time a quad is generated.
     * Creates a node representing the quad.
     * Applies the metadatageneration functions to the given quad.
     *
     * @param provenancedQuad provenanced Quad
     */
    public void insertQuad(ProvenancedQuad provenancedQuad) {
        if (provenancedQuad.getSubject() != null & provenancedQuad.getPredicate() != null & provenancedQuad.getObject() != null) {
            // Value: hash of subject + predicate + object
            Term node = new BlankNode(Utils.hashCode(provenancedQuad.getSubject().getTerm().getValue() +
                    provenancedQuad.getPredicate().getTerm().getValue() +
                    provenancedQuad.getObject().getTerm().getValue()));

            mdStore.addQuad(node, new NamedNode(NAMESPACES.RDF + "type"), new NamedNode(NAMESPACES.RDF + "Statement"));
            mdStore.addQuad(node, new NamedNode(NAMESPACES.RDF + "subject"), provenancedQuad.getSubject().getTerm());
            mdStore.addQuad(node, new NamedNode(NAMESPACES.RDF + "predicate"), provenancedQuad.getPredicate().getTerm());
            mdStore.addQuad(node, new NamedNode(NAMESPACES.RDF + "object"), provenancedQuad.getObject().getTerm());

            for (BiConsumer<Term, ProvenancedQuad> function : generationFunctions) {
                function.accept(node, provenancedQuad);
            }
        }
    }

    /**
     * Generates metadata before the actual mapping.
     *
     * @param triplesMaps list of tripleMap terms
     * @param mappingQuads mapping quadstore
     */
    public void preMappingGeneration(List<Term> triplesMaps, QuadStore mappingQuads) {
        this.triplesMaps = triplesMaps;
        if (detailLevel.getLevel() >= DETAIL_LEVEL.TRIPLE.getLevel()) {
            generatePreTripleLevelDetailMetadata();
            if (detailLevel.getLevel() >= DETAIL_LEVEL.TERM.getLevel()) {
                generatePreTermLevelDetailMetadata(mappingQuads);
            }
        }
    }

    /**
     * Generates metadata after the actual mapping.
     *
     * @param startTimestamp string of starting timestamp
     * @param stopTimestamp string of stopping timestamp
     * @param result result quadstore
     */
    public void postMappingGeneration(String startTimestamp, String stopTimestamp, QuadStore result) {
        if (detailLevel.getLevel() >= DETAIL_LEVEL.DATASET.getLevel()) {
            DatasetLevelMetadataGenerator.createMetadata(rdfDataset, rdfDatasetGeneration, rmlMapper,
                    mdStore, getLogicalSources(triplesMaps, inputData), startTimestamp, stopTimestamp, mappingFiles);
            if (detailLevel.getLevel() >= DETAIL_LEVEL.TRIPLE.getLevel()) {
                generatePostTripleLevelDetailMetadata(result);
            }
        }
    }

    private void generatePreTripleLevelDetailMetadata() {
        triplesMaptoActivityMap = new HashMap<>();

        // Describe triplesMaps
        for (Term triplesMap : triplesMaps) {
            mdStore.addQuad(triplesMap, new NamedNode(NAMESPACES.RDF + "type"), new NamedNode(NAMESPACES.PROV + "Entity"));
            mdStore.addQuad(triplesMap, new NamedNode(NAMESPACES.RDF + "type"), new NamedNode(NAMESPACES.VOID + "Dataset"));
            mdStore.addQuad(triplesMap, new NamedNode(NAMESPACES.VOID + "dataDump"), rdfDataset);
            createActivityStatements(triplesMap, triplesMaptoActivityMap);
        }
    }

    private void generatePreTermLevelDetailMetadata(QuadStore mappingQuads) {
        termMaptoActivityMap = new HashMap<>();

        for (Term triplesMap : triplesMaps) {
            List<Term> subjectMaps = Utils.getObjectsFromQuads(mappingQuads.getQuads(triplesMap, new NamedNode(NAMESPACES.RR + "subjectMap"),
                    null));

            if (!subjectMaps.isEmpty()) {
                Term subjectMap = subjectMaps.get(0);
                createActivityStatementsWithResultActivity(subjectMap, termMaptoActivityMap, triplesMaptoActivityMap.get(triplesMap));
            }

            List<Term> predicateObjectMaps = Utils.getObjectsFromQuads(mappingQuads.getQuads(triplesMap, new NamedNode(NAMESPACES.RR + "predicateObjectMap"),
                    null));

            for (Term pom : predicateObjectMaps) {
                Term pomActivity = createActivityStatementsWithResultActivity(pom, termMaptoActivityMap, triplesMaptoActivityMap.get(triplesMap));

                List<Term> predicateMaps = Utils.getObjectsFromQuads(mappingQuads.getQuads(pom, new NamedNode(NAMESPACES.RR + "predicateMap"),
                        null));
                List<Term> objectMaps = Utils.getObjectsFromQuads(mappingQuads.getQuads(pom, new NamedNode(NAMESPACES.RR + "objectMap"),
                        null));

                createActivityStatementsWithResultActivity(predicateMaps, termMaptoActivityMap, pomActivity);
                createActivityStatementsWithResultActivity(objectMaps, termMaptoActivityMap, pomActivity);
            }
        }
    }

    private Term createActivityStatements(Term termMap, Map<Term, Term> map) {
        Term termMapActivity;
        if (termMap instanceof BlankNode) {
            termMapActivity = new BlankNode(termMap.getValue() + "Activity");
        } else {
            termMapActivity = new NamedNode(termMap.getValue() + "Activity");
        }
        if (map != null) {
            map.put(termMap, termMapActivity);
        }
        mdStore.addQuad(termMapActivity, new NamedNode(NAMESPACES.RDF + "type"), new NamedNode(NAMESPACES.PROV + "Activity"));
        mdStore.addQuad(termMapActivity, new NamedNode(NAMESPACES.PROV + "used"), termMap);
        return termMapActivity;
    }

    private Term createActivityStatementsWithResultActivity(Term termMap, Map<Term, Term> map, Term resultActivity) {
        Term termMapActivity = createActivityStatements(termMap, map);
        mdStore.addQuad(resultActivity, new NamedNode(NAMESPACES.PROV + "wasInformedBy"),
                termMapActivity);
        return termMapActivity;
    }

    private Term createActivityStatementsWithResultActivity(List<Term> termMaps, Map<Term, Term> map, Term resultActivity) {
        if (!termMaps.isEmpty()) {
            return createActivityStatementsWithResultActivity(termMaps.get(0), map, resultActivity);
        }
        return null;
    }

    private void generatePostTripleLevelDetailMetadata(QuadStore result) {
        // Describe result
        mdStore.addQuad(rdfDataset, new NamedNode(NAMESPACES.RDF + "type"), new NamedNode(NAMESPACES.PROV + "Entity"));
        mdStore.addQuad(rdfDataset, new NamedNode(NAMESPACES.RDF + "type"), new NamedNode(NAMESPACES.VOID + "Dataset"));

        getLogicalSources(triplesMaps, inputData).forEach(inputSource -> {
            mdStore.addQuad(rdfDataset, new NamedNode(NAMESPACES.PROV + "wasDerivedFrom"), inputSource);
        });

        mdStore.addQuad(rdfDataset, new NamedNode(NAMESPACES.PROV + "wasGeneratedBy"), rdfDatasetGeneration);

        mdStore.addQuad(rdfDataset, new NamedNode(NAMESPACES.VOID + "triples"),
                new Literal(Integer.toString(result.getQuads(null, null, null, null).size())
                        , new NamedNode(NAMESPACES.XSD + "integer")));
        mdStore.addQuad(rdfDataset, new NamedNode(NAMESPACES.VOID + "distinctSubjects"),
                new Literal(Integer.toString(distinctSubjects.size())
                        , new NamedNode(NAMESPACES.XSD + "integer")));
        mdStore.addQuad(rdfDataset, new NamedNode(NAMESPACES.VOID + "distinctObjects"),
                new Literal(Integer.toString(distinctObjects.size())
                        , new NamedNode(NAMESPACES.XSD + "integer")));
        mdStore.addQuad(rdfDataset, new NamedNode(NAMESPACES.VOID + "classes"),
                new Literal(Integer.toString(distinctClasses.size())
                        , new NamedNode(NAMESPACES.XSD + "integer")));
        mdStore.addQuad(rdfDataset, new NamedNode(NAMESPACES.VOID + "properties"),
                new Literal(Integer.toString(distinctProperties.size())
                        , new NamedNode(NAMESPACES.XSD + "integer")));

        mdStore.addQuad(rdfDataset, new NamedNode(NAMESPACES.VOID + "documents"),
                new Literal("1"     // todo: change this when multiple output files are possible
                        , new NamedNode(NAMESPACES.XSD + "integer")));

        mdStore.addQuad(rdfDataset, new NamedNode(NAMESPACES.VOID + "feature"),
                new NamedNode("http://www.w3.org/ns/formats/N-Quads")); // todo: change this when output file format changes

    }

    /**
     * Creates a list of all source terms.
     *
     * @param triplesMaps list of triplemap terms
     * @param rmlStore mapping quadstore
     * @return list of logical sources of the triplesmaps
     */
    private List<Term> getLogicalSources(List<Term> triplesMaps, QuadStore rmlStore) {
        if (logicalSources == null) {
            logicalSources = new ArrayList<>();
            for (Term triplesMap : triplesMaps) {
                List<Term> logicalSourcesObjects = Utils.getObjectsFromQuads(rmlStore.getQuads(triplesMap,
                        new NamedNode(NAMESPACES.RML + "logicalSource"), null));

                if (logicalSourcesObjects.isEmpty()) {
                    throw new Error("No Logical Source is found for " + triplesMap + ". Exactly one Logical Source is required per Triples Map.");
                }

                Term logicalSource = logicalSourcesObjects.get(0);

                if (logicalSource instanceof BlankNode) {
                    List<Term> sourceObjects = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource,
                            new NamedNode(NAMESPACES.RML + "source"), null));

                    if (sourceObjects.isEmpty()) {
                        throw new Error("No Source is found for " + triplesMap + ". Exactly one Source is required per Logical Source.");
                    }

                    Term source = sourceObjects.get(0);
                    Term sourceNode;

                    // Literal -- encapsulate source in blank node
                    if (source instanceof Literal) {
//                        try {
//                            File sourceFile = Utils.getFile(sourceObjects.get(0).getValue(), null);
                        sourceNode = new NamedNode(String.format("file://%s", sourceObjects.get(0).getValue()));
//                        } catch (IOException ex) {
//                            ex.printStackTrace();
//                            throw new Error("Could not find source file: " + sourceObjects.get(0).getValue());
//                        }
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

    private void addTripleLevelFunctions() {
        // Add source triplesMap info
        generationFunctions.add((node, pquad) -> {
            // Get triplesMaps (Subject always has one, object does not always have one)
            Term subjectTM = pquad.getSubject().getMetadata().getTriplesMap();
            mdStore.addQuad(node, new NamedNode(NAMESPACES.PROV + "wasDerivedFrom"), subjectTM);

            if (pquad.getObject().getMetadata() != null && pquad.getObject().getMetadata().getTriplesMap() != null) {
                Term objectTM = pquad.getObject().getMetadata().getTriplesMap();
                mdStore.addQuad(node, new NamedNode(NAMESPACES.PROV + "wasDerivedFrom"), objectTM);
            }
        });
        // Add generation time info
        generationFunctions.add((node, pquad) -> {
            mdStore.addQuad(node, new NamedNode(NAMESPACES.PROV + "generatedAtTime"),
                    new Literal(Instant.now().toString(), new NamedNode(NAMESPACES.XSD + "dateTime")));
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

    private void addTermLevelFunctions() {
        generationFunctions.add((node, pquad) -> {
            Metadata subjectMD = pquad.getSubject().getMetadata();
            Metadata predicateMD = pquad.getPredicate().getMetadata();
            Metadata objectMD = pquad.getObject().getMetadata();

            // SUBJECT
            mdStore.addQuad(pquad.getSubject().getTerm(), new NamedNode(NAMESPACES.PROV + "wasDerivedFrom"),
                    subjectMD.getTriplesMap());
            if (!(subjectMD.getSourceMap() instanceof BlankNode)) {
                mdStore.addQuad(pquad.getSubject().getTerm(), new NamedNode(NAMESPACES.PROV + "wasGeneratedBy"),
                        termMaptoActivityMap.get(subjectMD.getSourceMap()));
            }

            // PREDICATE
            if (!(predicateMD.getSourceMap() instanceof BlankNode)) {
                createValueAndGeneratedByStatements(pquad.getPredicate().getTerm(), termMaptoActivityMap.get(predicateMD.getSourceMap()));
            }

            // OBJECT
            Term objectNode = (pquad.getObject().getTerm() instanceof Literal) ?
                    createValueStatements(pquad.getObject().getTerm()) : pquad.getObject().getTerm();

            if (objectMD.getTriplesMap() != null) {
                mdStore.addQuad(objectNode, new NamedNode(NAMESPACES.PROV + "wasDerivedFrom"),
                        objectMD.getTriplesMap());
            } else {
                mdStore.addQuad(objectNode, new NamedNode(NAMESPACES.PROV + "wasDerivedFrom"),
                        subjectMD.getTriplesMap());
            }

            if (!(objectMD.getSourceMap() instanceof BlankNode)) {
                createValueAndGeneratedByStatements(pquad.getObject().getTerm(), termMaptoActivityMap.get(objectMD.getSourceMap()));
            }
        });
    }

    private Term createValueStatements(Term value) {
        Term node = new BlankNode();
        mdStore.addQuad(node, new NamedNode(NAMESPACES.RDF + "type"), new NamedNode(NAMESPACES.PROV + "Entity"));
        mdStore.addQuad(node, new NamedNode(NAMESPACES.RDF + "value"), value);
        return node;
    }

    private Term createValueAndGeneratedByStatements(Term value, Term generatedBy) {
        Term node = createValueStatements(value);
        mdStore.addQuad(node, new NamedNode(NAMESPACES.PROV + "wasGeneratedBy"), generatedBy);
        return node;
    }

    public DETAIL_LEVEL getDetailLevel() {
        return detailLevel;
    }

    public QuadStore getResult() {
        return mdStore;
    }
}

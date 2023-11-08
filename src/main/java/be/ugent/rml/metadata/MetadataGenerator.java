package be.ugent.rml.metadata;

import be.ugent.rml.NAMESPACES;
import be.ugent.rml.Utils;
import be.ugent.rml.store.*;
import be.ugent.rml.term.*;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.time.Instant;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * Class that encapsulates the generation of metadata.
 * (Does everything for metadata generation)
 */
public class MetadataGenerator {

    private static final ValueFactory valueFactory = SimpleValueFactory.getInstance();


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
    private List<Value> triplesMaps;
    private List<BiConsumer<Value, ProvenancedQuad>> generationFunctions;    // Will contain different functions according to requested metadata detail level
    private List<Value> logicalSources;
    private Set<String> distinctSubjects;    // Used for counting number of distinct subjects
    private Set<String> distinctObjects;     // Used for counting number of distinct objects
    private Set<String> distinctClasses;     // Used for counting number of distinct classes
    private Set<String> distinctProperties;  // Used for counting number of distinct properties

    private Map<Value, Value> triplesMaptoActivityMap;
    private Map<Value, Value> termMaptoActivityMap;

    private Value rdfDataset;
    private Value rdfDatasetGeneration;
    private Value rmlMapper;

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

        rdfDataset = valueFactory.createIRI(String.format("file://%s", outputFile));
        rdfDatasetGeneration = valueFactory.createBNode(Utils.hashCode(outputFile));

        rmlMapper = valueFactory.createIRI("http://rml.io/tool/rmlmapper-java");


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
            Value node = valueFactory.createBNode(Utils.hashCode(provenancedQuad.getSubject().getTerm().stringValue() +
                    provenancedQuad.getPredicate().getTerm().stringValue() +
                    provenancedQuad.getObject().getTerm().stringValue()));

            mdStore.addQuad(node, valueFactory.createIRI(NAMESPACES.RDF + "type"), valueFactory.createIRI(NAMESPACES.RDF + "Statement"));
            mdStore.addQuad(node, valueFactory.createIRI(NAMESPACES.RDF + "subject"), provenancedQuad.getSubject().getTerm());
            mdStore.addQuad(node, valueFactory.createIRI(NAMESPACES.RDF + "predicate"), provenancedQuad.getPredicate().getTerm());
            mdStore.addQuad(node, valueFactory.createIRI(NAMESPACES.RDF + "object"), provenancedQuad.getObject().getTerm());

            for (BiConsumer<Value, ProvenancedQuad> function : generationFunctions) {
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
    public void preMappingGeneration(List<Value> triplesMaps, QuadStore mappingQuads) {
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
        for (Value triplesMap : triplesMaps) {
            mdStore.addQuad(triplesMap, valueFactory.createIRI(NAMESPACES.RDF + "type"), valueFactory.createIRI(NAMESPACES.PROV + "Entity"));
            mdStore.addQuad(triplesMap, valueFactory.createIRI(NAMESPACES.RDF + "type"), valueFactory.createIRI(NAMESPACES.VOID + "Dataset"));
            mdStore.addQuad(triplesMap, valueFactory.createIRI(NAMESPACES.VOID + "dataDump"), rdfDataset);
            createActivityStatements(triplesMap, triplesMaptoActivityMap);
        }
    }

    private void generatePreTermLevelDetailMetadata(QuadStore mappingQuads) {
        termMaptoActivityMap = new HashMap<>();

        for (Value triplesMap : triplesMaps) {
            List<Value> subjectMaps = Utils.getObjectsFromQuads(mappingQuads.getQuads(triplesMap, valueFactory.createIRI(NAMESPACES.RR + "subjectMap"),
                    null));

            if (!subjectMaps.isEmpty()) {
                Value subjectMap = subjectMaps.get(0);
                createActivityStatementsWithResultActivity(subjectMap, termMaptoActivityMap, triplesMaptoActivityMap.get(triplesMap));
            }

            List<Value> predicateObjectMaps = Utils.getObjectsFromQuads(mappingQuads.getQuads(triplesMap, valueFactory.createIRI(NAMESPACES.RR + "predicateObjectMap"),
                    null));

            for (Value pom : predicateObjectMaps) {
                Value pomActivity = createActivityStatementsWithResultActivity(pom, termMaptoActivityMap, triplesMaptoActivityMap.get(triplesMap));

                List<Value> predicateMaps = Utils.getObjectsFromQuads(mappingQuads.getQuads(pom, valueFactory.createIRI(NAMESPACES.RR + "predicateMap"),
                        null));
                List<Value> objectMaps = Utils.getObjectsFromQuads(mappingQuads.getQuads(pom, valueFactory.createIRI(NAMESPACES.RR + "objectMap"),
                        null));

                createActivityStatementsWithResultActivity(predicateMaps, termMaptoActivityMap, pomActivity);
                createActivityStatementsWithResultActivity(objectMaps, termMaptoActivityMap, pomActivity);
            }
        }
    }

    private Value createActivityStatements(Value termMap, Map<Value, Value> map) {
        Value termMapActivity;
        if (termMap.isBNode()) {
            termMapActivity = valueFactory.createBNode(termMap.stringValue() + "Activity");
        } else {
            termMapActivity = valueFactory.createIRI(termMap.stringValue() + "Activity");
        }
        if (map != null) {
            map.put(termMap, termMapActivity);
        }
        mdStore.addQuad(termMapActivity, valueFactory.createIRI(NAMESPACES.RDF + "type"), valueFactory.createIRI(NAMESPACES.PROV + "Activity"));
        mdStore.addQuad(termMapActivity, valueFactory.createIRI(NAMESPACES.PROV + "used"), termMap);
        return termMapActivity;
    }

    private Value createActivityStatementsWithResultActivity(Value termMap, Map<Value, Value> map, Value resultActivity) {
        Value termMapActivity = createActivityStatements(termMap, map);
        mdStore.addQuad(resultActivity, valueFactory.createIRI(NAMESPACES.PROV + "wasInformedBy"),
                termMapActivity);
        return termMapActivity;
    }

    private Value createActivityStatementsWithResultActivity(List<Value> termMaps, Map<Value, Value> map, Value resultActivity) {
        if (!termMaps.isEmpty()) {
            return createActivityStatementsWithResultActivity(termMaps.get(0), map, resultActivity);
        }
        return null;
    }

    private void generatePostTripleLevelDetailMetadata(QuadStore result) {
        // Describe result
        mdStore.addQuad(rdfDataset, valueFactory.createIRI(NAMESPACES.RDF + "type"), valueFactory.createIRI(NAMESPACES.PROV + "Entity"));
        mdStore.addQuad(rdfDataset, valueFactory.createIRI(NAMESPACES.RDF + "type"), valueFactory.createIRI(NAMESPACES.VOID + "Dataset"));

        getLogicalSources(triplesMaps, inputData).forEach(inputSource -> {
            mdStore.addQuad(rdfDataset, valueFactory.createIRI(NAMESPACES.PROV + "wasDerivedFrom"), inputSource);
        });

        mdStore.addQuad(rdfDataset, valueFactory.createIRI(NAMESPACES.PROV + "wasGeneratedBy"), rdfDatasetGeneration);

        mdStore.addQuad(rdfDataset, valueFactory.createIRI(NAMESPACES.VOID + "triples"),
                valueFactory.createLiteral(Integer.toString(result.getQuads(null, null, null, null).size())
                        , valueFactory.createIRI(NAMESPACES.XSD + "integer")));
        mdStore.addQuad(rdfDataset, valueFactory.createIRI(NAMESPACES.VOID + "distinctSubjects"),
                valueFactory.createLiteral(Integer.toString(distinctSubjects.size())
                        , valueFactory.createIRI(NAMESPACES.XSD + "integer")));
        mdStore.addQuad(rdfDataset, valueFactory.createIRI(NAMESPACES.VOID + "distinctObjects"),
                valueFactory.createLiteral(Integer.toString(distinctObjects.size())
                        , valueFactory.createIRI(NAMESPACES.XSD + "integer")));
        mdStore.addQuad(rdfDataset, valueFactory.createIRI(NAMESPACES.VOID + "classes"),
                valueFactory.createLiteral(Integer.toString(distinctClasses.size())
                        , valueFactory.createIRI(NAMESPACES.XSD + "integer")));
        mdStore.addQuad(rdfDataset, valueFactory.createIRI(NAMESPACES.VOID + "properties"),
                valueFactory.createLiteral(Integer.toString(distinctProperties.size())
                        , valueFactory.createIRI(NAMESPACES.XSD + "integer")));

        mdStore.addQuad(rdfDataset, valueFactory.createIRI(NAMESPACES.VOID + "documents"),
                valueFactory.createLiteral("1"     // todo: change this when multiple output files are possible
                        , valueFactory.createIRI(NAMESPACES.XSD + "integer")));

        mdStore.addQuad(rdfDataset, valueFactory.createIRI(NAMESPACES.VOID + "feature"),
                valueFactory.createIRI("http://www.w3.org/ns/formats/N-Quads")); // todo: change this when output file format changes

    }

    /**
     * Creates a list of all source terms.
     *
     * @param triplesMaps list of triplemap terms
     * @param rmlStore mapping quadstore
     * @return list of logical sources of the triplesmaps
     */
    private List<Value> getLogicalSources(List<Value> triplesMaps, QuadStore rmlStore) {
        if (logicalSources == null) {
            logicalSources = new ArrayList<>();
            for (Value triplesMap : triplesMaps) {
                List<Value> logicalSourcesObjects = Utils.getObjectsFromQuads(rmlStore.getQuads(triplesMap,
                        valueFactory.createIRI(NAMESPACES.RML + "logicalSource"), null));

                if (logicalSourcesObjects.isEmpty()) {
                    throw new Error("No Logical Source is found for " + triplesMap + ". Exactly one Logical Source is required per Triples Map.");
                }

                Value logicalSource = logicalSourcesObjects.get(0);

                if (logicalSource.isBNode()) {
                    List<Value> sourceObjects = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource,
                            valueFactory.createIRI(NAMESPACES.RML + "source"), null));

                    if (sourceObjects.isEmpty()) {
                        throw new Error("No Source is found for " + triplesMap + ". Exactly one Source is required per Logical Source.");
                    }

                    Value source = sourceObjects.get(0);
                    Value sourceNode;

                    // Literal -- encapsulate source in blank node
                    if (source.isLiteral()) {
//                        try {
//                            File sourceFile = Utils.getFile(sourceObjects.get(0).getValue(), null);
                        sourceNode = valueFactory.createIRI(String.format("file://%s", sourceObjects.get(0).stringValue()));
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
            Value subjectTM = pquad.getSubject().getMetadata().getTriplesMap();
            mdStore.addQuad(node, valueFactory.createIRI(NAMESPACES.PROV + "wasDerivedFrom"), subjectTM);

            if (pquad.getObject().getMetadata() != null && pquad.getObject().getMetadata().getTriplesMap() != null) {
                Value objectTM = pquad.getObject().getMetadata().getTriplesMap();
                mdStore.addQuad(node, valueFactory.createIRI(NAMESPACES.PROV + "wasDerivedFrom"), objectTM);
            }
        });
        // Add generation time info
        generationFunctions.add((node, pquad) -> {
            mdStore.addQuad(node, valueFactory.createIRI(NAMESPACES.PROV + "generatedAtTime"),
                    valueFactory.createLiteral(Instant.now().toString(), valueFactory.createIRI(NAMESPACES.XSD + "dateTime")));
        });
        // Add counters
        generationFunctions.add((node, pquad) -> {
            distinctSubjects.add(pquad.getSubject().getTerm().stringValue());
            distinctObjects.add(pquad.getObject().getTerm().stringValue());
            distinctProperties.add(pquad.getPredicate().getTerm().stringValue());
            if (pquad.getPredicate().getTerm().stringValue().equals(NAMESPACES.RDF + "type")) {
                distinctClasses.add(pquad.getObject().getTerm().stringValue());
            }
        });
    }

    private void addTermLevelFunctions() {
        generationFunctions.add((node, pquad) -> {
            Metadata subjectMD = pquad.getSubject().getMetadata();
            Metadata predicateMD = pquad.getPredicate().getMetadata();
            Metadata objectMD = pquad.getObject().getMetadata();

            // SUBJECT
            mdStore.addQuad(pquad.getSubject().getTerm(), valueFactory.createIRI(NAMESPACES.PROV + "wasDerivedFrom"),
                    subjectMD.getTriplesMap());
            if (!(subjectMD.getSourceMap().isBNode())) {
                mdStore.addQuad(pquad.getSubject().getTerm(), valueFactory.createIRI(NAMESPACES.PROV + "wasGeneratedBy"),
                        termMaptoActivityMap.get(subjectMD.getSourceMap()));
            }

            // PREDICATE
            if (!(predicateMD.getSourceMap().isBNode())) {
                createValueAndGeneratedByStatements(pquad.getPredicate().getTerm(), termMaptoActivityMap.get(predicateMD.getSourceMap()));
            }

            // OBJECT
            Value objectNode = (pquad.getObject().getTerm().isLiteral()) ?
                    createValueStatements(pquad.getObject().getTerm()) : pquad.getObject().getTerm();

            if (objectMD.getTriplesMap() != null) {
                mdStore.addQuad(objectNode, valueFactory.createIRI(NAMESPACES.PROV + "wasDerivedFrom"),
                        objectMD.getTriplesMap());
            } else {
                mdStore.addQuad(objectNode, valueFactory.createIRI(NAMESPACES.PROV + "wasDerivedFrom"),
                        subjectMD.getTriplesMap());
            }

            if (!(objectMD.getSourceMap().isBNode())) {
                createValueAndGeneratedByStatements(pquad.getObject().getTerm(), termMaptoActivityMap.get(objectMD.getSourceMap()));
            }
        });
    }

    private Value createValueStatements(Value value) {
        Value node = valueFactory.createBNode();
        mdStore.addQuad(node, valueFactory.createIRI(NAMESPACES.RDF + "type"), valueFactory.createIRI(NAMESPACES.PROV + "Entity"));
        mdStore.addQuad(node, valueFactory.createIRI(NAMESPACES.RDF + "value"), value);
        return node;
    }

    private Value createValueAndGeneratedByStatements(Value value, Value generatedBy) {
        Value node = createValueStatements(value);
        mdStore.addQuad(node, valueFactory.createIRI(NAMESPACES.PROV + "wasGeneratedBy"), generatedBy);
        return node;
    }

    public DETAIL_LEVEL getDetailLevel() {
        return detailLevel;
    }

    public QuadStore getResult() {
        return mdStore;
    }
}

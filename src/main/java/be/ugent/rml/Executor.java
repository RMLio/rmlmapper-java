package be.ugent.rml;

import be.ugent.idlab.knows.dataio.access.LocalFileAccess;
import be.ugent.idlab.knows.dataio.access.RemoteFileAccess;
import be.ugent.idlab.knows.dataio.record.Record;
import be.ugent.idlab.knows.functions.agent.Agent;
import be.ugent.knows.idlabFunctions.IDLabFunctions;
import be.ugent.rml.conformer.MappingConformer;
import be.ugent.rml.functions.MultipleRecordsFunctionExecutor;
import be.ugent.rml.metadata.Metadata;
import be.ugent.rml.metadata.MetadataGenerator;
import be.ugent.rml.records.MarkerRecord;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.Quad;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.RDF4JStore;
import be.ugent.rml.term.*;
import be.ugent.rml.termgenerator.TermGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.util.*;

public class Executor {

    private static final Logger logger = LoggerFactory.getLogger(Executor.class);

    private final Initializer initializer;
    private final MappingOptimizer mappingOptimizer;
    private final Map<Term, List<Record>> recordsHolders = new HashMap<>();

    /*
     * this map stores for every Triples Map, which is a Term,
     * a map with the record index and the record's corresponding subject, which is a ProvenancedTerm.
     */
    private final Map<Term, Map<Integer, List<ProvenancedTerm>>> subjectCache;
    private final QuadStore resultingQuads;
    private final QuadStore rmlStore;
    private final Map<Term, QuadStore> targetStores;
    private final RecordsFactory recordsFactory;
    private static int blankNodeCounter;
    private final Map<Term, Mapping> mappings;

    /**
     * Indicates whether the data to process contains an End-of-File (EOF) marker.
     * If it is not provided, it will be automatically inserted when necessary.
     * Don't change unless you're generating LDES and know what you're doing!
     */
    private boolean EOFProvidedInData = false;

    public Executor(QuadStore rmlStore, RecordsFactory recordsFactory, String baseIRI, StrictMode strictMode, final Agent functionAgent) throws Exception {
        this(rmlStore, recordsFactory, null, baseIRI, strictMode, functionAgent);
    }

    /**
     * Defaults to best effort operation. For strict mode,
     * use {@link Executor#Executor(QuadStore, RecordsFactory, QuadStore, String, StrictMode, Agent)}
     */
    public Executor(QuadStore rmlStore, RecordsFactory recordsFactory, QuadStore resultingQuads, String baseIRI, final Agent functionAgent) throws Exception {
        this(rmlStore, recordsFactory, resultingQuads, baseIRI, StrictMode.BEST_EFFORT, functionAgent);
    }

    /**
     * Call this if the data to process contains a specific End-of-File (EOF) marker.
     * If it is not provided, it will be automatically inserted when necessary.
     * Don't change unless you're generating LDES and know what you're doing!
     */
    public void setEOFProvidedInData() {
        this.EOFProvidedInData = true;
    }

    public Executor(QuadStore rmlStore, RecordsFactory recordsFactory, QuadStore resultingQuads, String baseIRI, StrictMode strictMode, final Agent functionAgent) throws Exception {
        this(rmlStore, recordsFactory, resultingQuads, baseIRI, strictMode, functionAgent, null);
    }

    public Executor(QuadStore rmlStore, RecordsFactory recordsFactory, QuadStore resultingQuads, String baseIRI, StrictMode strictMode, final Agent functionAgent, Map<String, String> mappingOptions) throws Exception {
        // Convert mapping file to RML if needed.
        MappingConformer conformer = new MappingConformer(rmlStore, mappingOptions);

        try {
            boolean conversionNeeded = conformer.conform();

            if (conversionNeeded) {
                logger.info("Conversion to RML was needed.");
            }
        } catch (Exception e) {
            logger.error("Failed to make mapping file conformant to RML spec.", e);
        }

        this.mappingOptimizer = new MappingOptimizer(rmlStore);
        this.rmlStore = mappingOptimizer.optimizeMapping();
        this.initializer = new Initializer(this.rmlStore, functionAgent, baseIRI, strictMode);
        this.mappings = this.initializer.getMappings();
        this.recordsFactory = recordsFactory;
        this.subjectCache = new HashMap<>();
        this.targetStores = new HashMap<>();
        Executor.blankNodeCounter = 0;

        // Default store if no Targets are available for a triple
        this.resultingQuads = Objects.requireNonNullElseGet(resultingQuads, RDF4JStore::new);

        // Output stores for Targets in Term Maps
        for (Map.Entry<Term, Mapping> tm: this.mappings.entrySet()) {
            Mapping mapping = tm.getValue();

            // Subject Map
            MappingInfo subjectMapInfo = mapping.getSubjectMappingInfo();
            Set<Term> targets = new HashSet<>(subjectMapInfo.getTargets());

            // Predicate, Object and Language Maps
            for(PredicateObjectGraphMapping pog: mapping.getPredicateObjectGraphMappings()) {
                if(pog.getPredicateMappingInfo() != null) {
                    targets.addAll(pog.getPredicateMappingInfo().getTargets());
                }
                if(pog.getObjectMappingInfo() != null) {
                    targets.addAll(pog.getObjectMappingInfo().getTargets());
                }
            }

            // Graph Map
            for(MappingInfo g: mapping.getGraphMappingInfos()) {
                targets.addAll(g.getTargets());
            }

            // Create stores
            for (Term t: targets) {
                logger.debug("Adding target for {}", t);
                this.targetStores.put(t, new RDF4JStore());
            }
        }
    }

    /*
     * New public API for the V5.X.X. releases
     */
    public Map<Term, QuadStore> execute(List<Term> triplesMaps, boolean removeDuplicates, MetadataGenerator metadataGenerator) throws Exception {

        POGFunction pogFunction;

        if (metadataGenerator != null && metadataGenerator.getDetailLevel().getLevel() >= MetadataGenerator.DETAIL_LEVEL.TRIPLE.getLevel()) {
            pogFunction = (subject, predicate, object, graph, checkEOFMarker) -> {
                if (generateQuad(subject, predicate, object, graph , checkEOFMarker)) {
                    metadataGenerator.insertQuad(new ProvenancedQuad(subject, predicate, object, graph));
                }
            };
        } else {
            pogFunction = this::generateQuad;
        }

        return executeWithFunction(triplesMaps, removeDuplicates, pogFunction);
    }

    public Map<Term, QuadStore> executeWithFunction(List<Term> triplesMaps, boolean removeDuplicates, POGFunction pogFunction) throws Exception {
        //check if TriplesMaps are provided
        if (triplesMaps == null || triplesMaps.isEmpty()) {
            triplesMaps = this.getTriplesMaps();
        }

        //we execute every mapping
        for (Term triplesMap : triplesMaps) {
            Mapping mapping = this.mappings.get(triplesMap);

            List<Record> records = this.getRecords(triplesMap);

            for (int j = 0; j < records.size(); j++) {
                Record record = records.get(j);
                List<ProvenancedTerm> subjects = getSubject(triplesMap, mapping, record, j);

                if (subjects != null) {
                    generatePredicateObjectsForSubjects(subjects, mapping, record, pogFunction, EOFProvidedInData);
                }
            }

            if (!EOFProvidedInData) {
                // Generate an EOF marker to indicate the end of the data source and run mappings once more (if not provided).
                // This is a hack to call implicitDelete a final time, where it then returns the list of deleted records
                TermGenerator generator = mapping.getSubjectMappingInfo().getTermGenerator();
                boolean needsEOFMarker = generator.needsEOFMarker();
                if (needsEOFMarker) {
                    Record record = new MarkerRecord();
                    List<ProvenancedTerm> subjects = new ArrayList<>();
                    List<Term> nodes = generator.generate(record);

                    if (!nodes.isEmpty()) {
                        List<Term> targets = mapping.getSubjectMappingInfo().getTargets();
                        for (Term node : nodes) {
                            subjects.add(new ProvenancedTerm(node, null, targets));
                        }
                    }
                    // TODO this only works for the constants in the triples map!
                    // TODO `record` is not really used, we only need to generate a subject here
                    generatePredicateObjectsForSubjects(subjects, mapping, null, pogFunction, true);
                }
            }
        }

        if (removeDuplicates) {
            this.resultingQuads.removeDuplicates();
        }

        // Add the legacy store to the list of targets as well
        this.targetStores.put(new NamedNode("rmlmapper://default.store"), this.resultingQuads);
        return this.targetStores;
    }

    public Map<Term, QuadStore> execute(List<Term> triplesMaps) throws Exception {
        return this.execute(triplesMaps, false, null);
    }

    private boolean generateQuad(ProvenancedTerm subject, ProvenancedTerm predicate, ProvenancedTerm object, ProvenancedTerm graph, boolean checkEOFMarker) {
        Term g = null;
        Set<Term> targets = new HashSet<>();

        if (subject != null && predicate != null && object != null) {
            if (graph != null) {
                g = graph.getTerm();
                targets.addAll(graph.getTargets());
            }

            if (checkEOFMarker) {
                if (subject.getTerm().getValue().contains(IDLabFunctions.MAGIC_MARKER)
                        || subject.getTerm().getValue().contains(IDLabFunctions.MAGIC_MARKER_ENCODED)
                        || predicate.getTerm().getValue().contains(IDLabFunctions.MAGIC_MARKER)
                        || predicate.getTerm().getValue().contains(IDLabFunctions.MAGIC_MARKER_ENCODED)
                        || object.getTerm().getValue().contains(IDLabFunctions.MAGIC_MARKER)
                        || object.getTerm().getValue().contains(IDLabFunctions.MAGIC_MARKER_ENCODED))
                    return false;

                if (g != null && (g.getValue().contains(IDLabFunctions.MAGIC_MARKER) || g.getValue().contains(IDLabFunctions.MAGIC_MARKER_ENCODED)))
                    return false;
            }

            // Get all possible targets for triple, the Set guarantees that we don't have duplicates
            targets.addAll(subject.getTargets());
            targets.addAll(predicate.getTargets());
            targets.addAll(object.getTargets());

            // If we have targets, write to them
            if (!targets.isEmpty()) {
                for (Term t: targets) {
                    this.targetStores.get(t).addQuad(subject.getTerm(), predicate.getTerm(), object.getTerm(), g);
                }
            }
            // If not, use the default processor target
            else {
                this.resultingQuads.addQuad(subject.getTerm(), predicate.getTerm(), object.getTerm(), g);
            }

            return true;
        }

        return false;
    }

    private List<ProvenancedTerm> getIRIsWithConditions(Record record, Term triplesMap, List<MultipleRecordsFunctionExecutor> conditions) throws Exception {
        List<ProvenancedTerm> goodIRIs = new ArrayList<>();
        List<List<ProvenancedTerm>> allIRIs = new ArrayList<>();

        for (MultipleRecordsFunctionExecutor condition : conditions) {
            allIRIs.add(this.getIRIsWithTrueCondition(record, triplesMap, condition));
        }

        if (!allIRIs.isEmpty()) {
            goodIRIs.addAll(allIRIs.get(0));

            for(int i = 1; i < allIRIs.size(); i ++) {
                List<ProvenancedTerm> list = allIRIs.get(i);

                for (int j = 0; j < goodIRIs.size(); j ++) {
                    if (!list.contains(goodIRIs.get(j))) {
                        goodIRIs.remove(j);
                        j --;
                    }
                }
            }
        }

        return goodIRIs;
    }

    private List<ProvenancedTerm> getIRIsWithTrueCondition(Record child, Term triplesMap, MultipleRecordsFunctionExecutor condition) throws Exception {
        Mapping mapping = this.mappings.get(triplesMap);

        //iterator over all the records corresponding with @triplesMap
        List<Record> records = this.getRecords(triplesMap);
        //this array contains all the IRIs that are valid regarding @path and @values
        List<ProvenancedTerm> iris = new ArrayList<>();

        for (int i = 0; i < records.size(); i++) {
            Record parent = records.get(i);

            Map<String, Record> recordsMap = new HashMap<>();
            recordsMap.put("child", child);
            recordsMap.put("parent", parent);

            Object expectedBoolean = condition.execute(recordsMap);

            if (Boolean.TRUE.equals(expectedBoolean)) {
                List<ProvenancedTerm> subjects = this.getSubject(triplesMap, mapping, parent, i);
                if (subjects != null)
                    iris.addAll(subjects);
            } else {
                logger.warn("The used condition with the Parent Triples Map does not return a boolean.");
            }
        }

        return iris;
    }

    private List<ProvenancedTerm> getSubject(Term triplesMap, Mapping mapping, Record record, int i) throws Exception {
        if (!this.subjectCache.containsKey(triplesMap)) {
            this.subjectCache.put(triplesMap, new HashMap<>());
        }

        if (!this.subjectCache.get(triplesMap).containsKey(i)) {
            TermGenerator generator = mapping.getSubjectMappingInfo().getTermGenerator();
            List<Term> nodes = generator.generate(record);

            if (!nodes.isEmpty()) {
                List<Term> targets = mapping.getSubjectMappingInfo().getTargets();
                List<ProvenancedTerm> terms = new ArrayList<>();
                Metadata meta = new Metadata(triplesMap, mapping.getSubjectMappingInfo().getTerm());

                // TODO: only create metadata when it's required
                for (Term node : nodes) {
                    terms.add(new ProvenancedTerm(node, meta, targets));
                }
                this.subjectCache.get(triplesMap).put(i, terms);
                return terms;
            }
        }

        return this.subjectCache.get(triplesMap).get(i);
    }

    private List<ProvenancedTerm> getAllIRIs(Term triplesMap) throws Exception {
        Mapping mapping = this.mappings.get(triplesMap);

        List<Record> records = getRecords(triplesMap);
        List<ProvenancedTerm> iris = new ArrayList<>();

        for (int i = 0; i < records.size(); i++) {
            Record record = records.get(i);
            List<ProvenancedTerm> subjects = getSubject(triplesMap, mapping, record, i);
            if (subjects != null)
                iris.addAll(subjects);
        }

        return iris;
    }

    private List<Record> getRecords(Term triplesMap) throws Exception {
        if (!this.recordsHolders.containsKey(triplesMap)) {
            this.recordsHolders.put(triplesMap, this.recordsFactory.createRecords(triplesMap, this.rmlStore));
        }

        return this.recordsHolders.get(triplesMap);
    }

    private List<PredicateObjectGraph> combineMultiplePOGs(List<ProvenancedTerm> predicates, List<ProvenancedTerm> objects, List<ProvenancedTerm> graphs) {
        List<PredicateObjectGraph> results = new ArrayList<>();

        if (graphs.isEmpty()) {
            graphs.add(null);
        }

        predicates.forEach(
                p -> objects.forEach(
                        o -> graphs.forEach(
                                g -> results.add(new PredicateObjectGraph(p, o, g))
                        )
                )
        );

        return results;
    }

    public static String getNewBlankNodeID() {
        String temp = "" + Executor.blankNodeCounter;
        Executor.blankNodeCounter++;

        return temp;
    }

    public List<Term> getTriplesMaps() {
        List<Quad> withSubjectMaps = rmlStore.getQuads(null, new NamedNode(NAMESPACES.RML2 + "subjectMap"), null);

        return withSubjectMaps.stream()
                .map(Quad::getSubject)
                .filter(subject -> rmlStore.contains(subject, new NamedNode(NAMESPACES.RML2 + "logicalSource"), null)).toList();
    }

    public QuadStore getRMLStore() {
        return this.rmlStore;
    }

    public Map<Term, QuadStore> getTargets(){
        if (this.targetStores.isEmpty()){
            return null;
        }
        return this.targetStores;
    }

    
    public void verifySources(String basepath, String mappingPath) throws Exception {
        for (Term triplesMap : this.getTriplesMaps()) {
            List<Term> logicalSources = Utils.getObjectsFromQuads(rmlStore.getQuads(triplesMap, new NamedNode(NAMESPACES.RML2 + "logicalSource"), null));
            Term logicalSource = logicalSources.get(0);
            List<Term> sources = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML2 + "source"), null));
            for (Term source : sources) {
                String value = source.getValue();
                if (source instanceof Literal) {
                    InputStream is;
                    if (Utils.isRemoteFile(value)) {
                        is = new RemoteFileAccess(value).getInputStream();
                    } else {
                        try {
                            is = new LocalFileAccess(value, basepath, ((Literal) source).getDatatype().stringValue()).getInputStream();
                        } catch (NoSuchFileException e) {
                            is = new LocalFileAccess(value, mappingPath, ((Literal) source).getDatatype().stringValue()).getInputStream();
                        }
                    }
                    is.close(); // close resources.
                }
            }
        }
    }

    private void generatePredicateObjectsForSubjects(final List<ProvenancedTerm> subjects,
                                                     final Mapping mapping,
                                                     final Record record,
                                                     final POGFunction pogFunction,
                                                     final boolean checkEOFMarker) throws Exception {
        for (ProvenancedTerm subject: subjects) {
            //TODO validate subject or check if blank node
            if (subject != null) {
                List<ProvenancedTerm> subjectGraphs = new ArrayList<>();

                mapping.getGraphMappingInfos().forEach(mappingInfo -> {
                    List<Term> terms = null;

                    try {
                        terms = mappingInfo.getTermGenerator().generate(record);
                    } catch (Exception e) {
                        //todo be more nice and gentle
                        logger.error("Could not generate graph term for record {}", record, e);
                    }

                    if (terms != null) {
                        terms.forEach(term -> {
                            if (!term.equals(new NamedNode(NAMESPACES.RML2 + "defaultGraph"))) {
                                subjectGraphs.add(new ProvenancedTerm(term));
                            }
                        });
                    }
                });

                List<PredicateObjectGraph> pogs = new ArrayList<>();
                List<PredicateObjectGraphMapping> predicateObjectGraphMappings = mapping.getPredicateObjectGraphMappings();

                for (PredicateObjectGraphMapping pogMapping : predicateObjectGraphMappings) {
                    ArrayList<ProvenancedTerm> predicates = new ArrayList<>();
                    MappingInfo pogGraphMappingInfo = pogMapping.getGraphMappingInfo();
                    MappingInfo pogPredicateMappingInfo = pogMapping.getPredicateMappingInfo();
                    MappingInfo pogObjectMappingInfo = pogMapping.getObjectMappingInfo();

                    ArrayList<ProvenancedTerm> poGraphs = new ArrayList<>(subjectGraphs);

                    if (pogGraphMappingInfo != null) {
                        TermGenerator pogGraphGenerator = pogGraphMappingInfo.getTermGenerator();
                        if (pogGraphGenerator != null) {
                            pogGraphGenerator.generate(record).forEach(term -> {
                                if (!term.equals(new NamedNode(NAMESPACES.RML2 + "defaultGraph"))) {
                                    poGraphs.add(new ProvenancedTerm(term));
                                }
                            });
                        }
                    }


                    /* Predicates */
                    if (pogPredicateMappingInfo != null) {
                        TermGenerator pogPredicateGenerator = pogPredicateMappingInfo.getTermGenerator();
                        pogPredicateGenerator.generate(record).forEach(p -> predicates.add(new ProvenancedTerm(p, pogPredicateMappingInfo)));
                    }

                    /* Objects */
                    if (pogObjectMappingInfo != null) {
                        TermGenerator pogObjectGenerator = pogObjectMappingInfo.getTermGenerator();
                        if (pogObjectGenerator != null) {
                            List<Term> objects = pogObjectGenerator.generate(record);
                            List<ProvenancedTerm> provenancedObjects = new ArrayList<>();

                            objects.forEach(object -> provenancedObjects.add(new ProvenancedTerm(object, pogObjectMappingInfo)));

                            if (!objects.isEmpty()) {
                                //add pogs
                                pogs.addAll(combineMultiplePOGs(predicates, provenancedObjects, poGraphs));
                            }
                        }

                    //check if we are dealing with a parentTriplesMap (RefObjMap)
                    } else if (pogMapping.getParentTriplesMap() != null) {
                        List<ProvenancedTerm> objects;

                        //check if need to apply a join condition
                        if (!pogMapping.getJoinConditions().isEmpty()) {
                            logger.debug("mapping {}'s join conditions are not empty", pogMapping.toString());
                            objects = this.getIRIsWithConditions(record, pogMapping.getParentTriplesMap(), pogMapping.getJoinConditions());
                            //this.generateTriples(subject, po.getPredicateGenerator(), objects, record, combinedGraphs);
                        } else {
                            logger.debug("mapping {}'s join conditions are empty", pogMapping.toString());
                            objects = this.getAllIRIs(pogMapping.getParentTriplesMap());
                        }

                        pogs.addAll(combineMultiplePOGs(predicates, objects, poGraphs));
                    }
                }

                pogs.forEach(pog -> pogFunction.generateQuad(subject, pog.getPredicate(), pog.getObject(), pog.getGraph(), checkEOFMarker));
            }
        }
    }
}

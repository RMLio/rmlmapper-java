package be.ugent.rml;

import be.ugent.idlab.knows.dataio.access.LocalFileAccess;
import be.ugent.idlab.knows.dataio.access.RemoteFileAccess;
import be.ugent.idlab.knows.dataio.record.Record;
import be.ugent.idlab.knows.functions.agent.Agent;
import be.ugent.knows.idlabFunctions.IDLabFunctions;
import be.ugent.rml.functions.MultipleRecordsFunctionExecutor;
import be.ugent.rml.metadata.Metadata;
import be.ugent.rml.metadata.MetadataGenerator;
import be.ugent.rml.records.MarkerRecord;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.RDF4JStore;
import be.ugent.rml.term.*;
import be.ugent.rml.termgenerator.TermGenerator;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;
import java.util.function.BiConsumer;

public class Executor {

    private static final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private static final Logger logger = LoggerFactory.getLogger(Executor.class);

    private Initializer initializer;
    private HashMap<Value, List<Record>> recordsHolders = new HashMap<>();
    /*
     * this map stores for every Triples Map, which is a Term,
     * a map with the record index and the record's corresponding subject, which is a ProvenancedTerm.
     */
    private HashMap<Value, HashMap<Integer, List<ProvenancedTerm>>> subjectCache;
    private QuadStore resultingQuads;
    private QuadStore rmlStore;
    private HashMap<Value, QuadStore> targetStores;
    private RecordsFactory recordsFactory;
    private static int blankNodeCounter;
    private HashMap<Value, Mapping> mappings;

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

    public Executor(QuadStore rmlStore, RecordsFactory recordsFactory, QuadStore resultingQuads, String baseIRI, StrictMode strictMode, final Agent functionAgent) throws Exception {
        this.initializer = new Initializer(rmlStore, functionAgent, baseIRI, strictMode);
        this.mappings = this.initializer.getMappings();
        this.rmlStore = rmlStore;
        this.recordsFactory = recordsFactory;
        this.subjectCache = new HashMap<>();
        this.targetStores = new HashMap<>();
        Executor.blankNodeCounter = 0;

        // Default store if no Targets are available for a triple
        if (resultingQuads == null) {
            this.resultingQuads = new RDF4JStore();
        } else {
            this.resultingQuads = resultingQuads;
        }

        // Output stores for Targets in Term Maps
        for (Map.Entry<Value, Mapping> tm: this.mappings.entrySet()) {
            Mapping mapping = tm.getValue();
            Set<Value> targets = new HashSet<>();

            // Subject Map
            MappingInfo subjectMapInfo = mapping.getSubjectMappingInfo();
            targets.addAll(subjectMapInfo.getTargets());

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
            for (Value t: targets) {
                logger.debug("Adding target for {}", t);
                this.targetStores.put(t, new RDF4JStore());
            }
        }
    }

    public Executor(RDF4JStore rmlStore, RecordsFactory factory, QuadStore outputStore, final Agent functionAgent) throws Exception {
        this(rmlStore, factory, outputStore, rmlStore.getBase(), functionAgent);
    }

    /*
     * New public API for the V5.X.X. releases
     */
    public HashMap<Value, QuadStore> execute(List<Value> triplesMaps, boolean removeDuplicates, MetadataGenerator metadataGenerator) throws Exception {

        BiConsumer<ProvenancedTerm, PredicateObjectGraph> pogFunction;

        if (metadataGenerator != null && metadataGenerator.getDetailLevel().getLevel() >= MetadataGenerator.DETAIL_LEVEL.TRIPLE.getLevel()) {
            pogFunction = (subject, pog) -> {
                if (generateQuad(subject, pog.getPredicate(), pog.getObject(), pog.getGraph()))
                    metadataGenerator.insertQuad(new ProvenancedQuad(subject, pog.getPredicate(), pog.getObject(), pog.getGraph()));
            };
        } else {
            pogFunction = (subject, pog) -> {
                generateQuad(subject, pog.getPredicate(), pog.getObject(), pog.getGraph());
            };
        }

        return executeWithFunction(triplesMaps, removeDuplicates, pogFunction);
    }

    public HashMap<Value, QuadStore> executeWithFunction(List<Value> triplesMaps, boolean removeDuplicates, BiConsumer<ProvenancedTerm, PredicateObjectGraph> pogFunction) throws Exception {
        //check if TriplesMaps are provided
        if (triplesMaps == null || triplesMaps.isEmpty()) {
            triplesMaps = this.initializer.getTriplesMaps();
        }

        //we execute every mapping
        for (Value triplesMap : triplesMaps) {
            Mapping mapping = this.mappings.get(triplesMap);

            List<Record> records = this.getRecords(triplesMap);

            for (int j = 0; j < records.size(); j++) {
                Record record = records.get(j);
                List<ProvenancedTerm> subjects = getSubject(triplesMap, mapping, record, j);

                if (subjects == null)
                    continue;

                for (ProvenancedTerm subject: subjects) {
                    final ProvenancedTerm finalSubject = subject;

                    //TODO validate subject or check if blank node
                    if (subject != null) {
                        List<ProvenancedTerm> subjectGraphs = new ArrayList<>();

                        mapping.getGraphMappingInfos().forEach(mappingInfo -> {
                            List<Value> terms = null;

                            try {
                                terms = mappingInfo.getTermGenerator().generate(record);
                            } catch (Exception e) {
                                //todo be more nice and gentle
                                e.printStackTrace();
                            }

                            terms.forEach(term -> {
                                if (!term.equals(valueFactory.createIRI(NAMESPACES.RR + "defaultGraph"))) {
                                    subjectGraphs.add(new ProvenancedTerm(term));
                                }
                            });
                        });

                        List<PredicateObjectGraph> pogs = this.generatePredicateObjectGraphs(mapping, record, subjectGraphs);

                        pogs.forEach(pog -> pogFunction.accept(finalSubject, pog));
                    }
                }
            }
        }

        /* Magic Marker */
        for (Value triplesMap : triplesMaps) {
            Record record = new MarkerRecord();
            Mapping mapping = this.mappings.get(triplesMap);
            List<ProvenancedTerm> subjects = new ArrayList<>();
            TermGenerator generator = mapping.getSubjectMappingInfo().getTermGenerator();
            List<Value> nodes = null;

            /* Skip any subjects that are not applicable for the marker */
            if (!generator.magic())
                continue;

            nodes = generator.generate(record);

            if (!nodes.isEmpty()) {
                List<Value> targets = mapping.getSubjectMappingInfo().getTargets();

                for (int j=0; j < nodes.size(); j++) {
                    subjects.add(new ProvenancedTerm(nodes.get(j), null, targets));
                }
            }

            for (ProvenancedTerm subject : subjects) {
                final ProvenancedTerm finalSubject = subject;

                //TODO validate subject or check if blank node
                if (subject != null) {
                    List<ProvenancedTerm> subjectGraphs = new ArrayList<>();
                    mapping.getGraphMappingInfos().forEach(mappingInfo -> {
                        List<Value> terms = new ArrayList<>();

                        try {
                            TermGenerator generatorGraph = mappingInfo.getTermGenerator();
                            terms = generatorGraph.generate(record);
                        } catch (Exception e) {
                            //todo be more nice and gentle
                            e.printStackTrace();
                        }

                        terms.forEach(term -> {
                            if (!term.equals(valueFactory.createIRI(NAMESPACES.RR + "defaultGraph"))) {
                                subjectGraphs.add(new ProvenancedTerm(term));
                            }
                        });
                    });

                    List<PredicateObjectGraph> pogs = new ArrayList<>();
                    List<PredicateObjectGraphMapping> predicateObjectGraphMappings = mapping.getPredicateObjectGraphMappings();

                    for (PredicateObjectGraphMapping pogMapping : predicateObjectGraphMappings) {
                        ArrayList<ProvenancedTerm> predicates = new ArrayList<>();
                        ArrayList<ProvenancedTerm> poGraphs = new ArrayList<>();
                        MappingInfo pogGraphMappingInfo = pogMapping.getGraphMappingInfo();
                        MappingInfo pogPredicateMappingInfo = pogMapping.getPredicateMappingInfo();
                        MappingInfo pogObjectMappingInfo = pogMapping.getObjectMappingInfo();
                        TermGenerator pogGraphGenerator = null;
                        TermGenerator pogPredicateGenerator = null;
                        TermGenerator pogObjectGenerator = null;
                        poGraphs.addAll(subjectGraphs);

                        /* Named Graph */
                        if (pogGraphMappingInfo != null) {
                            pogGraphGenerator = pogGraphMappingInfo.getTermGenerator();
                        }

                        if (pogGraphGenerator != null) {
                            pogGraphGenerator.generate(record).forEach(term -> {
                                if (!term.equals(valueFactory.createIRI(NAMESPACES.RR + "defaultGraph"))) {
                                    poGraphs.add(new ProvenancedTerm(term));
                                }
                            });
                        }

                        /* Predicates */
                        if (pogPredicateMappingInfo != null) {
                            pogPredicateGenerator = pogPredicateMappingInfo.getTermGenerator();
                        }

                        pogPredicateGenerator.generate(record).forEach(p -> {
                            predicates.add(new ProvenancedTerm(p, pogPredicateMappingInfo));
                        });

                        /* Objects */
                        if (pogObjectMappingInfo != null) {
                            pogObjectGenerator = pogObjectMappingInfo.getTermGenerator();
                        }

                        if (pogObjectMappingInfo != null && pogObjectGenerator != null) {
                            List<Value> objects = pogObjectGenerator.generate(record);
                            ArrayList<ProvenancedTerm> provenancedObjects = new ArrayList<>();

                            objects.forEach(object -> {
                                provenancedObjects.add(new ProvenancedTerm(object, pogMapping.getObjectMappingInfo()));
                            });

                            if (objects.size() > 0) {
                                //add pogs
                                pogs.addAll(combineMultiplePOGs(predicates, provenancedObjects, poGraphs));
                            }

                            //check if we are dealing with a parentTriplesMap (RefObjMap)
                        } else if (pogMapping.getParentTriplesMap() != null) {
                            List<ProvenancedTerm> objects;

                            //check if need to apply a join condition
                            if (!pogMapping.getJoinConditions().isEmpty()) {
                                objects = this.getIRIsWithConditions(record, pogMapping.getParentTriplesMap(), pogMapping.getJoinConditions());
                            } else {
                                objects = this.getAllIRIs(pogMapping.getParentTriplesMap());
                            }

                            pogs.addAll(combineMultiplePOGs(predicates, objects, poGraphs));
                        }
                    }

                    pogs.forEach(pog -> {
                        pogFunction.accept(finalSubject, pog);
                    });
                }
            }
        }

        if (removeDuplicates) {
            this.resultingQuads.removeDuplicates();
        }

        // Add the legacy store to the list of targets as well
        this.targetStores.put(valueFactory.createIRI("rmlmapper://default.store"), this.resultingQuads);
        return this.targetStores;
    }

    public HashMap<Value, QuadStore> execute(List<Value> triplesMaps) throws Exception {
        return this.execute(triplesMaps, false, null);
    }


    private List<PredicateObjectGraph> generatePredicateObjectGraphs(Mapping mapping, Record record, List<ProvenancedTerm> alreadyNeededGraphs) throws Exception {
        ArrayList<PredicateObjectGraph> results = new ArrayList<>();

        List<PredicateObjectGraphMapping> predicateObjectGraphMappings = mapping.getPredicateObjectGraphMappings();

        for (PredicateObjectGraphMapping pogMapping : predicateObjectGraphMappings) {
            ArrayList<ProvenancedTerm> predicates = new ArrayList<>();
            ArrayList<ProvenancedTerm> poGraphs = new ArrayList<>(alreadyNeededGraphs);

            if (pogMapping.getGraphMappingInfo() != null && pogMapping.getGraphMappingInfo().getTermGenerator() != null) {
                pogMapping.getGraphMappingInfo().getTermGenerator().generate(record).forEach(term -> {
                    if (!term.equals(valueFactory.createIRI(NAMESPACES.RR + "defaultGraph"))) {
                        poGraphs.add(new ProvenancedTerm(term));
                    }
                });
            }

            pogMapping.getPredicateMappingInfo().getTermGenerator().generate(record).forEach(p -> {
                predicates.add(new ProvenancedTerm(p, pogMapping.getPredicateMappingInfo()));
            });

            if (pogMapping.getObjectMappingInfo() != null && pogMapping.getObjectMappingInfo().getTermGenerator() != null) {
                List<Value> objects = pogMapping.getObjectMappingInfo().getTermGenerator().generate(record);
                ArrayList<ProvenancedTerm> provenancedObjects = new ArrayList<>();

                objects.forEach(object -> {
                    provenancedObjects.add(new ProvenancedTerm(object, pogMapping.getObjectMappingInfo()));
                });

                if (!objects.isEmpty()) {
                    //add pogs
                    results.addAll(combineMultiplePOGs(predicates, provenancedObjects, poGraphs));
                }

                //check if we are dealing with a parentTriplesMap (RefObjMap)
            } else if (pogMapping.getParentTriplesMap() != null) {
                List<ProvenancedTerm> objects;

                //check if need to apply a join condition
                if (!pogMapping.getJoinConditions().isEmpty()) {
                    objects = this.getIRIsWithConditions(record, pogMapping.getParentTriplesMap(), pogMapping.getJoinConditions());
                    //this.generateTriples(subject, po.getPredicateGenerator(), objects, record, combinedGraphs);
                } else {
                    objects = this.getAllIRIs(pogMapping.getParentTriplesMap());
                }

                results.addAll(combineMultiplePOGs(predicates, objects, poGraphs));
            }
        }

        return results;
    }

    private boolean generateQuad(ProvenancedTerm subject, ProvenancedTerm predicate, ProvenancedTerm object, ProvenancedTerm graph) {
        Value g = null;
        Set<Value> targets = new HashSet<Value>();

        if (subject != null && predicate != null && object != null) {
            if (graph != null) {
                g = graph.getTerm();
                targets.addAll(graph.getTargets());
            }

            if (subject.getTerm().stringValue().contains(IDLabFunctions.MAGIC_MARKER)
                    || subject.getTerm().stringValue().contains(IDLabFunctions.MAGIC_MARKER_ENCODED)
                    || predicate.getTerm().stringValue().contains(IDLabFunctions.MAGIC_MARKER)
                    || predicate.getTerm().stringValue().contains(IDLabFunctions.MAGIC_MARKER_ENCODED)
                    || object.getTerm().stringValue().contains(IDLabFunctions.MAGIC_MARKER)
                    || object.getTerm().stringValue().contains(IDLabFunctions.MAGIC_MARKER_ENCODED))
                return false;

            if (g != null && (g.stringValue().contains(IDLabFunctions.MAGIC_MARKER) || g.stringValue().contains(IDLabFunctions.MAGIC_MARKER_ENCODED)))
                return false;

            // Get all possible targets for triple, the Set guarantees that we don't have duplicates
            targets.addAll(subject.getTargets());
            targets.addAll(predicate.getTargets());
            targets.addAll(object.getTargets());

            // If we have targets, write to them
            if (!targets.isEmpty()) {
                for (Value t: targets) {
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

    private List<ProvenancedTerm> getIRIsWithConditions(Record record, Value triplesMap, List<MultipleRecordsFunctionExecutor> conditions) throws Exception {
        ArrayList<ProvenancedTerm> goodIRIs = new ArrayList<ProvenancedTerm>();
        ArrayList<List<ProvenancedTerm>> allIRIs = new ArrayList<List<ProvenancedTerm>>();

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

    private List<ProvenancedTerm> getIRIsWithTrueCondition(Record child, Value triplesMap, MultipleRecordsFunctionExecutor condition) throws Exception {
        Mapping mapping = this.mappings.get(triplesMap);

        //iterator over all the records corresponding with @triplesMap
        List<Record> records = this.getRecords(triplesMap);
        //this array contains all the IRIs that are valid regarding @path and @values
        ArrayList<ProvenancedTerm> iris = new ArrayList<ProvenancedTerm>();

        for (int i = 0; i < records.size(); i++) {
            Record parent = records.get(i);

            HashMap<String, Record> recordsMap = new HashMap<>();
            recordsMap.put("child", child);
            recordsMap.put("parent", parent);

            Object expectedBoolean = condition.execute(recordsMap);

            if (Boolean.TRUE.equals(expectedBoolean)) {
                List<ProvenancedTerm> subjects = this.getSubject(triplesMap, mapping, parent, i);
                iris.addAll(subjects);
            } else {
                logger.warn("The used condition with the Parent Triples Map does not return a boolean.");
            }
        }

        return iris;
    }

    private List<ProvenancedTerm> getSubject(Value triplesMap, Mapping mapping, Record record, int i) throws Exception {
        if (!this.subjectCache.containsKey(triplesMap)) {
            this.subjectCache.put(triplesMap, new HashMap<Integer, List<ProvenancedTerm>>());
        }

        if (!this.subjectCache.get(triplesMap).containsKey(i)) {
            TermGenerator generator = mapping.getSubjectMappingInfo().getTermGenerator();
            List<Value> nodes = generator.generate(record);

            if (!nodes.isEmpty()) {
                List<Value> targets = mapping.getSubjectMappingInfo().getTargets();
                List<ProvenancedTerm> terms = new ArrayList<>();
                Metadata meta = new Metadata(triplesMap, mapping.getSubjectMappingInfo().getTerm());

                // TODO: only create metadata when it's required
                for (int j=0; j < nodes.size(); j++) {
                    terms.add(new ProvenancedTerm(nodes.get(j), meta, targets));
                }
                this.subjectCache.get(triplesMap).put(i, terms);
                return terms;
            }
        }

        return this.subjectCache.get(triplesMap).get(i);
    }

    private List<ProvenancedTerm> getAllIRIs(Value triplesMap) throws Exception {
        Mapping mapping = this.mappings.get(triplesMap);

        List<Record> records = getRecords(triplesMap);
        ArrayList<ProvenancedTerm> iris = new ArrayList<ProvenancedTerm>();

        for (int i = 0; i < records.size(); i++) {
            Record record = records.get(i);
            List<ProvenancedTerm> subjects = getSubject(triplesMap, mapping, record, i);
            if (subjects != null)
                iris.addAll(subjects);
        }

        return iris;
    }

    private List<Record> getRecords(Value triplesMap) throws Exception {
        if (!this.recordsHolders.containsKey(triplesMap)) {
            this.recordsHolders.put(triplesMap, this.recordsFactory.createRecords(triplesMap, this.rmlStore));
        }

        return this.recordsHolders.get(triplesMap);
    }

    private List<PredicateObjectGraph> combineMultiplePOGs(List<ProvenancedTerm> predicates, List<ProvenancedTerm> objects, List<ProvenancedTerm> graphs) {
        ArrayList<PredicateObjectGraph> results = new ArrayList<>();

        if (graphs.isEmpty()) {
            graphs.add(null);
        }

        predicates.forEach(p -> {
            objects.forEach(o -> {
                graphs.forEach(g -> {
                    results.add(new PredicateObjectGraph(p, o, g));
                });
            });
        });

        return results;
    }

    public static String getNewBlankNodeID() {
        String temp = "" + Executor.blankNodeCounter;
        Executor.blankNodeCounter++;

        return temp;
    }

    public List<Value> getTriplesMaps() {
        return initializer.getTriplesMaps();
    }

    public QuadStore getRMLStore() {
        return this.rmlStore;
    }

    public HashMap<Value, QuadStore> getTargets(){
        if (this.targetStores.isEmpty()){
            return null;
        }
        return this.targetStores;
    }

    
    public void verifySources(String basepath) throws Exception {
        for (Value triplesMap : this.getTriplesMaps()) {
            List<Value> logicalSources = Utils.getObjectsFromQuads(rmlStore.getQuads(triplesMap, valueFactory.createIRI(NAMESPACES.RML + "logicalSource"), null));
            Value logicalSource = logicalSources.get(0);
            List<Value> sources = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, valueFactory.createIRI(NAMESPACES.RML + "source"), null));
            for (Value source : sources) {
                String value = source.stringValue();
                if (source.isLiteral()) {
                    InputStream is;
                    if (Utils.isRemoteFile(value)) {
                        is = new RemoteFileAccess(value).getInputStream();
                    } else {
                        is = new LocalFileAccess(value, basepath, ((Literal) source).getDatatype().stringValue()).getInputStream();
                    }
                    is.close(); // close resources.
                }
            }
        }
    }
}

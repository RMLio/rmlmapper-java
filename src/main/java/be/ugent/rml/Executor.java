package be.ugent.rml;

import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.functions.MultipleRecordsFunctionExecutor;
import be.ugent.rml.metadata.Metadata;
import be.ugent.rml.metadata.MetadataGenerator;
import be.ugent.rml.records.Record;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.RDF4JStore;
import be.ugent.rml.store.SimpleQuadStore;
import be.ugent.rml.store.RDF4JStore;
import be.ugent.rml.term.ProvenancedQuad;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.ProvenancedTerm;
import be.ugent.rml.term.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.function.BiConsumer;

public class Executor {

    private static final Logger logger = LoggerFactory.getLogger(Executor.class);

    private Initializer initializer;
    private HashMap<Term, List<Record>> recordsHolders;
    // this map stores for every Triples Map, which is a Term, a map with the record index and the record's corresponding subject,
    // which is a ProvenancedTerm.
    private HashMap<Term, HashMap<Integer, ProvenancedTerm>> subjectCache;
    private QuadStore resultingQuads;
    private QuadStore rmlStore;
    private HashMap<Term, QuadStore> targetStores;
    private RecordsFactory recordsFactory;
    private static int blankNodeCounter = 0;
    private HashMap<Term, Mapping> mappings;
    private String baseIRI;

    public Executor(QuadStore rmlStore, RecordsFactory recordsFactory, String baseIRI) throws Exception {
        this(rmlStore, recordsFactory, null, null, baseIRI);
    }

    public Executor(QuadStore rmlStore, RecordsFactory recordsFactory, FunctionLoader functionLoader, String baseIRI) throws Exception {
        this(rmlStore, recordsFactory, functionLoader, null, baseIRI);
    }

    public Executor(QuadStore rmlStore, RecordsFactory recordsFactory, FunctionLoader functionLoader, QuadStore resultingQuads, String baseIRI) throws Exception {
        this.initializer = new Initializer(rmlStore, functionLoader);
        this.mappings = this.initializer.getMappings();
        this.rmlStore = rmlStore;
        this.recordsFactory = recordsFactory;
        this.baseIRI = baseIRI;
        this.recordsHolders = new HashMap<Term, List<Record>>();
        this.subjectCache = new HashMap<Term, HashMap<Integer, ProvenancedTerm>>();
        this.targetStores = new HashMap<Term, QuadStore>();

        // Default store if no Targets are available for a triple
        if (resultingQuads == null) {
            this.resultingQuads = new RDF4JStore();
        } else {
            this.resultingQuads = resultingQuads;
        }

        // Output stores for Targets in Term Maps
        for (Map.Entry<Term, Mapping> tm: this.mappings.entrySet()) {
            Term triplesMap = tm.getKey();
            Mapping mapping = tm.getValue();
            Set<Term> targets = new HashSet<Term>();

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
            for (Term t: targets) {
                logger.debug("Adding target for " + t);
                this.targetStores.put(t, new RDF4JStore());
            }
        }
    }

    /*
     * Backwards compatibility for the V4.X.X releases.
     * This API will be deprecated in the first V5.X.X release in which this API will change to the new one.
     */
    @Deprecated
    public QuadStore execute(List<Term> triplesMaps, boolean removeDuplicates, MetadataGenerator metadataGenerator) throws Exception {
        HashMap<Term, QuadStore> result = this.executeV5(triplesMaps, removeDuplicates, metadataGenerator);
        return result.get(new NamedNode("rmlmapper://legacy.store"));
    }

    @Deprecated
    public QuadStore executeWithFunction(List<Term> triplesMaps, boolean removeDuplicates, BiConsumer<ProvenancedTerm, PredicateObjectGraph> pogFunction) throws Exception {
        HashMap<Term, QuadStore> result = this.executeWithFunctionV5(triplesMaps, removeDuplicates, pogFunction);
        return result.get(new NamedNode("rmlmapper://legacy.store"));
    }

    @Deprecated
    public QuadStore execute(List<Term> triplesMaps) throws Exception {
        HashMap<Term, QuadStore> result = this.executeV5(triplesMaps, false, null);
        return result.get(new NamedNode("rmlmapper://legacy.store"));
    }

    /*
     * New public API for the V5.X.X. releases
     */
    public HashMap<Term, QuadStore> executeV5(List<Term> triplesMaps, boolean removeDuplicates, MetadataGenerator metadataGenerator) throws Exception {

        BiConsumer<ProvenancedTerm, PredicateObjectGraph> pogFunction;

        if (metadataGenerator != null && metadataGenerator.getDetailLevel().getLevel() >= MetadataGenerator.DETAIL_LEVEL.TRIPLE.getLevel()) {
            pogFunction = (subject, pog) -> {
                generateQuad(subject, pog.getPredicate(), pog.getObject(), pog.getGraph());
                metadataGenerator.insertQuad(new ProvenancedQuad(subject, pog.getPredicate(), pog.getObject(), pog.getGraph()));
            };
        } else {
            pogFunction = (subject, pog) -> {
                generateQuad(subject, pog.getPredicate(), pog.getObject(), pog.getGraph());
            };
        }

        return executeWithFunctionV5(triplesMaps, removeDuplicates, pogFunction);
    }

    public HashMap<Term, QuadStore> executeWithFunctionV5(List<Term> triplesMaps, boolean removeDuplicates, BiConsumer<ProvenancedTerm, PredicateObjectGraph> pogFunction) throws Exception {
        //check if TriplesMaps are provided
        if (triplesMaps == null || triplesMaps.isEmpty()) {
            triplesMaps = this.initializer.getTriplesMaps();
        }

        //we execute every mapping
        for (Term triplesMap : triplesMaps) {
            Mapping mapping = this.mappings.get(triplesMap);

            List<Record> records = this.getRecords(triplesMap);

            for (int j = 0; j < records.size(); j++) {
                Record record = records.get(j);
                ProvenancedTerm subject = getSubject(triplesMap, mapping, record, j);

                // If we have subject and it's a named node,
                // we validate it and make it an absolute IRI if needed.
                if (subject != null && subject.getTerm() instanceof NamedNode) {
                    String iri = subject.getTerm().getValue();

                    // Is the IRI valid?
                    if (!Utils.isValidIRI(iri)) {
                        logger.error("The subject \"" + iri + "\" is not a valid IRI. Skipped.");
                        subject = null;

                    // Is the IRI relative?
                    } else if (Utils.isRelativeIRI(iri)) {

                        // Check the base IRI to see if we can use it to turn the IRI into an absolute one.
                        if (this.baseIRI == null) {
                            logger.error("The base IRI is null, so relative IRI of subject cannot be turned in to absolute IRI. Skipped.");
                            subject = null;
                        } else {
                            logger.debug("The IRI of subject is made absolute via base IRI.");
                            iri = this.baseIRI + iri;

                            // Check if the new absolute IRI is valid.
                            if (Utils.isValidIRI(iri)) {
                                subject = new ProvenancedTerm(new NamedNode(iri), subject.getMetadata(), subject.getTargets());
                            } else {
                                logger.error("The subject \"" + iri + "\" is not a valid IRI. Skipped.");
                            }
                        }
                    }
                }

                final ProvenancedTerm finalSubject = subject;

                //TODO validate subject or check if blank node
                if (subject != null) {
                    List<ProvenancedTerm> subjectGraphs = new ArrayList<>();

                    mapping.getGraphMappingInfos().forEach(mappingInfo -> {
                        List<Term> terms = null;

                        try {
                            terms = mappingInfo.getTermGenerator().generate(record);
                        } catch (Exception e) {
                            //todo be more nice and gentle
                            e.printStackTrace();
                        }

                        terms.forEach(term -> {
                            if (!term.equals(new NamedNode(NAMESPACES.RR + "defaultGraph"))) {
                                subjectGraphs.add(new ProvenancedTerm(term));
                            }
                        });
                    });

                    List<PredicateObjectGraph> pogs = this.generatePredicateObjectGraphs(mapping, record, subjectGraphs);

                    pogs.forEach(pog -> pogFunction.accept(finalSubject, pog));
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

    public HashMap<Term, QuadStore> executeV5(List<Term> triplesMaps) throws Exception {
        return this.executeV5(triplesMaps, false, null);
    }


    private List<PredicateObjectGraph> generatePredicateObjectGraphs(Mapping mapping, Record record, List<ProvenancedTerm> alreadyNeededGraphs) throws Exception {
        ArrayList<PredicateObjectGraph> results = new ArrayList<>();

        List<PredicateObjectGraphMapping> predicateObjectGraphMappings = mapping.getPredicateObjectGraphMappings();

        for (PredicateObjectGraphMapping pogMapping : predicateObjectGraphMappings) {
            ArrayList<ProvenancedTerm> predicates = new ArrayList<>();
            ArrayList<ProvenancedTerm> poGraphs = new ArrayList<>();
            poGraphs.addAll(alreadyNeededGraphs);

            if (pogMapping.getGraphMappingInfo() != null && pogMapping.getGraphMappingInfo().getTermGenerator() != null) {
                pogMapping.getGraphMappingInfo().getTermGenerator().generate(record).forEach(term -> {
                    if (!term.equals(new NamedNode(NAMESPACES.RR + "defaultGraph"))) {
                        poGraphs.add(new ProvenancedTerm(term));
                    }
                });
            }

            pogMapping.getPredicateMappingInfo().getTermGenerator().generate(record).forEach(p -> {
                predicates.add(new ProvenancedTerm(p, pogMapping.getPredicateMappingInfo()));
            });

            if (pogMapping.getObjectMappingInfo() != null && pogMapping.getObjectMappingInfo().getTermGenerator() != null) {
                List<Term> objects = pogMapping.getObjectMappingInfo().getTermGenerator().generate(record);
                ArrayList<ProvenancedTerm> provenancedObjects = new ArrayList<>();

                objects.forEach(object -> {
                    provenancedObjects.add(new ProvenancedTerm(object, pogMapping.getObjectMappingInfo()));
                });

                if (objects.size() > 0) {
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

    private void generateQuad(ProvenancedTerm subject, ProvenancedTerm predicate, ProvenancedTerm object, ProvenancedTerm graph) {
        Term g = null;
        Set<Term> targets = new HashSet<Term>();

        if (graph != null) {
            g = graph.getTerm();
            targets.addAll(graph.getTargets());
        }

        if (subject != null && predicate != null && object != null) {
            // Get all possible targets for triple, the Set guarantees that we don't have duplicates
            targets.addAll(subject.getTargets());
            targets.addAll(predicate.getTargets());
            targets.addAll(object.getTargets());

            // If we have targets, write to them
            if (!targets.isEmpty()) {
                for(Term t: targets) {
                    this.targetStores.get(t).addQuad(subject.getTerm(), predicate.getTerm(), object.getTerm(), g);
                }
            }
            // If not, use the default processor target
            else {
                this.resultingQuads.addQuad(subject.getTerm(), predicate.getTerm(), object.getTerm(), g);
            }
        }
    }

    private List<ProvenancedTerm> getIRIsWithConditions(Record record, Term triplesMap, List<MultipleRecordsFunctionExecutor> conditions) throws Exception {
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

    private List<ProvenancedTerm> getIRIsWithTrueCondition(Record child, Term triplesMap, MultipleRecordsFunctionExecutor condition) throws Exception {
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

            if (expectedBoolean instanceof Boolean) {
                if ((boolean) expectedBoolean) {
                    ProvenancedTerm subject = this.getSubject(triplesMap, mapping, parent, i);
                    iris.add(subject);
                }
            } else {
                logger.warn("The used condition with the Parent Triples Map does not return a boolean.");
            }
        }

        return iris;
    }

    private ProvenancedTerm getSubject(Term triplesMap, Mapping mapping, Record record, int i) throws Exception {
        if (!this.subjectCache.containsKey(triplesMap)) {
            this.subjectCache.put(triplesMap, new HashMap<Integer, ProvenancedTerm>());
        }

        if (!this.subjectCache.get(triplesMap).containsKey(i)) {
            List<Term> nodes = mapping.getSubjectMappingInfo().getTermGenerator().generate(record);

            if (!nodes.isEmpty()) {
                //todo: only create metadata when it's required
                Metadata meta = new Metadata(triplesMap, mapping.getSubjectMappingInfo().getTerm());
                List<Term> targets = mapping.getSubjectMappingInfo().getTargets();
                this.subjectCache.get(triplesMap).put(i, new ProvenancedTerm(nodes.get(0), meta, targets));
            }
        }

        return this.subjectCache.get(triplesMap).get(i);
    }

    private List<ProvenancedTerm> getAllIRIs(Term triplesMap) throws Exception {
        Mapping mapping = this.mappings.get(triplesMap);

        List<Record> records = getRecords(triplesMap);
        ArrayList<ProvenancedTerm> iris = new ArrayList<ProvenancedTerm>();

        for (int i = 0; i < records.size(); i++) {
            Record record = records.get(i);
            ProvenancedTerm subject = getSubject(triplesMap, mapping, record, i);

            iris.add(subject);
        }

        return iris;
    }

    private List<Record> getRecords(Term triplesMap) throws IOException, SQLException, ClassNotFoundException {
        if (!this.recordsHolders.containsKey(triplesMap)) {
            this.recordsHolders.put(triplesMap, this.recordsFactory.createRecords(triplesMap, this.rmlStore));
        }

        return this.recordsHolders.get(triplesMap);
    }

    public FunctionLoader getFunctionLoader() {
        return this.initializer.getFunctionLoader();
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

    public List<Term> getTriplesMaps() {
        return initializer.getTriplesMaps();
    }
}
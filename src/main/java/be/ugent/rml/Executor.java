package be.ugent.rml;

import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.functions.JoinConditionFunction;
import be.ugent.rml.records.Record;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.ProvenancedQuad;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.SimpleQuadStore;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.ProvenancedTerm;
import be.ugent.rml.term.Term;
import be.ugent.rml.termgenerator.TermGenerator;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class Executor {

    private Initializer initializer;
    private HashMap<Term, List<Record>> recordsHolders;
    // this map stores for every Triples Map, which is a Term, a map with the record index and the record's corresponding subject,
    // which is a ProvenancedTerm.
    private HashMap<Term, HashMap<Integer, ProvenancedTerm>> subjectCache;
    private QuadStore resultingTriples;
    private QuadStore rmlStore;
    private RecordsFactory recordsFactory;
    private static int blankNodeCounter = 0;
    private HashMap<Term, Mapping> mappings;

    public Executor(QuadStore rmlStore, RecordsFactory recordsFactory) throws IOException {
        this(rmlStore, recordsFactory, null);
    }

    public Executor(QuadStore rmlStore, RecordsFactory recordsFactory, FunctionLoader functionLoader) throws IOException {
        this.initializer = new Initializer(rmlStore, functionLoader);
        this.mappings = this.initializer.getMappings();
        this.resultingTriples = new SimpleQuadStore();
        this.rmlStore = rmlStore;
        this.recordsFactory = recordsFactory;
        this.recordsHolders = new HashMap<Term, List<Record>>();
        this.subjectCache = new HashMap<Term, HashMap<Integer, ProvenancedTerm>>();
    }

    public QuadStore execute(List<Term> triplesMaps, boolean removeDuplicates, MetadataGenerator metadataGenerator) throws IOException {

        BiConsumer<ProvenancedTerm, PredicateObjectGraph> pogFunction;

        if (metadataGenerator != null && metadataGenerator.getDetailLevel().getLevel() >= MetadataGenerator.DETAIL_LEVEL.TRIPLE.getLevel()) {
            pogFunction = (subject, pog) -> {
                generateQuad(subject, pog.getPredicate(), pog.getObject(), pog.getGraph());
                metadataGenerator.insertQuad(new ProvenancedQuad(subject, pog.getPredicate(), pog.getObject(), pog.getGraph()));
            };
        } else {
            pogFunction = (subject,pog) -> {
                generateQuad(subject, pog.getPredicate(), pog.getObject(), pog.getGraph());
            };
        }

        return executeWithFunction(triplesMaps, removeDuplicates, pogFunction);
    }

    public QuadStore executeWithFunction(List<Term> triplesMaps, boolean removeDuplicates, BiConsumer<ProvenancedTerm, PredicateObjectGraph> pogFunction) throws IOException {
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

                //TODO validate subject or check if blank node
                if (subject != null) {
                    List<ProvenancedTerm> subjectGraphs = new ArrayList<>();

                    mapping.getGraphMappingInfos().forEach(mappingInfo -> {
                        List<Term> terms = null;
                        try {
                            terms = mappingInfo.getTermGenerator().generate(record);
                        } catch (IOException e) {
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

                    pogs.forEach(pog -> pogFunction.accept(subject, pog));
                }
            }
        }

        if (removeDuplicates) {
            this.resultingTriples.removeDuplicates();
        }

        return resultingTriples;
    }

    public QuadStore execute(List<Term> triplesMaps) throws IOException {
        return this.execute(triplesMaps, false, null);
    }


    private List<PredicateObjectGraph> generatePredicateObjectGraphs(Mapping mapping, Record record,  List<ProvenancedTerm> alreadyNeededGraphs) throws IOException {
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

        if (graph != null) {
            g = graph.getTerm();
        }

        this.resultingTriples.addQuad(subject.getTerm(), predicate.getTerm(), object.getTerm(), g);
    }

    private List<ProvenancedTerm> getIRIsWithConditions(Record record, Term triplesMap, List<JoinConditionFunction> conditions) throws IOException {
        ArrayList<ProvenancedTerm> goodIRIs = new ArrayList<ProvenancedTerm>();
        ArrayList<List<ProvenancedTerm>> allIRIs = new ArrayList<List<ProvenancedTerm>>();

        for (JoinConditionFunction condition : conditions) {
            allIRIs.add(this.getIRIsWithTrueCondition(record, triplesMap, condition));
        }

        if (allIRIs.size() > 0) {
            for (ProvenancedTerm iri : allIRIs.get(0)) {
                int i = 1;

                while (i < allIRIs.size() && !allIRIs.get(i).contains(iri)) {
                    i++;
                }

                if (i == allIRIs.size()) {
                    goodIRIs.add(iri);
                }
            }
        }

        return goodIRIs;
    }

    private List<ProvenancedTerm> getIRIsWithTrueCondition(Record child, Term triplesMap, JoinConditionFunction condition) throws IOException {
        Mapping mapping = this.mappings.get(triplesMap);

        //iterator over all the records corresponding with @triplesMap
        List<Record> records = this.getRecords(triplesMap);
        //this array contains all the IRIs that are valid regarding @path and @values
        ArrayList<ProvenancedTerm> iris = new ArrayList<ProvenancedTerm>();

        for (int i = 0; i < records.size(); i++) {
            Record parent = records.get(i);

            if (condition.execute(child, parent)) {
                ProvenancedTerm subject = this.getSubject(triplesMap, mapping, parent, i);
                iris.add(subject);
            }
        }

        return iris;
    }

    private ProvenancedTerm getSubject(Term triplesMap, Mapping mapping, Record record, int i) throws IOException {
        if (!this.subjectCache.containsKey(triplesMap)) {
            this.subjectCache.put(triplesMap, new HashMap<Integer, ProvenancedTerm>());
        }

        if (!this.subjectCache.get(triplesMap).containsKey(i)) {
            List<Term> nodes = mapping.getSubjectMappingInfo().getTermGenerator().generate(record);

            if (!nodes.isEmpty()) {
                //todo: only create metadata when it's required
                this.subjectCache.get(triplesMap).put(i, new ProvenancedTerm(nodes.get(0), new Metadata(triplesMap, mapping.getSubjectMappingInfo().getTerm())));
            }
        }

        return this.subjectCache.get(triplesMap).get(i);
    }

    private List<ProvenancedTerm> getAllIRIs(Term triplesMap) throws IOException {
        Mapping mapping = this.mappings.get(triplesMap);

        List<Record> records = getRecords(triplesMap);
        ArrayList<ProvenancedTerm> iris = new ArrayList<ProvenancedTerm>();

        for (int i = 0; i < records.size(); i ++) {
            Record record = records.get(i);
            ProvenancedTerm subject = getSubject(triplesMap, mapping, record, i);

            iris.add(subject);
        }

        return iris;
    }

    private List<Record> getRecords(Term triplesMap) throws IOException {
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
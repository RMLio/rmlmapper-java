package be.ugent.rml;

import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.functions.JoinConditionFunction;
import be.ugent.rml.records.Record;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.SimpleQuadStore;
import be.ugent.rml.term.BlankNode;
import be.ugent.rml.term.Literal;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Executor {

    private Initializer initializer;
    private HashMap<Term, List<Record>> recordsHolders;
    private HashMap<Term, HashMap<Integer, ProvenancedTerm>> subjects;
    private QuadStore resultingTriples;
    private QuadStore rmlStore;
    private RecordsFactory recordsFactory;
    private int blankNodeCounter;
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
        this.blankNodeCounter = 0;
        this.recordsHolders = new HashMap<Term, List<Record>>();
        this.subjects = new HashMap<Term, HashMap<Integer, ProvenancedTerm>>();
    }

    public QuadStore execute(List<Term> triplesMaps, boolean removeDuplicates) throws IOException {

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

                    for (Template graph: mapping.getSubject().getGraphs()) {
                        String g = Utils.applyTemplate(graph, record, true).get(0);

                        if (!g.equals(NAMESPACES.RR + "defaultGraph")) {
                            subjectGraphs.add(new ProvenancedTerm(new NamedNode(g)));
                        }
                    }

                    List<PredicateObjectGraph> pogs = this.generatePredicateObjectGraphs(mapping, record, subjectGraphs);

                    pogs.forEach(pog -> {
                        generateQuad(subject, pog.getPredicate(), pog.getObject(), pog.getGraph());
                    });
                }
            }
        }

        if (removeDuplicates) {
            this.resultingTriples.removeDuplicates();
        }

        return resultingTriples;
    }

    public QuadStore execute(List<Term> triplesMaps) throws IOException {
        return this.execute(triplesMaps, false);
    }

    private List<PredicateObjectGraph> generatePredicateObjectGraphs(Mapping mapping, Record record,  List<ProvenancedTerm> alreadyNeededGraphs) throws IOException {
        ArrayList<PredicateObjectGraph> results = new ArrayList<>();

        List<PredicateObjectGenerator> predicateObjectGenerators = mapping.getPredicateObjectGenerators();

        for (PredicateObjectGenerator po : predicateObjectGenerators) {
            ArrayList<ProvenancedTerm> predicates = new ArrayList<>();
            ArrayList<ProvenancedTerm> poGraphs = new ArrayList<>();
            poGraphs.addAll(alreadyNeededGraphs);

            for (Template graph : po.getGraphs()) {
                String g = Utils.applyTemplate(graph, record, true).get(0);

                if (!g.equals(NAMESPACES.RR + "defaultGraph")) {
                    poGraphs.add(new ProvenancedTerm(new NamedNode(g)));
                }
            }

            List<Template> predicateRules = po.getPredicates();

            predicateRules.forEach(predicateRule -> {
                List<String> terms = Utils.applyTemplate(predicateRule, record);

                terms.forEach(term -> {
                    predicates.add(new ProvenancedTerm(new NamedNode(term)));
                });
            });

            if (po.getFunction() != null) {
                ArrayList<ProvenancedTerm> objects = new ArrayList<>();
                List<String> objectStrings = (List<String>) po.getFunction().execute(record);

                if (objectStrings.size() > 0) {
                    if (po.getTermType().getValue().equals(NAMESPACES.RR + "IRI")) {
                        for (String object : objectStrings) {
                            //todo check valid IRI
                            objects.add(new ProvenancedTerm(new NamedNode(object)));
                        }
                    } else {
                        //is Literal
                        //add language tag if present
                        objectStrings.forEach(objectString -> {
                            if (po.getLanguage() != null) {
                                objects.add(new ProvenancedTerm(new Literal(objectString, po.getLanguage())));
                            } else if (po.getDataType() != null) {
                                //add datatype if present; language and datatype can't be combined because the language tag implies langString as datatype
                                objects.add(new ProvenancedTerm(new Literal(objectString, po.getDataType())));
                            } else {
                                objects.add(new ProvenancedTerm(new Literal(objectString)));
                            }
                        });
                    }

                    //add pogs
                    results.addAll(combineMultiplePOGs(predicates, objects, poGraphs));
                }

                //check if we are dealing with a parentTriplesMap (RefObjMap)
            } else if (po.getParentTriplesMap() != null) {
                List<ProvenancedTerm> objects;

                //check if need to apply a join condition
                if (!po.getJoinConditions().isEmpty()) {
                    objects = this.getIRIsWithConditions(record, po.getParentTriplesMap(), po.getJoinConditions());
                    //this.generateTriples(subject, po.getPredicates(), objects, record, combinedGraphs);
                } else {
                    objects = this.getAllIRIs(po.getParentTriplesMap());
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

    private ProvenancedTerm getSubject(Term triplesMap, Mapping mapping, Record record, int i) {
        if (!this.subjects.containsKey(triplesMap)) {
            this.subjects.put(triplesMap, new HashMap<Integer, ProvenancedTerm>());
        }

        if (!this.subjects.get(triplesMap).containsKey(i)) {
            //we want a IRI and not a Blank Node
            if (mapping.getSubject().getTermType().getValue().equals(NAMESPACES.RR + "IRI")) {
                List<String> subjects = (List<String>) mapping.getSubject().getFunction().execute(record);
                String subject = null;

                if (!subjects.isEmpty()) {
                    subject = subjects.get(0);
                }

                this.subjects.get(triplesMap).put(i,new ProvenancedTerm(new NamedNode(subject)));
            } else {
                //we want a Blank Node

                if (mapping.getSubject().getFunction() != null) {
                    this.subjects.get(triplesMap).put(i, new ProvenancedTerm(new BlankNode(mapping.getSubject().getFunction().execute(record).get(0).toString())));
                } else {
                    this.subjects.get(triplesMap).put(i, new ProvenancedTerm(new BlankNode("" + this.blankNodeCounter)));
                    this.blankNodeCounter++;
                }
            }
        }

        return this.subjects.get(triplesMap).get(i);
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
}
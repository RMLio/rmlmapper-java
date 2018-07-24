package be.ugent.rml;

import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.functions.JoinConditionFunction;
import be.ugent.rml.records.Record;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.SimpleQuadStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Executor {

    private Initializer initializer;
    private HashMap<String, List<Record>> recordsHolders;
    private HashMap<String, HashMap<Integer, String>> subjects;
    private QuadStore resultingTriples;
    private QuadStore rmlStore;
    private RecordsFactory recordsFactory;
    private int blankNodeCounter;
    private HashMap<String, Mapping> mappings;

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
        this.recordsHolders = new HashMap<String, List<Record>>();
        this.subjects = new HashMap<String, HashMap<Integer, String>>();
    }

    public QuadStore execute(List<String> triplesMaps, boolean removeDuplicates) throws IOException {

        //check if TriplesMaps are provided
        if (triplesMaps == null || triplesMaps.isEmpty()) {
            triplesMaps = this.initializer.getTriplesMaps();
        }

        //we execute every mapping
        for (String triplesMap : triplesMaps) {
            Mapping mapping = this.mappings.get(triplesMap);
            List<Record> records = this.getRecords(triplesMap);

            for (int j = 0; j < records.size(); j++) {
                Record record = records.get(j);
                String subject = getSubject(triplesMap, mapping, record, j);

                //TODO validate subject or check if blank node
                if (subject != null) {
                    this.generatePredicateObjectsForSubject(subject, mapping, record);
                }
            }
        }

        if (removeDuplicates) {
            this.resultingTriples.removeDuplicates();
        }

        return resultingTriples;
    }

    public QuadStore execute(List<String> triplesMaps) throws IOException {
        return this.execute(triplesMaps, false);
    }

    private void generatePredicateObjectsForSubject(String subject, Mapping mapping, Record record) throws IOException {
        ArrayList<String> subjectGraphs = new ArrayList<String>();

        for (Template graph: mapping.getSubject().getGraphs()) {
            String g = Utils.applyTemplate(graph, record, true).get(0);

            if (!g.equals(NAMESPACES.RR + "defaultGraph")) {
                subjectGraphs.add(g);
            }
        }

        List<PredicateObject> predicateObjects = mapping.getPredicateObjects();

        for (PredicateObject po : predicateObjects) {
            ArrayList<String> poGraphs = new ArrayList<String>();

            for (Template graph : po.getGraphs()) {
                String g = Utils.applyTemplate(graph, record, true).get(0);

                if (!g.equals(NAMESPACES.RR + "defaultGraph")) {
                    poGraphs.add(g);
                }
            }

            List<String> combinedGraphs = new ArrayList<String>();
            combinedGraphs.addAll(subjectGraphs);
            combinedGraphs.addAll(poGraphs);

            if (po.getFunction() != null) {
                List<String> objects = (List<String>) po.getFunction().execute(record);

                if (objects.size() > 0) {
                    if (po.getTermType().equals(NAMESPACES.RR + "IRI")) {
                        for (String object : objects) {
                            //todo check valid IRI
                        }
                    } else {
                        //is Literal
                        for (int i = 0; i < objects.size(); i++) {
                            objects.set(i, "\"" + objects.get(i) + "\"");

                            //add language tag if present
                            if (po.getLanguage() != null) {
                                objects.set(i, objects.get(i) + "@" + po.getLanguage());
                            } else if (po.getDataType() != null) {
                                //add datatype if present; language and datatype can't be combined because the language tag implies langString as datatype
                                objects.set(i, objects.get(i) + "^^<" + po.getDataType() + ">");
                            }
                        }
                    }


                    //generate the triples
                    this.generateTriples(subject, po.getPredicates(), objects, record, combinedGraphs);
                }

                //check if we are dealing with a parentTriplesMap (RefObjMap)
            } else if (po.getParentTriplesMap() != null) {
                //check if need to apply a join condition
                if (!po.getJoinConditions().isEmpty()) {
                    List<String> objects = this.getIRIsWithConditions(record, po.getParentTriplesMap(), po.getJoinConditions());
                    this.generateTriples(subject, po.getPredicates(), objects, record, combinedGraphs);
                } else {
                    List<String> objects = this.getAllIRIs(po.getParentTriplesMap());
                    this.generateTriples(subject, po.getPredicates(), objects, record, combinedGraphs);
                }
            }
        }
    }

    private void generateTriples(String subject, List<Template> predicates, List<String> objects, Record record, List<String> graphs) {
        for (Template p : predicates) {
            List<String> realPredicates = Utils.applyTemplate(p, record);

            for (String predicate : realPredicates) {
                for (String object : objects) {
                    if (object != null) {
                        if (graphs.size() > 0) {
                            for (String graph : graphs) {
                                this.resultingTriples.addQuad(subject, predicate, object, graph);
                            }
                        } else {
                            this.resultingTriples.addTriple(subject, predicate, object);
                        }
                    }
                }
            }
        }
    }

    private List<String> getIRIsWithConditions(Record record, String triplesMap, List<JoinConditionFunction> conditions) throws IOException {
        ArrayList<String> goodIRIs = new ArrayList<String>();
        ArrayList<List<String>> allIRIs = new ArrayList<List<String>>();

        for (JoinConditionFunction condition : conditions) {
            allIRIs.add(this.getIRIsWithTrueCondition(record, triplesMap, condition));
        }

        if (allIRIs.size() > 0) {
            for (String iri : allIRIs.get(0)) {
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

    private List<String> getIRIsWithTrueCondition(Record child, String triplesMap, JoinConditionFunction condition) throws IOException {
        Mapping mapping = this.mappings.get(triplesMap);

        //iterator over all the records corresponding with @triplesMap
        List<Record> records = this.getRecords(triplesMap);
        //this array contains all the IRIs that are valid regarding @path and @values
        ArrayList<String> iris = new ArrayList<String>();

        for (int i = 0; i < records.size(); i++) {
            Record parent = records.get(i);

            if (condition.execute(child, parent)) {
                String subject = this.getSubject(triplesMap, mapping, parent, i);
                iris.add(subject);
            }
        }

        return iris;
    }

    private String getSubject(String triplesMap, Mapping mapping, Record record, int i) {
        if (!this.subjects.containsKey(triplesMap)) {
            this.subjects.put(triplesMap, new HashMap<Integer, String>());
        }

        if (!this.subjects.get(triplesMap).containsKey(i)) {
            //we want a IRI and not a Blank Node
            if (mapping.getSubject().getTermType().equals(NAMESPACES.RR + "IRI")) {
                List<String> subjects = (List<String>) mapping.getSubject().getFunction().execute(record);
                String subject = null;

                if (!subjects.isEmpty()) {
                    subject = subjects.get(0);
                }

                this.subjects.get(triplesMap).put(i,subject);
            } else {
                //we want a Blank Node

                if (mapping.getSubject().getFunction() != null) {
                    this.subjects.get(triplesMap).put(i, "_:" + mapping.getSubject().getFunction().execute(record).get(0));
                } else {
                    this.subjects.get(triplesMap).put(i, "_:b" + this.blankNodeCounter);
                    this.blankNodeCounter++;
                }
            }
        }

        return this.subjects.get(triplesMap).get(i);
    }

    private List<String> getAllIRIs(String triplesMap) throws IOException {
        Mapping mapping = this.mappings.get(triplesMap);

        List<Record> records = getRecords(triplesMap);
        ArrayList<String> iris = new ArrayList<String>();

        for (int i = 0; i < records.size(); i ++) {
            Record record = records.get(i);
            String subject = getSubject(triplesMap, mapping, record, i);

            iris.add(subject);
        }

        return iris;
    }

    private List<Record> getRecords(String triplesMap) throws IOException {
        if (!this.recordsHolders.containsKey(triplesMap)) {
            this.recordsHolders.put(triplesMap, this.recordsFactory.createRecords(triplesMap, this.rmlStore));
        }

        return this.recordsHolders.get(triplesMap);
    }

    public FunctionLoader getFunctionLoader() {
        return this.initializer.getFunctionLoader();
    }
}
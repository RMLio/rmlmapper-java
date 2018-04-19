package be.ugent.rml;

import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.records.Record;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.SimpleQuadStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Executor {

    private Initializer initializer;
    private HashMap<String, Record[]> recordsHolders;
    private HashMap<String, HashMap<Integer, String>> subjects;
    private QuadStore resultingTriples;
    private QuadStore rmlStore;
    private RecordsFactory recordsFactory;
    private int blankNodeCounter;
    private HashMap<String, Mapping> mappings;

    public Executor(QuadStore rmlStore, RecordsFactory recordsFactory, FunctionLoader functionLoader) {
        this.initializer = new Initializer(rmlStore, functionLoader);
        this.mappings = this.initializer.getMappings();
        this.resultingTriples = new SimpleQuadStore();
        this.rmlStore = rmlStore;
        this.recordsFactory = recordsFactory;
        this.blankNodeCounter = 0;
        this.recordsHolders = new HashMap<String, Record[]>();
        this.subjects = new HashMap<String, HashMap<Integer, String>>();
    }

    public void execute(List<String> triplesMaps, boolean removeDuplicates) {

        //check if TriplesMaps are provided
        if (triplesMaps == null || triplesMaps.isEmpty()) {
            triplesMaps = this.initializer.getTriplesMaps();
        }

        //we execute every mapping
        for (int i = 0; i < triplesMaps.size(); i++) {
            String triplesMap = triplesMaps.get(i);
            Mapping mapping = this.mappings.get(triplesMap);

            Record[] records = this.getRecords(triplesMap);

            for (int j = 0; j < records.length; j++) {
                Record record = records[j];
                String subject = getSubject(triplesMap, mapping, record, j);

                //TODO validate subject or check if blank node
                this.generatePredicateObjectsForSubject(subject, mapping, record);
            }
        }

        if (removeDuplicates) {
            this.resultingTriples.removeDuplicates();
        }
    }

    public void execute(List<String> triplesMaps) {
        this.execute(triplesMaps, false);
    }

    private void generatePredicateObjectsForSubject(String subject, Mapping mapping, Record record) {
        ArrayList<String> subjectGraphs = new ArrayList<>();

        for (List<Element> graph: mapping.getSubject().getGraphs()) {
            subjectGraphs.add(Utils.applyTemplate(graph, record).get(0));
        }

        List<PredicateObject> predicateObjects = mapping.getPredicateObjects();

        for (PredicateObject po : predicateObjects) {
            ArrayList<String> poGraphs = new ArrayList<String>();

            for (List<Element> graph : po.getGraphs()) {
                poGraphs.add(Utils.applyTemplate(graph, record).get(0));
            }

            List<String> combinedGraphs = new ArrayList<String>();
            combinedGraphs.addAll(subjectGraphs);
            combinedGraphs.addAll(poGraphs);

            if (po.getFunction() != null) {
                List<String> objects = po.getFunction().execute(record, po.getParameters());

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
                                //add datatype if present; language and datatype can't be combined because the lauguage tag implies langString as datatype
                                objects.set(i, objects.get(i) + "^^" + po.getDataType());
                            }
                        }
                    }


                    //generate the triples
                    this.generateTriples(subject, po.getPredicates(), objects, record, combinedGraphs);
                }

                //check if we are dealing with a parentTriplesMap (RefObjMap)
            } else if (po.getParentTriplesMap() != null) {
                //check if need to apply a join condition
                if (po.getJoinConditions() != null) {
                    ArrayList<ValuedJoinCondition> valuedJoinConditions = new ArrayList<ValuedJoinCondition>();

                    for (JoinCondition join : po.getJoinConditions()) {
                        valuedJoinConditions.add(new ValuedJoinCondition(join.getParent(), Utils.applyTemplate(join.getChild(), record)));
                    }

                    List<String> objects = this.getIRIsWithConditions(po.getParentTriplesMap(), valuedJoinConditions);
                    this.generateTriples(subject, po.getPredicates(), objects, record, combinedGraphs);
                } else {
                    List<String> objects = this.getAllIRIs(po.getParentTriplesMap());
                    this.generateTriples(subject, po.getPredicates(), objects, record, combinedGraphs);
                }
            }
        }
    }

    private void generateTriples(String subject, List<List<Element>> predicates, List<String> objects, Record record, List<String> graphs) {
        for (List<Element> p : predicates) {
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

    private List<String> getIRIsWithConditions(String triplesMap, List<ValuedJoinCondition> conditions) {
        ArrayList<String> goodIRIs = new ArrayList<String>();
        ArrayList<List<String>> allIRIs = new ArrayList<List<String>>();

        for (ValuedJoinCondition condition : conditions) {
            allIRIs.add(this.getIRIsWithValue(triplesMap, condition.getPath(), condition.getValues()));
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

    private List<String> getIRIsWithValue(String triplesMap, List<Element> path, List<String> values) {
        Mapping mapping = this.mappings.get(triplesMap);

        //iterator over all the records corresponding with @triplesMap
        Record[] records = this.getRecords(triplesMap);
        //this array contains all the IRIs that are valid regarding @path and @values
        ArrayList<String> iris = new ArrayList<String>();

        for (int j = 0; j < values.size(); j++) {
            String value = values.get(j);

            for (int i = 0; i < records.length; i ++) {
                Record record = records[i];
                List<String> foundValues = Utils.applyTemplate(path, record);

                //we found a match
                if (foundValues.contains(value)) {
                    //we get the subject corresponding to the current record
                    String subject = this.getSubject(triplesMap, mapping, record, i);
                    iris.add(subject);
                }
            }
        }

        return iris;
    }

    private String getSubject(String triplesMap, Mapping mapping, Record record, int i) {
        if (!this.subjects.containsKey(triplesMap)) {
            this.subjects.put(triplesMap, new HashMap<>());
        }

        if (!this.subjects.get(triplesMap).containsKey(i)) {
            //we want a IRI and not a Blank Node
            if (mapping.getSubject().getTermType().equals(NAMESPACES.RR + "IRI")) {
                //TODO encode URI
                this.subjects.get(triplesMap).put(i, mapping.getSubject().getFunction().execute(record, mapping.getSubject().getParameters()).get(0));
            } else {
                //we want a Blank Node
                this.subjects.get(triplesMap).put(i, "_:b" + this.blankNodeCounter);
                this.blankNodeCounter ++;
            }
        }

        return this.subjects.get(triplesMap).get(i);
    }

    private List<String> getAllIRIs(String triplesMap) {
        Mapping mapping = this.mappings.get(triplesMap);

        Record[] records = getRecords(triplesMap);
        ArrayList<String> iris = new ArrayList<String>();

        for (int i = 0; i < iris.size(); i ++) {
            Record record = records[i];
            String subject = getSubject(triplesMap, mapping, record, i);

            iris.add(subject);
        }

        return iris;
    }

    private Record[] getRecords(String triplesMap) {
        if (!this.recordsHolders.containsKey(triplesMap)) {
            this.recordsHolders.put(triplesMap, this.recordsFactory.createRecords(triplesMap, this.rmlStore));
        }

        return this.recordsHolders.get(triplesMap);
    }
}
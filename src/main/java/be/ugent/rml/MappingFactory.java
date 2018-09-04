package be.ugent.rml;

import be.ugent.rml.extractor.ConstantExtractor;
import be.ugent.rml.extractor.Extractor;
import be.ugent.rml.extractor.ReferenceExtractor;
import be.ugent.rml.functions.*;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.Literal;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import be.ugent.rml.termgenerator.BlankNodeGenerator;
import be.ugent.rml.termgenerator.LiteralGenerator;
import be.ugent.rml.termgenerator.NamedNodeGenerator;
import be.ugent.rml.termgenerator.TermGenerator;
import org.elasticsearch.cluster.routing.allocation.decider.Decision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class MappingFactory {
    private final FunctionLoader functionLoader;
    private TermGenerator subject;
    private List<TermGenerator> graphs;
    private Term triplesMap;
    private QuadStore store;
    private ArrayList<PredicateObjectGraphGenerator> predicateObjectGraphGenerators;
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public MappingFactory(FunctionLoader functionLoader) {
        this.functionLoader = functionLoader;
    }

    public Mapping createMapping(Term triplesMap, QuadStore store) throws IOException {
        this.triplesMap = triplesMap;
        this.store = store;
        this.subject = null;
        this.predicateObjectGraphGenerators = new ArrayList<>();
        this.graphs = null;

        parseSubjectMap();
        parsePredicateObjectMaps();

        //return the mapping
        return new Mapping(subject, predicateObjectGraphGenerators, graphs);
    }

    private void parseSubjectMap() throws IOException {
        if (this.subject == null) {
            List<Term> subjectmaps = Utils.getObjectsFromQuads(store.getQuads(triplesMap, new NamedNode(NAMESPACES.RR + "subjectMap"), null));

            if (!subjectmaps.isEmpty()) {
                if (subjectmaps.size() > 1) {
                    logger.warn(triplesMap + " has " + subjectmaps.size() + "Subject Maps. You can only have one. A random one is taken.");
                }

                Term subjectmap = subjectmaps.get(0);
                List<Term> functionValues =  Utils.getObjectsFromQuads(store.getQuads(subjectmap, new NamedNode(NAMESPACES.FNML + "functionValue"), null));

                if (functionValues.isEmpty()) {
                    List<Term> termTypes = Utils.getObjectsFromQuads(store.getQuads(subjectmap, new NamedNode(NAMESPACES.RR  + "termType"), null));

                    //checking if we are dealing with a Blank Node as subject
                    if (!termTypes.isEmpty() && termTypes.get(0).equals(new NamedNode(NAMESPACES.RR  + "BlankNode"))) {
                        SingleRecordFunctionExecutor executor = RecordFunctionExecutorFactory.generate(store, subjectmap, true);

                        if (executor != null) {
                            this.subject = new BlankNodeGenerator(executor);
                        } else {
                            this.subject = new BlankNodeGenerator();
                        }
                    } else {
                        //we are not dealing with a Blank Node, so we create the template
                        this.subject = new NamedNodeGenerator(RecordFunctionExecutorFactory.generate(store, subjectmap, true));
                    }
                } else {
                    SingleRecordFunctionExecutor functionExecutor = parseFunctionTermMap(functionValues.get(0));

                    this.subject = new NamedNodeGenerator(functionExecutor);
                }

                this.graphs = parseGraphMapsAndShortcuts(subjectmap);

                //get classes
                List<Term> classes = Utils.getObjectsFromQuads(store.getQuads(subjectmap, new NamedNode(NAMESPACES.RR  + "class"), null));

                //we create predicateobjects for the classes
                for (Term c: classes) {
                    // Don't put in graph for rr:class, subject is already put in graph, otherwise double export
                    NamedNodeGenerator predicateGenerator = new NamedNodeGenerator(new ConstantExtractor(NAMESPACES.RDF + "type"));
                    NamedNodeGenerator objectGenerator = new NamedNodeGenerator(new ConstantExtractor(c.getValue()));
                    predicateObjectGraphGenerators.add(new PredicateObjectGraphGenerator(predicateGenerator, objectGenerator, null));
                }
            } else {
                throw new Error(triplesMap + " has no Subject Map. Each Triples Map should have exactly one Subject Map.");
            }
        }
    }

    private void parsePredicateObjectMaps() throws IOException {
        List<Term> predicateobjectmaps = Utils.getObjectsFromQuads(store.getQuads(triplesMap, new NamedNode(NAMESPACES.RR + "predicateObjectMap"), null));

        for (Term pom : predicateobjectmaps) {
            List<TermGenerator> predicateGenerators = parsePredicateMapsAndShortcuts(pom);
            List<TermGenerator> graphGenerators = parseGraphMapsAndShortcuts(pom);

            parseObjectMapsAndShortcutsAndGeneratePOGGenerators(pom, predicateGenerators, graphGenerators);
        }
    }

    private void parseObjectMapsAndShortcutsAndGeneratePOGGenerators(Term termMap, List<TermGenerator> predicateGenerators, List<TermGenerator> graphGenerators) throws IOException {
        parseObjectMapsAndShortcutsWithCallback(termMap, (oGen, childOrParent) -> {
            predicateGenerators.forEach(pGen -> {
                if (graphGenerators.isEmpty()) {
                    predicateObjectGraphGenerators.add(new PredicateObjectGraphGenerator(pGen, oGen, null));
                } else {
                    graphGenerators.forEach(gGen -> {
                        predicateObjectGraphGenerators.add(new PredicateObjectGraphGenerator(pGen, oGen, gGen));
                    });
                }
            });
        }, (parentTriplesMap, joinConditionFunctionExecutors) -> {
            predicateGenerators.forEach(pGen -> {
                List<PredicateObjectGraphGenerator> pos = getPredicateObjectGraphGeneratorFromMultipleGraphGenerators(pGen, null, graphGenerators);

                pos.forEach(pogGen -> {
                    pogGen.setParentTriplesMap(parentTriplesMap);

                    joinConditionFunctionExecutors.forEach(jcfe -> {
                        pogGen.addJoinCondition(jcfe);
                    });

                    predicateObjectGraphGenerators.add(pogGen);
                });
            });
        });
    }

    private void parseObjectMapsAndShortcutsWithCallback(Term termMap, BiConsumer<TermGenerator, String> objectMapCallback, BiConsumer<Term, List<MultipleRecordsFunctionExecutor>> refObjectMapCallback) throws IOException {
        List<Term> objectmaps = Utils.getObjectsFromQuads(store.getQuads(termMap, new NamedNode(NAMESPACES.RR + "objectMap"), null));

        for (Term objectmap : objectmaps) {
            parseObjectMapWithCallback(objectmap, objectMapCallback, refObjectMapCallback);
        }

        //dealing with rr:object
        List<Term> objectsConstants = Utils.getObjectsFromQuads(store.getQuads(termMap, new NamedNode(NAMESPACES.RR + "object"), null));

        for (Term o : objectsConstants) {
            TermGenerator gen;
            SingleRecordFunctionExecutor fn = new ConstantExtractor(o.getValue());

            if (o instanceof Literal) {
                gen = new LiteralGenerator(fn);
            } else {
                gen = new NamedNodeGenerator(fn);
            }

            objectMapCallback.accept(gen, "child");
        }
    }

    private void parseObjectMapWithCallback(Term objectmap, BiConsumer<TermGenerator, String> objectMapCallback, BiConsumer<Term, List<MultipleRecordsFunctionExecutor>> refObjectMapCallback) throws IOException {
        List<Term> functionValues = Utils.getObjectsFromQuads(store.getQuads(objectmap, new NamedNode(NAMESPACES.FNML + "functionValue"), null));
        Term termType = getTermType(objectmap);

        List<Term> datatypes = Utils.getObjectsFromQuads(store.getQuads(objectmap, new NamedNode(NAMESPACES.RR + "datatype"), null));
        List<Term> languages = Utils.getObjectsFromQuads(store.getQuads(objectmap, new NamedNode(NAMESPACES.RR + "language"), null));
        List<Term> parentTriplesMaps = Utils.getObjectsFromQuads(store.getQuads(objectmap, new NamedNode(NAMESPACES.RR + "parentTriplesMap"), null));
        List<Term> parentTermMaps = Utils.getObjectsFromQuads(store.getQuads(objectmap, new NamedNode(NAMESPACES.RML + "parentTermMap"), null));

        if (functionValues.isEmpty()) {
            SingleRecordFunctionExecutor executor = RecordFunctionExecutorFactory.generate(store, objectmap, false);

            if (executor != null) {
                TermGenerator oGen;

                if (termType.equals(new NamedNode(NAMESPACES.RR + "Literal"))) {
                    //check if we need to apply a datatype to the object
                    if (!datatypes.isEmpty()) {
                        oGen = new LiteralGenerator(executor, datatypes.get(0));
                        //check if we need to apply a language to the object
                    } else if (!languages.isEmpty()) {
                        oGen = new LiteralGenerator(executor, languages.get(0).getValue());
                    } else {
                        oGen = new LiteralGenerator(executor);
                    }
                } else {
                    oGen = new NamedNodeGenerator(executor);
                }

                objectMapCallback.accept(oGen, "child");
            } else if (! parentTriplesMaps.isEmpty()) {
                if (parentTriplesMaps.size() > 1) {
                    logger.warn(triplesMap + " has " + parentTriplesMaps.size() + " Parent Triples Maps. You can only have one. A random one is taken.");
                }

                Term parentTriplesMap = parentTriplesMaps.get(0);

                List<Term> rrJoinConditions = Utils.getObjectsFromQuads(store.getQuads(objectmap, new NamedNode(NAMESPACES.RR + "joinCondition"), null));
                List<Term> rmljoinConditions = Utils.getObjectsFromQuads(store.getQuads(objectmap, new NamedNode(NAMESPACES.RML + "joinCondition"), null));
                ArrayList<MultipleRecordsFunctionExecutor> joinConditionFunctionExecutors = new ArrayList<>();

                for (Term joinCondition : rrJoinConditions) {

                    List<String> parents = Utils.getLiteralObjectsFromQuads(store.getQuads(joinCondition, new NamedNode(NAMESPACES.RR + "parent"), null));
                    List<String> childs = Utils.getLiteralObjectsFromQuads(store.getQuads(joinCondition, new NamedNode(NAMESPACES.RR + "child"), null));

                    if (parents.isEmpty()) {
                        throw new Error("One of the join conditions of " + triplesMap + " is missing rr:parent.");
                    } else if (childs.isEmpty()) {
                        throw new Error("One of the join conditions of " + triplesMap + " is missing rr:child.");
                    } else {
                        FunctionModel equal = functionLoader.getFunction(new NamedNode("http://example.com/idlab/function/equal"));
                        Map<String, Object[]> parameters = new HashMap<>();

                        SingleRecordFunctionExecutor parent = new ReferenceExtractor(parents.get(0));
                        Object[] detailsParent = {"parent", parent};
                        parameters.put("http://users.ugent.be/~bjdmeest/function/grel.ttl#valueParameter", detailsParent);

                        SingleRecordFunctionExecutor child = new ReferenceExtractor(childs.get(0));
                        Object[] detailsChild = {"child", child};
                        parameters.put("http://users.ugent.be/~bjdmeest/function/grel.ttl#valueParameter2", detailsChild);

                        joinConditionFunctionExecutors.add(new StaticMultipleRecordsFunctionExecutor(equal, parameters));
                    }
                }

                for (Term joinCondition: rmljoinConditions) {
                    Term functionValue = Utils.getObjectsFromQuads(store.getQuads(joinCondition, new NamedNode(NAMESPACES.FNML + "functionValue"), null)).get(0);
                    joinConditionFunctionExecutors.add(parseJoinConditionFunctionTermMap(functionValue));
                }

                if (refObjectMapCallback != null) {
                    refObjectMapCallback.accept(parentTriplesMap, joinConditionFunctionExecutors);
                }
            } else if (!parentTermMaps.isEmpty()) {
                parseObjectMapWithCallback(parentTermMaps.get(0), (objectGenerator, childOrParent) -> {
                    objectMapCallback.accept(objectGenerator, "parent");
                }, null);
            }
        } else {
            SingleRecordFunctionExecutor functionExecutor = parseFunctionTermMap(functionValues.get(0));
            TermGenerator gen;

            //TODO is literal the default?
            if (termType == null || termType.equals( new NamedNode(NAMESPACES.RR + "Literal"))) {
                //check if we need to apply a datatype to the object
                if (!datatypes.isEmpty()) {
                    gen = new LiteralGenerator(functionExecutor, datatypes.get(0));
                    //check if we need to apply a language to the object
                } else if (!languages.isEmpty()) {
                    gen = new LiteralGenerator(functionExecutor, languages.get(0).getValue());
                } else {
                    gen = new LiteralGenerator(functionExecutor);
                }
            } else {
                gen = new NamedNodeGenerator(functionExecutor);
            }

            objectMapCallback.accept(gen, "child");
        }
    }

    private List<TermGenerator> parseGraphMapsAndShortcuts(Term termMap) throws IOException {
        ArrayList<TermGenerator> graphs = new ArrayList<>();

        List<Term> graphMaps = Utils.getObjectsFromQuads(store.getQuads(termMap, new NamedNode(NAMESPACES.RR + "graphMap"), null));

        for (Term graphMap : graphMaps) {
            List<Term> functionValues = Utils.getObjectsFromQuads(store.getQuads(graphMap, new NamedNode(NAMESPACES.FNML + "functionValue"), null));
            List<Term> termTypes = Utils.getObjectsFromQuads(store.getQuads(graphMap, new NamedNode(NAMESPACES.RR + "termType"), null));
            Term termType = null;

            if (!termTypes.isEmpty()) {
                termType = termTypes.get(0);
            }

            if (functionValues.isEmpty()) {
                SingleRecordFunctionExecutor executor = RecordFunctionExecutorFactory.generate(store, graphMap, true);

                if (termType == null || termType.equals(new NamedNode(NAMESPACES.RR + "IRI"))) {
                    graphs.add(new NamedNodeGenerator(executor));
                } else {
                    if (executor == null) {
                        graphs.add(new BlankNodeGenerator());
                    } else {
                        graphs.add(new BlankNodeGenerator(executor));
                    }
                }
            } else {
                SingleRecordFunctionExecutor functionExecutor = parseFunctionTermMap(functionValues.get(0));

                if (termType == null || termType.equals(new NamedNode(NAMESPACES.RR + "IRI"))) {
                    graphs.add(new NamedNodeGenerator(functionExecutor));
                } else {
                    graphs.add(new BlankNodeGenerator(functionExecutor));
                }
            }
        }

        List<Term> graphShortcuts = Utils.getObjectsFromQuads(store.getQuads(termMap, new NamedNode(NAMESPACES.RR + "graph"), null));

        for (Term graph : graphShortcuts) {
            String gStr = graph.getValue();
            graphs.add(new NamedNodeGenerator(new ConstantExtractor(gStr)));
        }

        return graphs;
    }

    private List<TermGenerator> parsePredicateMapsAndShortcuts(Term termMap) throws IOException {
        ArrayList<TermGenerator> predicates = new ArrayList<>();

        List<Term> predicateMaps = Utils.getObjectsFromQuads(store.getQuads(termMap, new NamedNode(NAMESPACES.RR + "predicateMap"), null));

        for (Term predicateMap : predicateMaps) {
            List<Term> functionValues = Utils.getObjectsFromQuads(store.getQuads(predicateMap, new NamedNode(NAMESPACES.FNML + "functionValue"), null));

            if (functionValues.isEmpty()) {
                predicates.add(new NamedNodeGenerator(RecordFunctionExecutorFactory.generate(store, predicateMap, false)));
            } else {
                SingleRecordFunctionExecutor functionExecutor = parseFunctionTermMap(functionValues.get(0));

                predicates.add(new NamedNodeGenerator(functionExecutor));
            }
        }

        List<Term> predicateShortcuts = Utils.getObjectsFromQuads(store.getQuads(termMap, new NamedNode(NAMESPACES.RR + "predicate"), null));

        for (Term predicate : predicateShortcuts) {
            String pStr = predicate.getValue();
            predicates.add(new NamedNodeGenerator(new ConstantExtractor(pStr)));
        }

        return predicates;
    }

    private SingleRecordFunctionExecutor parseFunctionTermMap(Term functionValue) throws IOException {
        List<Term> functionPOMs = Utils.getObjectsFromQuads(store.getQuads(functionValue, new NamedNode(NAMESPACES.RR + "predicateObjectMap"), null));
        ArrayList<ParameterValuePair> params = new ArrayList<>();

        for (Term pom : functionPOMs) {
            List<TermGenerator> predicateGenerators = parsePredicateMapsAndShortcuts(pom);
            List<TermGenerator> objectGenerators = parseObjectMapsAndShortcuts(pom);

            params.add(new ParameterValuePair(predicateGenerators, objectGenerators));
        }

        return new DynamicSingleRecordFunctionExecutor(params, functionLoader);
    }

    private MultipleRecordsFunctionExecutor parseJoinConditionFunctionTermMap(Term functionValue) throws IOException {
        List<Term> functionPOMs = Utils.getObjectsFromQuads(store.getQuads(functionValue, new NamedNode(NAMESPACES.RR + "predicateObjectMap"), null));
        ArrayList<ParameterValueOriginPair> params = new ArrayList<>();

        for (Term pom : functionPOMs) {
            List<TermGenerator> predicateGenerators = parsePredicateMapsAndShortcuts(pom);
            ArrayList<TermGeneratorOriginPair> objectGeneratorOriginPairs = new ArrayList<>();

            parseObjectMapsAndShortcutsWithCallback(pom, (oGen, childOrParent) -> {
                objectGeneratorOriginPairs.add(new TermGeneratorOriginPair(oGen, childOrParent));
            }, null);

            params.add(new ParameterValueOriginPair(predicateGenerators, objectGeneratorOriginPairs));
        }

        return new DynamicMultipleRecordsFunctionExecutor(params, functionLoader);
    }

    private List<TermGenerator> parseObjectMapsAndShortcuts(Term pom) throws IOException {
        ArrayList<TermGenerator> generators = new ArrayList<>();

        parseObjectMapsAndShortcutsWithCallback(pom, (termGenerator, childOrParent) -> {
            generators.add(termGenerator);
        }, (term, joinConditionFunctions) -> {});

        return generators;
    }

    /**
     * This method returns the TermType of a given Term Map.
     * If no Term Type is found, a default Term Type is return based on the R2RML specification.
     **/
    private Term getTermType(Term map) {
        List<Term> references = Utils.getObjectsFromQuads(store.getQuads(map, new NamedNode(NAMESPACES.RML + "reference"), null));
        List<Term> templates = Utils.getObjectsFromQuads(store.getQuads(map, new NamedNode(NAMESPACES.RR  + "template"), null));
        List<Term> constants = Utils.getObjectsFromQuads(store.getQuads(map, new NamedNode(NAMESPACES.RR  + "constant"), null));
        List<Term> termTypes = Utils.getObjectsFromQuads(store.getQuads(map, new NamedNode(NAMESPACES.RR  + "termType"), null));

        Term termType = null;

        if (!termTypes.isEmpty()) {
            termType = termTypes.get(0);
        } else {
            if (!references.isEmpty()) {
                termType = new NamedNode(NAMESPACES.RR + "Literal");
            } else if (!templates.isEmpty()) {
                termType = new NamedNode(NAMESPACES.RR + "IRI");
            } else if (!constants.isEmpty()) {
                termType = new NamedNode(NAMESPACES.RR + "Literal");
            }
        }

        return termType;
    }

    private List<PredicateObjectGraphGenerator> getPredicateObjectGraphGeneratorFromMultipleGraphGenerators(TermGenerator pGen, TermGenerator oGen, List<TermGenerator> gGens) {
        ArrayList<PredicateObjectGraphGenerator> list = new ArrayList<>();

        gGens.forEach(gGen -> {
            list.add(new PredicateObjectGraphGenerator(pGen, oGen, gGen));
        });

        if (gGens.isEmpty()) {
            list.add(new PredicateObjectGraphGenerator(pGen, oGen, null));
        }

        return list;
    }

}

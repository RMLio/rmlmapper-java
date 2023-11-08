package be.ugent.rml;

import be.ugent.idlab.knows.functions.agent.Agent;
import be.ugent.rml.extractor.ConstantExtractor;
import be.ugent.rml.extractor.HashExtractor;
import be.ugent.rml.extractor.ReferenceExtractor;
import be.ugent.rml.functions.*;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.termgenerator.BlankNodeGenerator;
import be.ugent.rml.termgenerator.LiteralGenerator;
import be.ugent.rml.termgenerator.NamedNodeGenerator;
import be.ugent.rml.termgenerator.TermGenerator;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static be.ugent.rml.Utils.isValidrrLanguage;

public class MappingFactory {

    private static final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final Agent functionAgent;
    private MappingInfo subjectMappingInfo;
    private List<MappingInfo> graphMappingInfos;
    private Value triplesMap;
    private QuadStore store;
    private List<PredicateObjectGraphMapping> predicateObjectGraphMappings;
    // This boolean is true when the double in a reference need to be ignored.
    // For example, when accessing data in a RDB.
    private boolean ignoreDoubleQuotes;

    // Base IRI to prepend to a relative IRI to make it absolute.
    private final String baseIRI;

    // StrictMode determines RMLMapper's behaviour when an IRI for a NamedNode is invalid.
    // If set to BEST_EFFORT, RMLMapper will not generate a NamedNode and go on.
    // If set to STRICT, RMLMapper will stop execution with an exception.
    private final StrictMode strictMode;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public MappingFactory(final Agent functionAgent, final String baseIRI, final StrictMode strictMode) {
        this.functionAgent = functionAgent;
        this.baseIRI = baseIRI;
        this.strictMode = strictMode;
    }

    public Mapping createMapping(Value triplesMap, QuadStore store) throws Exception {
        this.triplesMap = triplesMap;
        this.store = store;
        this.subjectMappingInfo = null;
        this.predicateObjectGraphMappings = new ArrayList<>();
        this.graphMappingInfos = null;
        this.ignoreDoubleQuotes = this.areDoubleQuotesIgnored(store, triplesMap);

        parseSubjectMap();
        parsePredicateObjectMaps();
        graphMappingInfos = parseGraphMapsAndShortcuts(subjectMappingInfo.getTerm());


        //return the mapping
        return new Mapping(subjectMappingInfo, predicateObjectGraphMappings, graphMappingInfos);
    }

    private void parseSubjectMap() throws Exception {
        if (this.subjectMappingInfo == null) {
            TermGenerator generator;
            List<Value> subjectmaps = Utils.getObjectsFromQuads(store.getQuads(triplesMap, valueFactory.createIRI(NAMESPACES.RR + "subjectMap"), null));

            if (!subjectmaps.isEmpty()) {
                if (subjectmaps.size() > 1) {
                    throw new Exception(String.format("%s has %d Subject Maps. You can only have one.", triplesMap, subjectmaps.size()));
                }

                Value subjectmap = subjectmaps.get(0);
                List<Value> functionValues = Utils.getObjectsFromQuads(store.getQuads(subjectmap, valueFactory.createIRI(NAMESPACES.FNML + "functionValue"), null));
                List<Value> termTypes = Utils.getObjectsFromQuads(store.getQuads(subjectmap, valueFactory.createIRI(NAMESPACES.RR + "termType"), null));

                if (termTypes.contains(valueFactory.createIRI(NAMESPACES.RR + "Literal"))) {
                    throw new Exception(triplesMap + " is a Literal Term Map. Accepted term types for Subject Maps are: IRI, Blank Node");
                }

                boolean isBlankNode = !termTypes.isEmpty() && termTypes.get(0).equals(valueFactory.createIRI(NAMESPACES.RR  + "BlankNode"));

                if (functionValues.isEmpty()) {
                    //checking if we are dealing with a Blank Node as subject
                    if (isBlankNode) {
                        SingleRecordFunctionExecutor executor = RecordFunctionExecutorFactory.generate(store, subjectmap, true, ignoreDoubleQuotes);

                        if (executor != null) {
                            generator = new BlankNodeGenerator(executor);
                        } else {
                            generator = new BlankNodeGenerator();
                        }
                    } else {
                        //we are not dealing with a Blank Node, so we create the template
                        generator = new NamedNodeGenerator(RecordFunctionExecutorFactory.generate(store, subjectmap, true, ignoreDoubleQuotes), baseIRI, strictMode);
                    }
                } else {
                    SingleRecordFunctionExecutor functionExecutor = parseFunctionTermMap(functionValues.get(0));

                    if (isBlankNode) {
                        generator = new BlankNodeGenerator(functionExecutor);
                    } else {
                        generator = new NamedNodeGenerator(functionExecutor, baseIRI, strictMode);
                    }
                }

                // get targets for subject
                List<Value> targets = Utils.getObjectsFromQuads(store.getQuads(subjectmap, valueFactory.createIRI(NAMESPACES.RML + "logicalTarget"), null));

                this.subjectMappingInfo = new MappingInfo(subjectmap, generator, targets);

                //get classes
                List<Value> classes = Utils.getObjectsFromQuads(store.getQuads(subjectmap, valueFactory.createIRI(NAMESPACES.RR + "class"), null));

                //we create predicateobjects for the classes
                for (Value c : classes) {
                    /*
                     * Don't put in graph for rr:class, subject is already put in graph, otherwise double export.
                     * Same holds for targets, the rdf:type triple will be exported to the subject target already.
                     */
                    NamedNodeGenerator predicateGenerator = new NamedNodeGenerator(new ConstantExtractor(NAMESPACES.RDF + "type"), baseIRI, strictMode);
                    NamedNodeGenerator objectGenerator = new NamedNodeGenerator(new ConstantExtractor(c.stringValue()), baseIRI, strictMode);
                    predicateObjectGraphMappings.add(new PredicateObjectGraphMapping(
                            new MappingInfo(subjectmap, predicateGenerator),
                            new MappingInfo(subjectmap, objectGenerator),
                            null, null));
                }
            } else {
                throw new Exception(triplesMap + " has no Subject Map. Each Triples Map should have exactly one Subject Map.");
            }
        }
    }

    private void parsePredicateObjectMaps() throws Exception {
        List<Value> predicateobjectmaps = Utils.getObjectsFromQuads(store.getQuads(triplesMap, valueFactory.createIRI(NAMESPACES.RR + "predicateObjectMap"), null));

        for (Value pom : predicateobjectmaps) {
            List<MappingInfo> predicateMappingInfos = parsePredicateMapsAndShortcuts(pom);
            List<MappingInfo> graphMappingInfos = parseGraphMapsAndShortcuts(pom);

            parseObjectMapsAndShortcutsAndGeneratePOGGenerators(pom, predicateMappingInfos, graphMappingInfos);
        }
    }

    private void parseObjectMapsAndShortcutsAndGeneratePOGGenerators(Value termMap, List<MappingInfo> predicateMappingInfos, List<MappingInfo> graphMappingInfos) throws IOException {
        parseObjectMapsAndShortcutsWithCallback(termMap, (oMappingInfo, childOrParent) -> {
            MappingInfo lMappingInfo = parseLanguageMappingInfo(oMappingInfo.getTerm());

            predicateMappingInfos.forEach(pMappingInfo -> {
                if (graphMappingInfos.isEmpty()) {
                    predicateObjectGraphMappings.add(new PredicateObjectGraphMapping(pMappingInfo, oMappingInfo, null, lMappingInfo));
                } else {
                    graphMappingInfos.forEach(gMappingInfo -> {
                        predicateObjectGraphMappings.add(new PredicateObjectGraphMapping(pMappingInfo, oMappingInfo, gMappingInfo, lMappingInfo));
                    });
                }
            });
        }, (parentTriplesMap, joinConditionFunctionExecutors) -> {
            predicateMappingInfos.forEach(pMappingInfo -> {
                List<PredicateObjectGraphMapping> pos = getPredicateObjectGraphMappingFromMultipleGraphMappingInfos(pMappingInfo, null, graphMappingInfos);

                pos.forEach(pogMappingInfo -> {
                    pogMappingInfo.setParentTriplesMap(parentTriplesMap);

                    joinConditionFunctionExecutors.forEach(jcfe -> {
                        pogMappingInfo.addJoinCondition(jcfe);
                    });

                    predicateObjectGraphMappings.add(pogMappingInfo);
                });
            });
        });
    }

    private void parseObjectMapsAndShortcutsWithCallback(Value termMap, BiConsumer<MappingInfo, String> objectMapCallback, BiConsumer<Value, List<MultipleRecordsFunctionExecutor>> refObjectMapCallback) throws IOException {
        List<Value> objectmaps = Utils.getObjectsFromQuads(store.getQuads(termMap, valueFactory.createIRI(NAMESPACES.RR + "objectMap"), null));

        for (Value objectmap : objectmaps) {
            parseObjectMapWithCallback(objectmap, objectMapCallback, refObjectMapCallback);
        }

        //dealing with rr:object
        List<Value> objectsConstants = Utils.getObjectsFromQuads(store.getQuads(termMap, valueFactory.createIRI(NAMESPACES.RR + "object"), null));

        for (Value o : objectsConstants) {
            TermGenerator gen;
            SingleRecordFunctionExecutor fn = new ConstantExtractor(o.stringValue());

            if (o.isLiteral()) {
                gen = new LiteralGenerator(fn);
            } else {
                gen = new NamedNodeGenerator(fn, baseIRI, strictMode);
            }

            // rr:object shortcut can never have targets
            objectMapCallback.accept(new MappingInfo(termMap, gen), "child");
        }
    }

    private void parseObjectMapWithCallback(Value objectmap, BiConsumer<MappingInfo, String> objectMapCallback, BiConsumer<Value, List<MultipleRecordsFunctionExecutor>> refObjectMapCallback) throws IOException {
        List<Value> functionValues = Utils.getObjectsFromQuads(store.getQuads(objectmap, valueFactory.createIRI(NAMESPACES.FNML + "functionValue"), null));
        Value termType = getTermType(objectmap, true);

        List<Value> datatypes = Utils.getObjectsFromQuads(store.getQuads(objectmap, valueFactory.createIRI(NAMESPACES.RR + "datatype"), null));
        List<Value> parentTriplesMaps = Utils.getObjectsFromQuads(store.getQuads(objectmap, valueFactory.createIRI(NAMESPACES.RR + "parentTriplesMap"), null));
        List<Value> parentTermMaps = Utils.getObjectsFromQuads(store.getQuads(objectmap, valueFactory.createIRI(NAMESPACES.RML + "parentTermMap"), null));

        List<SingleRecordFunctionExecutor> languages = getLanguageExecutorsForObjectMap(objectmap);

        if (functionValues.isEmpty()) {
            boolean encodeIRI = termType != null && termType.stringValue().equals(NAMESPACES.RR + "IRI");
            SingleRecordFunctionExecutor executor = RecordFunctionExecutorFactory.generate(store, objectmap, encodeIRI, ignoreDoubleQuotes);

            if (parentTriplesMaps.isEmpty() && parentTermMaps.isEmpty()) {
                TermGenerator oGen;

                if (termType.equals(valueFactory.createIRI(NAMESPACES.RR + "Literal"))) {
                    //check if we need to apply a datatype to the object
                    if (!datatypes.isEmpty()) {
                        oGen = new LiteralGenerator(executor, datatypes.get(0));
                        //check if we need to apply a language to the object
                    } else if (!languages.isEmpty()) {
                        oGen = new LiteralGenerator(executor, languages.get(0));
                    } else {
                        oGen = new LiteralGenerator(executor);
                    }
                } else if (termType.equals(valueFactory.createIRI(NAMESPACES.RR + "IRI"))) {
                    oGen = new NamedNodeGenerator(executor, baseIRI, strictMode);
                } else {
                    if (executor == null) {
                        // This will generate Blank Node with random identifiers.
                        oGen = new BlankNodeGenerator();
                    } else {
                        oGen = new BlankNodeGenerator(executor);
                    }
                }

                // get language maps targets for object map
                // TODO why is this here?
                MappingInfo languageMapInfo = null;
                List<Value> languageMaps = Utils.getObjectsFromQuads(store.getQuads(objectmap, valueFactory.createIRI(NAMESPACES.RML + "languageMap"), null));

                // get targets for object map
                List<Value> oTargets = Utils.getObjectsFromQuads(store.getQuads(objectmap, valueFactory.createIRI(NAMESPACES.RML + "logicalTarget"), null));

                objectMapCallback.accept(new MappingInfo(objectmap, oGen, oTargets), "child");
            } else if (!parentTriplesMaps.isEmpty()) {
                if (parentTriplesMaps.size() > 1) {
                    logger.warn("{} has {} Parent Triples Maps. You can only have one. A random one is taken.", triplesMap, parentTriplesMaps.size());
                }

                Value parentTriplesMap = parentTriplesMaps.get(0);

                List<Value> rrJoinConditions = Utils.getObjectsFromQuads(store.getQuads(objectmap, valueFactory.createIRI(NAMESPACES.RR + "joinCondition"), null));
                List<Value> rmljoinConditions = Utils.getObjectsFromQuads(store.getQuads(objectmap, valueFactory.createIRI(NAMESPACES.RML + "joinCondition"), null));
                ArrayList<MultipleRecordsFunctionExecutor> joinConditionFunctionExecutors = new ArrayList<>();

                for (Value joinCondition : rrJoinConditions) {

                    List<String> parents = Utils.getLiteralObjectsFromQuads(store.getQuads(joinCondition, valueFactory.createIRI(NAMESPACES.RR + "parent"), null));
                    List<String> childs = Utils.getLiteralObjectsFromQuads(store.getQuads(joinCondition, valueFactory.createIRI(NAMESPACES.RR + "child"), null));

                    if (parents.isEmpty()) {
                        throw new Error("One of the join conditions of " + triplesMap + " is missing rr:parent.");
                    } else if (childs.isEmpty()) {
                        throw new Error("One of the join conditions of " + triplesMap + " is missing rr:child.");
                    } else {
                        Map<String, Object[]> parameters = new HashMap<>();

                        boolean ignoreDoubleQuotesInParent = this.areDoubleQuotesIgnored(store, parentTriplesMap);
                        SingleRecordFunctionExecutor parent = new ReferenceExtractor(parents.get(0), ignoreDoubleQuotesInParent);
                        Object[] detailsParent = {"parent", parent};
                        parameters.put("http://users.ugent.be/~bjdmeest/function/grel.ttl#valueParameter", detailsParent);

                        SingleRecordFunctionExecutor child = new ReferenceExtractor(childs.get(0), ignoreDoubleQuotes);
                        Object[] detailsChild = {"child", child};
                        parameters.put("http://users.ugent.be/~bjdmeest/function/grel.ttl#valueParameter2", detailsChild);

                        joinConditionFunctionExecutors.add(new StaticMultipleRecordsFunctionExecutor(parameters, functionAgent, "http://example.com/idlab/function/equal"));
                    }
                }

                for (Value joinCondition : rmljoinConditions) {
                    Value functionValue = Utils.getObjectsFromQuads(store.getQuads(joinCondition, valueFactory.createIRI(NAMESPACES.FNML + "functionValue"), null)).get(0);
                    joinConditionFunctionExecutors.add(parseJoinConditionFunctionTermMap(functionValue));
                }

                // get logical source of parentTriplesMap
                List<Value> logicalSources = Utils.getObjectsFromQuads(store.getQuads(this.triplesMap, valueFactory.createIRI(NAMESPACES.RML + "logicalSource"), null));
                Value logicalSource = null;
                if (!logicalSources.isEmpty()) {
                    logicalSource = logicalSources.get(0);
                }

                List<Value> parentLogicalSources = Utils.getObjectsFromQuads(store.getQuads(parentTriplesMap, valueFactory.createIRI(NAMESPACES.RML + "logicalSource"), null));
                Value parentLogicalSource = null;
                if (!parentLogicalSources.isEmpty()) {
                    parentLogicalSource = parentLogicalSources.get(0);
                }
                // Check if there is at least one Logical Source.
                // If logical sources are the same (i.e., have the same IRI): the condition is 'join on same record'
                if (logicalSource.equals(parentLogicalSource) && rrJoinConditions.isEmpty() && rmljoinConditions.isEmpty()) {
                    // TODO this is a _WILDLY_ inefficient way of handling this: a join is still executed, but then on the hashcode of the record.
                    // I'm not sure what a more elegant solution would entail in the current architecture: joins are currently hard to optimize
                    joinConditionFunctionExecutors.add(generateSameLogicalSourceJoinConditionFunctionTermMap());
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
            if (termType == null || termType.equals(valueFactory.createIRI(NAMESPACES.RR + "Literal"))) {
                //check if we need to apply a datatype to the object
                if (!datatypes.isEmpty()) {
                    gen = new LiteralGenerator(functionExecutor, datatypes.get(0));
                    //check if we need to apply a language to the object
                } else if (!languages.isEmpty()) {
                    gen = new LiteralGenerator(functionExecutor, languages.get(0));
                } else {
                    gen = new LiteralGenerator(functionExecutor);
                }
            } else {
                gen = new NamedNodeGenerator(functionExecutor, baseIRI, strictMode);
            }

            // get targets for object map
            List<Value> targets = Utils.getObjectsFromQuads(store.getQuads(objectmap, valueFactory.createIRI(NAMESPACES.RML + "logicalTarget"), null));

            objectMapCallback.accept(new MappingInfo(objectmap, gen, targets), "child");
        }
    }

    private List<MappingInfo> parseGraphMapsAndShortcuts(Value termMap) throws Exception {
        ArrayList<MappingInfo> graphMappingInfos = new ArrayList<>();

        List<Value> graphMaps = Utils.getObjectsFromQuads(store.getQuads(termMap, valueFactory.createIRI(NAMESPACES.RR + "graphMap"), null));

        for (Value graphMap : graphMaps) {
            List<Value> functionValues = Utils.getObjectsFromQuads(store.getQuads(graphMap, valueFactory.createIRI(NAMESPACES.FNML + "functionValue"), null));
            List<Value> termTypes = Utils.getObjectsFromQuads(store.getQuads(graphMap, valueFactory.createIRI(NAMESPACES.RR + "termType"), null));
            Value termType = null;

            if (!termTypes.isEmpty()) {
                termType = termTypes.get(0);

                if (termType.equals(valueFactory.createIRI(NAMESPACES.RR + "Literal"))) {
                    throw new Exception("A Graph Map cannot generate literals.");
                }
            }

            TermGenerator generator;

            if (functionValues.isEmpty()) {
                SingleRecordFunctionExecutor executor = RecordFunctionExecutorFactory.generate(store, graphMap, true, ignoreDoubleQuotes);

                if (termType == null || termType.equals(valueFactory.createIRI(NAMESPACES.RR + "IRI"))) {
                    generator = new NamedNodeGenerator(executor, baseIRI, strictMode);
                } else {
                    if (executor == null) {
                        generator = new BlankNodeGenerator();
                    } else {
                        generator = new BlankNodeGenerator(executor);
                    }
                }
            } else {
                SingleRecordFunctionExecutor functionExecutor = parseFunctionTermMap(functionValues.get(0));

                if (termType == null || termType.equals(valueFactory.createIRI(NAMESPACES.RR + "IRI"))) {
                    generator = new NamedNodeGenerator(functionExecutor, baseIRI, strictMode);
                } else {
                    generator = new BlankNodeGenerator(functionExecutor);
                }
            }

            // get targets for graph maps
            List<Value> targets = Utils.getObjectsFromQuads(store.getQuads(graphMap, valueFactory.createIRI(NAMESPACES.RML + "logicalTarget"), null));

            graphMappingInfos.add(new MappingInfo(termMap, generator, targets));
        }

        List<Value> graphShortcuts = Utils.getObjectsFromQuads(store.getQuads(termMap, valueFactory.createIRI(NAMESPACES.RR + "graph"), null));

        for (Value graph : graphShortcuts) {
            String gStr = graph.stringValue();
            // rr:graph shortcut can never have targets
            graphMappingInfos.add(new MappingInfo(termMap, new NamedNodeGenerator(new ConstantExtractor(gStr), baseIRI, strictMode)));
        }

        return graphMappingInfos;
    }

    private List<MappingInfo> parsePredicateMapsAndShortcuts(Value termMap) throws IOException {
        ArrayList<MappingInfo> predicateMappingInfos = new ArrayList<>();

        List<Value> predicateMaps = Utils.getObjectsFromQuads(store.getQuads(termMap, valueFactory.createIRI(NAMESPACES.RR + "predicateMap"), null));

        for (Value predicateMap : predicateMaps) {
            // get functionValue for predicate maps
            List<Value> functionValues = Utils.getObjectsFromQuads(store.getQuads(predicateMap, valueFactory.createIRI(NAMESPACES.FNML + "functionValue"), null));

            // get targets for predicate maps
            List<Value> targets = Utils.getObjectsFromQuads(store.getQuads(predicateMap, valueFactory.createIRI(NAMESPACES.RML + "logicalTarget"), null));

            if (functionValues.isEmpty()) {
                predicateMappingInfos.add(new MappingInfo(predicateMap,
                        new NamedNodeGenerator(RecordFunctionExecutorFactory.generate(store, predicateMap, false, ignoreDoubleQuotes), baseIRI, strictMode),
                        targets));
            } else {
                SingleRecordFunctionExecutor functionExecutor = parseFunctionTermMap(functionValues.get(0));

                predicateMappingInfos.add(new MappingInfo(predicateMap, new NamedNodeGenerator(functionExecutor, baseIRI, strictMode), targets));
            }
        }

        List<Value> predicateShortcuts = Utils.getObjectsFromQuads(store.getQuads(termMap, valueFactory.createIRI(NAMESPACES.RR + "predicate"), null));

        for (Value predicate : predicateShortcuts) {
            String pStr = predicate.stringValue();
            // rr:predicate shortcut can never have targets
            predicateMappingInfos.add(new MappingInfo(termMap, new NamedNodeGenerator(new ConstantExtractor(pStr), baseIRI, strictMode)));
        }

        return predicateMappingInfos;
    }

    private SingleRecordFunctionExecutor parseFunctionTermMap(Value functionValue) throws IOException {
        List<Value> functionPOMs = Utils.getObjectsFromQuads(store.getQuads(functionValue, valueFactory.createIRI(NAMESPACES.RR + "predicateObjectMap"), null));
        ArrayList<ParameterValuePair> params = new ArrayList<>();

        for (Value pom : functionPOMs) {
            List<MappingInfo> pMappingInfos = parsePredicateMapsAndShortcuts(pom);
            List<MappingInfo> oMappingInfos = parseObjectMapsAndShortcuts(pom);

            List<TermGenerator> pGenerators = new ArrayList<>();
            pMappingInfos.forEach(mappingInfo -> {
                pGenerators.add(mappingInfo.getTermGenerator());
            });

            List<TermGenerator> oGenerators = new ArrayList<>();
            oMappingInfos.forEach(mappingInfo -> {
                oGenerators.add(mappingInfo.getTermGenerator());
            });

            params.add(new ParameterValuePair(pGenerators, oGenerators));
        }

        return new DynamicSingleRecordFunctionExecutor(params, functionAgent);
    }

    private MultipleRecordsFunctionExecutor parseJoinConditionFunctionTermMap(Value functionValue) throws IOException {
        List<Value> functionPOMs = Utils.getObjectsFromQuads(store.getQuads(functionValue, valueFactory.createIRI(NAMESPACES.RR + "predicateObjectMap"), null));
        ArrayList<ParameterValueOriginPair> params = new ArrayList<>();

        for (Value pom : functionPOMs) {
            List<MappingInfo> pMappingInfos = parsePredicateMapsAndShortcuts(pom);

            List<TermGenerator> pGenerators = new ArrayList<>();
            pMappingInfos.forEach(mappingInfo -> {
                pGenerators.add(mappingInfo.getTermGenerator());
            });

            ArrayList<TermGeneratorOriginPair> objectGeneratorOriginPairs = new ArrayList<>();
            parseObjectMapsAndShortcutsWithCallback(pom, (oGen, childOrParent) -> {
                objectGeneratorOriginPairs.add(new TermGeneratorOriginPair(oGen.getTermGenerator(), childOrParent));
            }, null);

            params.add(new ParameterValueOriginPair(pGenerators, objectGeneratorOriginPairs));
        }

        return new DynamicMultipleRecordsFunctionExecutor(params, functionAgent);
    }

    /**
     * Generate a join condition that only returns true if the same record hash is encountered
     * @return
     * @throws IOException
     */
    private MultipleRecordsFunctionExecutor generateSameLogicalSourceJoinConditionFunctionTermMap() throws IOException {
        Map<String, Object[]> parameters = new HashMap<>();

        SingleRecordFunctionExecutor parent = new HashExtractor();
        Object[] detailsParent = {"parent", parent};
        parameters.put("http://users.ugent.be/~bjdmeest/function/grel.ttl#valueParameter", detailsParent);

        SingleRecordFunctionExecutor child = new HashExtractor();
        Object[] detailsChild = {"child", child};
        parameters.put("http://users.ugent.be/~bjdmeest/function/grel.ttl#valueParameter2", detailsChild);

        return new StaticMultipleRecordsFunctionExecutor(parameters, functionAgent, "http://example.com/idlab/function/equal");
    }

    private List<MappingInfo> parseObjectMapsAndShortcuts(Value pom) throws IOException {
        List<MappingInfo> mappingInfos = new ArrayList<>();

        parseObjectMapsAndShortcutsWithCallback(pom, (mappingInfo, childOrParent) -> {
            mappingInfos.add(mappingInfo);
        }, (term, joinConditionFunctions) -> {
        });

        return mappingInfos;
    }

    /**
     * This method returns all executors for the languages of an Object Map.
     * @param objectmap the object for which the executors need to be determined.
     * @return a list of executors that return language tags.
     */
    private List<SingleRecordFunctionExecutor> getLanguageExecutorsForObjectMap(Value objectmap) throws IOException {
        ArrayList<SingleRecordFunctionExecutor> executors = new ArrayList<>();

        // Parse rr:language
        List<Value> languages = Utils.getObjectsFromQuads(store.getQuads(objectmap, valueFactory.createIRI(NAMESPACES.RR + "language"), null));

        // Validate languages.
        languages.stream().map(Value::stringValue).forEach(language -> {if (! isValidrrLanguage(language)) {
            throw new RuntimeException(String.format("Language tag \"%s\" does not conform to BCP 47 standards", language));
        }});

        for (Value language: languages) {
            executors.add(new ConstantExtractor(language.stringValue()));
        }

        // Parse rml:languageMap
        List<Value> languageMaps = Utils.getObjectsFromQuads(store.getQuads(objectmap, valueFactory.createIRI(NAMESPACES.RML + "languageMap"), null));

        for (Value languageMap : languageMaps) {
            List<Value> functionValues = Utils.getObjectsFromQuads(store.getQuads(languageMap, valueFactory.createIRI(NAMESPACES.FNML + "functionValue"), null));

            if (functionValues.isEmpty()) {
                executors.add(RecordFunctionExecutorFactory.generate(store, languageMap, false, ignoreDoubleQuotes));
            } else {
                executors.add(parseFunctionTermMap(functionValues.get(0)));
            }
        }

        return executors;
    }

    private MappingInfo parseLanguageMappingInfo(Value objectMap) {
        // get optional language map targets for object map
        MappingInfo mappingInfo = null;

        if(objectMap == null) {
            return mappingInfo;
        }

        List<Value> languageMaps = Utils.getObjectsFromQuads(store.getQuads(objectMap, valueFactory.createIRI(NAMESPACES.RML + "languageMap"), null));
        if (languageMaps.size() == 1) {
            Value l = languageMaps.get(0);
            List<Value> lTargets = Utils.getObjectsFromQuads(store.getQuads(l, valueFactory.createIRI(NAMESPACES.RML + "logicalTarget"), null));
            mappingInfo = new MappingInfo(l, lTargets);
        }
        else if (languageMaps.size() > 1) {
            logger.warn("Multiple language maps found, a random language map is used");
        }
        return mappingInfo;
    }

    /**
     * This method returns the TermType of a given Term Map.
     * If no Term Type is found, a default Term Type is return based on the R2RML specification.
     **/
    private Value getTermType(Value map, boolean isObjectMap) {
        List<Value> termTypes = Utils.getObjectsFromQuads(store.getQuads(map, valueFactory.createIRI(NAMESPACES.RR + "termType"), null));

        Value termType = null;

        if (!termTypes.isEmpty()) {
            termType = termTypes.get(0);
        } else {
            List<Value> constants = Utils.getObjectsFromQuads(store.getQuads(map, valueFactory.createIRI(NAMESPACES.RR + "constant"), null));

            if (!constants.isEmpty()) {
                Value constant = constants.get(0);

                if (constant.isLiteral()) {
                    termType = valueFactory.createIRI(NAMESPACES.RR + "Literal");
                } else if (constant.isIRI()) {
                    termType = valueFactory.createIRI(NAMESPACES.RR + "IRI");
                } else {
                    termType = valueFactory.createIRI(NAMESPACES.RR + "BlankNode");
                }
            } else if (isObjectMap) {
                boolean hasReference = !Utils.getObjectsFromQuads(store.getQuads(map, valueFactory.createIRI(NAMESPACES.RML + "reference"), null)).isEmpty();
                boolean hasFunctionValues = !Utils.getObjectsFromQuads(store.getQuads(map, valueFactory.createIRI(NAMESPACES.FNML + "functionValue"), null)).isEmpty();
                boolean hasLanguage = !Utils.getObjectsFromQuads(store.getQuads(map, valueFactory.createIRI(NAMESPACES.RR + "language"), null)).isEmpty() ||
                        !Utils.getObjectsFromQuads(store.getQuads(map, valueFactory.createIRI(NAMESPACES.RML + "languageMap"), null)).isEmpty();
                boolean hasDatatype = !Utils.getObjectsFromQuads(store.getQuads(map, valueFactory.createIRI(NAMESPACES.RR + "datatype"), null)).isEmpty();

                if (hasReference || hasLanguage || hasDatatype || hasFunctionValues) {
                    termType = valueFactory.createIRI(NAMESPACES.RR + "Literal");
                } else {
                    termType = valueFactory.createIRI(NAMESPACES.RR + "IRI");
                }
            } else {
                termType = valueFactory.createIRI(NAMESPACES.RR + "IRI");
            }
        }

        return termType;
    }

    private List<PredicateObjectGraphMapping> getPredicateObjectGraphMappingFromMultipleGraphMappingInfos(MappingInfo pMappingInfo, MappingInfo oMappingInfo, List<MappingInfo> gMappingInfos) {
        ArrayList<PredicateObjectGraphMapping> list = new ArrayList<>();
        MappingInfo lMappingInfo = null;
        if(oMappingInfo != null) {
            lMappingInfo = parseLanguageMappingInfo(oMappingInfo.getTerm());
        }

        for(MappingInfo gMappingInfo: gMappingInfos) {
            list.add(new PredicateObjectGraphMapping(pMappingInfo, oMappingInfo, gMappingInfo, lMappingInfo));
        }

        if (gMappingInfos.isEmpty()) {
            list.add(new PredicateObjectGraphMapping(pMappingInfo, oMappingInfo, null, lMappingInfo));
        }

        return list;
    }

    /**
     * This function returns true if double quotes should be ignored in references.
     * @param store The store with the RML rules.
     * @param triplesMap The Triples Map that should be checked.
     * @return true if double quotes should be ignored in references, else false.
     */
    private boolean areDoubleQuotesIgnored(QuadStore store, Value triplesMap) {
        List<Value> logicalSources = Utils.getObjectsFromQuads(store.getQuads(triplesMap, valueFactory.createIRI(NAMESPACES.RML + "logicalSource"), null));

        if (!logicalSources.isEmpty()) {
            Value logicalSource = logicalSources.get(0);

            List<Value> sources = Utils.getObjectsFromQuads(store.getQuads(logicalSource, valueFactory.createIRI(NAMESPACES.RML + "source"), null));

            if (!sources.isEmpty()) {
                Value source = sources.get(0);

                if (! (sources.get(0).isLiteral())) {
                    List<Value> sourceType = Utils.getObjectsFromQuads(store.getQuads(source, valueFactory.createIRI(NAMESPACES.RDF + "type"), null));

                    return sourceType.get(0).stringValue().equals(NAMESPACES.D2RQ + "Database");
                }
            }
        }

        return false;
    }
}

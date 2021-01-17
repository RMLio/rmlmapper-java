package be.ugent.rml;

import be.ugent.rml.extractor.ConstantExtractor;
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
    private final FunctionLoader functionLoader;
    private MappingInfo subjectMappingInfo;
    private List<MappingInfo> graphMappingInfos;
    private Term triplesMap;
    private QuadStore store;
    private List<PredicateObjectGraphMapping> predicateObjectGraphMappings;
    // This boolean is true when the double in a reference need to be ignored.
    // For example, when accessing data in a RDB.
    private boolean ignoreDoubleQuotes;
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public MappingFactory(FunctionLoader functionLoader) {
        this.functionLoader = functionLoader;
    }

    public Mapping createMapping(Term triplesMap, QuadStore store) throws Exception {
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
            List<Term> subjectmaps = Utils.getObjectsFromQuads(store.getQuads(triplesMap, new NamedNode(NAMESPACES.RR + "subjectMap"), null));

            if (!subjectmaps.isEmpty()) {
                if (subjectmaps.size() > 1) {
                    throw new Exception(String.format("%s has %d Subject Maps. You can only have one.", triplesMap, subjectmaps.size()));
                }

                Term subjectmap = subjectmaps.get(0);
                List<Term> functionValues = Utils.getObjectsFromQuads(store.getQuads(subjectmap, new NamedNode(NAMESPACES.FNML + "functionValue"), null));
                List<Term> termTypes = Utils.getObjectsFromQuads(store.getQuads(subjectmap, new NamedNode(NAMESPACES.RR + "termType"), null));
                boolean isBlankNode = !termTypes.isEmpty() && termTypes.get(0).equals(new NamedNode(NAMESPACES.RR  + "BlankNode"));

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
                        generator = new NamedNodeGenerator(RecordFunctionExecutorFactory.generate(store, subjectmap, true, ignoreDoubleQuotes));
                    }
                } else {
                    SingleRecordFunctionExecutor functionExecutor = parseFunctionTermMap(functionValues.get(0));

                    if (isBlankNode) {
                        generator = new BlankNodeGenerator(functionExecutor);
                    } else {
                        generator = new NamedNodeGenerator(functionExecutor);
                    }
                }

                // get targets for subject
                List<Term> targets = Utils.getObjectsFromQuads(store.getQuads(subjectmap, new NamedNode(NAMESPACES.RML + "logicalTarget"), null));

                this.subjectMappingInfo = new MappingInfo(subjectmap, generator, targets);

                //get classes
                List<Term> classes = Utils.getObjectsFromQuads(store.getQuads(subjectmap, new NamedNode(NAMESPACES.RR + "class"), null));

                //we create predicateobjects for the classes
                for (Term c : classes) {
                    /*
                     * Don't put in graph for rr:class, subject is already put in graph, otherwise double export.
                     * Same holds for targets, the rdf:type triple will be exported to the subject target already.
                     */
                    NamedNodeGenerator predicateGenerator = new NamedNodeGenerator(new ConstantExtractor(NAMESPACES.RDF + "type"));
                    NamedNodeGenerator objectGenerator = new NamedNodeGenerator(new ConstantExtractor(c.getValue()));
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
        List<Term> predicateobjectmaps = Utils.getObjectsFromQuads(store.getQuads(triplesMap, new NamedNode(NAMESPACES.RR + "predicateObjectMap"), null));

        for (Term pom : predicateobjectmaps) {
            List<MappingInfo> predicateMappingInfos = parsePredicateMapsAndShortcuts(pom);
            List<MappingInfo> graphMappingInfos = parseGraphMapsAndShortcuts(pom);

            parseObjectMapsAndShortcutsAndGeneratePOGGenerators(pom, predicateMappingInfos, graphMappingInfos);
        }
    }

    private void parseObjectMapsAndShortcutsAndGeneratePOGGenerators(Term termMap, List<MappingInfo> predicateMappingInfos, List<MappingInfo> graphMappingInfos) throws IOException {
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

    private void parseObjectMapsAndShortcutsWithCallback(Term termMap, BiConsumer<MappingInfo, String> objectMapCallback, BiConsumer<Term, List<MultipleRecordsFunctionExecutor>> refObjectMapCallback) throws IOException {
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

            // rr:object shortcut can never have targets
            objectMapCallback.accept(new MappingInfo(termMap, gen), "child");
        }
    }

    private void parseObjectMapWithCallback(Term objectmap, BiConsumer<MappingInfo, String> objectMapCallback, BiConsumer<Term, List<MultipleRecordsFunctionExecutor>> refObjectMapCallback) throws IOException {
        List<Term> functionValues = Utils.getObjectsFromQuads(store.getQuads(objectmap, new NamedNode(NAMESPACES.FNML + "functionValue"), null));
        Term termType = getTermType(objectmap, true);

        List<Term> datatypes = Utils.getObjectsFromQuads(store.getQuads(objectmap, new NamedNode(NAMESPACES.RR + "datatype"), null));
        List<Term> parentTriplesMaps = Utils.getObjectsFromQuads(store.getQuads(objectmap, new NamedNode(NAMESPACES.RR + "parentTriplesMap"), null));
        List<Term> parentTermMaps = Utils.getObjectsFromQuads(store.getQuads(objectmap, new NamedNode(NAMESPACES.RML + "parentTermMap"), null));

        List<SingleRecordFunctionExecutor> languages = getLanguageExecutorsForObjectMap(objectmap);

        if (functionValues.isEmpty()) {
            boolean encodeIRI = termType != null && termType.getValue().equals(NAMESPACES.RR + "IRI");
            SingleRecordFunctionExecutor executor = RecordFunctionExecutorFactory.generate(store, objectmap, encodeIRI, ignoreDoubleQuotes);

            if (parentTriplesMaps.isEmpty() && parentTermMaps.isEmpty()) {
                TermGenerator oGen;

                if (termType.equals(new NamedNode(NAMESPACES.RR + "Literal"))) {
                    //check if we need to apply a datatype to the object
                    if (!datatypes.isEmpty()) {
                        oGen = new LiteralGenerator(executor, datatypes.get(0));
                        //check if we need to apply a language to the object
                    } else if (!languages.isEmpty()) {
                        oGen = new LiteralGenerator(executor, languages.get(0));
                    } else {
                        oGen = new LiteralGenerator(executor);
                    }
                } else if (termType.equals(new NamedNode(NAMESPACES.RR + "IRI"))) {
                    oGen = new NamedNodeGenerator(executor);
                } else {
                    if (executor == null) {
                        // This will generate Blank Node with random identifiers.
                        oGen = new BlankNodeGenerator();
                    } else {
                        oGen = new BlankNodeGenerator(executor);
                    }
                }

                // get language maps targets for object map
                MappingInfo languageMapInfo = null;
                List<Term> languageMaps = Utils.getObjectsFromQuads(store.getQuads(objectmap, new NamedNode(NAMESPACES.RML + "languageMap"), null));

                // get targets for object map
                List<Term> oTargets = Utils.getObjectsFromQuads(store.getQuads(objectmap, new NamedNode(NAMESPACES.RML + "logicalTarget"), null));

                objectMapCallback.accept(new MappingInfo(objectmap, oGen, oTargets), "child");
            } else if (!parentTriplesMaps.isEmpty()) {
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

                        boolean ignoreDoubleQuotesInParent = this.areDoubleQuotesIgnored(store, parentTriplesMap);
                        SingleRecordFunctionExecutor parent = new ReferenceExtractor(parents.get(0), ignoreDoubleQuotesInParent);
                        Object[] detailsParent = {"parent", parent};
                        parameters.put("http://users.ugent.be/~bjdmeest/function/grel.ttl#valueParameter", detailsParent);

                        SingleRecordFunctionExecutor child = new ReferenceExtractor(childs.get(0), ignoreDoubleQuotes);
                        Object[] detailsChild = {"child", child};
                        parameters.put("http://users.ugent.be/~bjdmeest/function/grel.ttl#valueParameter2", detailsChild);

                        joinConditionFunctionExecutors.add(new StaticMultipleRecordsFunctionExecutor(equal, parameters));
                    }
                }

                for (Term joinCondition : rmljoinConditions) {
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
            if (termType == null || termType.equals(new NamedNode(NAMESPACES.RR + "Literal"))) {
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
                gen = new NamedNodeGenerator(functionExecutor);
            }

            // get targets for object map
            List<Term> targets = Utils.getObjectsFromQuads(store.getQuads(objectmap, new NamedNode(NAMESPACES.RML + "logicalTarget"), null));

            objectMapCallback.accept(new MappingInfo(objectmap, gen, targets), "child");
        }
    }

    private List<MappingInfo> parseGraphMapsAndShortcuts(Term termMap) throws Exception {
        ArrayList<MappingInfo> graphMappingInfos = new ArrayList<>();

        List<Term> graphMaps = Utils.getObjectsFromQuads(store.getQuads(termMap, new NamedNode(NAMESPACES.RR + "graphMap"), null));

        for (Term graphMap : graphMaps) {
            List<Term> functionValues = Utils.getObjectsFromQuads(store.getQuads(graphMap, new NamedNode(NAMESPACES.FNML + "functionValue"), null));
            List<Term> termTypes = Utils.getObjectsFromQuads(store.getQuads(graphMap, new NamedNode(NAMESPACES.RR + "termType"), null));
            Term termType = null;

            if (!termTypes.isEmpty()) {
                termType = termTypes.get(0);

                if (termType.equals(new NamedNode(NAMESPACES.RR + "Literal"))) {
                    throw new Exception("A Graph Map cannot generate literals.");
                }
            }

            TermGenerator generator;

            if (functionValues.isEmpty()) {
                SingleRecordFunctionExecutor executor = RecordFunctionExecutorFactory.generate(store, graphMap, true, ignoreDoubleQuotes);

                if (termType == null || termType.equals(new NamedNode(NAMESPACES.RR + "IRI"))) {
                    generator = new NamedNodeGenerator(executor);
                } else {
                    if (executor == null) {
                        generator = new BlankNodeGenerator();
                    } else {
                        generator = new BlankNodeGenerator(executor);
                    }
                }
            } else {
                SingleRecordFunctionExecutor functionExecutor = parseFunctionTermMap(functionValues.get(0));

                if (termType == null || termType.equals(new NamedNode(NAMESPACES.RR + "IRI"))) {
                    generator = new NamedNodeGenerator(functionExecutor);
                } else {
                    generator = new BlankNodeGenerator(functionExecutor);
                }
            }

            // get targets for graph maps
            List<Term> targets = Utils.getObjectsFromQuads(store.getQuads(graphMap, new NamedNode(NAMESPACES.RML + "logicalTarget"), null));

            graphMappingInfos.add(new MappingInfo(termMap, generator, targets));
        }

        List<Term> graphShortcuts = Utils.getObjectsFromQuads(store.getQuads(termMap, new NamedNode(NAMESPACES.RR + "graph"), null));

        for (Term graph : graphShortcuts) {
            String gStr = graph.getValue();
            // rr:graph shortcut can never have targets
            graphMappingInfos.add(new MappingInfo(termMap, new NamedNodeGenerator(new ConstantExtractor(gStr))));
        }

        return graphMappingInfos;
    }

    private List<MappingInfo> parsePredicateMapsAndShortcuts(Term termMap) throws IOException {
        ArrayList<MappingInfo> predicateMappingInfos = new ArrayList<>();

        List<Term> predicateMaps = Utils.getObjectsFromQuads(store.getQuads(termMap, new NamedNode(NAMESPACES.RR + "predicateMap"), null));

        for (Term predicateMap : predicateMaps) {
            // get functionValue for predicate maps
            List<Term> functionValues = Utils.getObjectsFromQuads(store.getQuads(predicateMap, new NamedNode(NAMESPACES.FNML + "functionValue"), null));

            // get targets for predicate maps
            List<Term> targets = Utils.getObjectsFromQuads(store.getQuads(predicateMap, new NamedNode(NAMESPACES.RML + "logicalTarget"), null));

            if (functionValues.isEmpty()) {
                predicateMappingInfos.add(new MappingInfo(predicateMap,
                        new NamedNodeGenerator(RecordFunctionExecutorFactory.generate(store, predicateMap, false, ignoreDoubleQuotes)),
                        targets));
            } else {
                SingleRecordFunctionExecutor functionExecutor = parseFunctionTermMap(functionValues.get(0));

                predicateMappingInfos.add(new MappingInfo(predicateMap, new NamedNodeGenerator(functionExecutor), targets));
            }
        }

        List<Term> predicateShortcuts = Utils.getObjectsFromQuads(store.getQuads(termMap, new NamedNode(NAMESPACES.RR + "predicate"), null));

        for (Term predicate : predicateShortcuts) {
            String pStr = predicate.getValue();
            // rr:predicate shortcut can never have targets
            predicateMappingInfos.add(new MappingInfo(termMap, new NamedNodeGenerator(new ConstantExtractor(pStr))));
        }

        return predicateMappingInfos;
    }

    private SingleRecordFunctionExecutor parseFunctionTermMap(Term functionValue) throws IOException {
        List<Term> functionPOMs = Utils.getObjectsFromQuads(store.getQuads(functionValue, new NamedNode(NAMESPACES.RR + "predicateObjectMap"), null));
        ArrayList<ParameterValuePair> params = new ArrayList<>();

        for (Term pom : functionPOMs) {
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

        return new DynamicSingleRecordFunctionExecutor(params, functionLoader);
    }

    private MultipleRecordsFunctionExecutor parseJoinConditionFunctionTermMap(Term functionValue) throws IOException {
        List<Term> functionPOMs = Utils.getObjectsFromQuads(store.getQuads(functionValue, new NamedNode(NAMESPACES.RR + "predicateObjectMap"), null));
        ArrayList<ParameterValueOriginPair> params = new ArrayList<>();

        for (Term pom : functionPOMs) {
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

        return new DynamicMultipleRecordsFunctionExecutor(params, functionLoader);
    }

    private List<MappingInfo> parseObjectMapsAndShortcuts(Term pom) throws IOException {
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
    private List<SingleRecordFunctionExecutor> getLanguageExecutorsForObjectMap(Term objectmap) throws IOException {
        ArrayList<SingleRecordFunctionExecutor> executors = new ArrayList<>();

        // Parse rr:language
        List<Term> languages = Utils.getObjectsFromQuads(store.getQuads(objectmap, new NamedNode(NAMESPACES.RR + "language"), null));

        // Validate languages.
        languages.stream().map(Term::getValue).forEach(language -> {if (! isValidrrLanguage(language)) {
            throw new RuntimeException(String.format("Language tag \"%s\" does not conform to BCP 47 standards", language));
        }});

        for (Term language: languages) {
            executors.add(new ConstantExtractor(language.getValue()));
        }

        // Parse rml:languageMap
        List<Term> languageMaps = Utils.getObjectsFromQuads(store.getQuads(objectmap, new NamedNode(NAMESPACES.RML + "languageMap"), null));

        for (Term languageMap : languageMaps) {
            List<Term> functionValues = Utils.getObjectsFromQuads(store.getQuads(languageMap, new NamedNode(NAMESPACES.FNML + "functionValue"), null));

            if (functionValues.isEmpty()) {
                executors.add(RecordFunctionExecutorFactory.generate(store, languageMap, false, ignoreDoubleQuotes));
            } else {
                executors.add(parseFunctionTermMap(functionValues.get(0)));
            }
        }

        return executors;
    }

    private MappingInfo parseLanguageMappingInfo(Term objectMap) {
        // get optional language map targets for object map
        MappingInfo mappingInfo = null;

        if(objectMap == null) {
            return mappingInfo;
        }

        List<Term> languageMaps = Utils.getObjectsFromQuads(store.getQuads(objectMap, new NamedNode(NAMESPACES.RML + "languageMap"), null));
        if (languageMaps.size() == 1) {
            Term l = languageMaps.get(0);
            List<Term> lTargets = Utils.getObjectsFromQuads(store.getQuads(l, new NamedNode(NAMESPACES.RML + "logicalTarget"), null));
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
    private Term getTermType(Term map, boolean isObjectMap) {
        List<Term> termTypes = Utils.getObjectsFromQuads(store.getQuads(map, new NamedNode(NAMESPACES.RR + "termType"), null));

        Term termType = null;

        if (!termTypes.isEmpty()) {
            termType = termTypes.get(0);
        } else {
            List<Term> constants = Utils.getObjectsFromQuads(store.getQuads(map, new NamedNode(NAMESPACES.RR + "constant"), null));

            if (!constants.isEmpty()) {
                Term constant = constants.get(0);

                if (constant instanceof Literal) {
                    termType = new NamedNode(NAMESPACES.RR + "Literal");
                } else if (constant instanceof NamedNode) {
                    termType = new NamedNode(NAMESPACES.RR + "IRI");
                } else {
                    termType = new NamedNode(NAMESPACES.RR + "BlankNode");
                }
            } else if (isObjectMap) {
                boolean hasReference = !Utils.getObjectsFromQuads(store.getQuads(map, new NamedNode(NAMESPACES.RML + "reference"), null)).isEmpty();
                boolean hasFunctionValues = !Utils.getObjectsFromQuads(store.getQuads(map, new NamedNode(NAMESPACES.FNML + "functionValue"), null)).isEmpty();
                boolean hasLanguage = !Utils.getObjectsFromQuads(store.getQuads(map, new NamedNode(NAMESPACES.RR + "language"), null)).isEmpty() ||
                        !Utils.getObjectsFromQuads(store.getQuads(map, new NamedNode(NAMESPACES.RML + "languageMap"), null)).isEmpty();
                boolean hasDatatype = !Utils.getObjectsFromQuads(store.getQuads(map, new NamedNode(NAMESPACES.RR + "datatype"), null)).isEmpty();

                if (hasReference || hasLanguage || hasDatatype || hasFunctionValues) {
                    termType = new NamedNode(NAMESPACES.RR + "Literal");
                } else {
                    termType = new NamedNode(NAMESPACES.RR + "IRI");
                }
            } else {
                termType = new NamedNode(NAMESPACES.RR + "IRI");
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
    private boolean areDoubleQuotesIgnored(QuadStore store, Term triplesMap) {
        List<Term> logicalSources = Utils.getObjectsFromQuads(store.getQuads(triplesMap, new NamedNode(NAMESPACES.RML + "logicalSource"), null));

        if (!logicalSources.isEmpty()) {
            Term logicalSource = logicalSources.get(0);

            List<Term> sources = Utils.getObjectsFromQuads(store.getQuads(logicalSource, new NamedNode(NAMESPACES.RML + "source"), null));

            if (!sources.isEmpty()) {
                Term source = sources.get(0);

                if (! (sources.get(0) instanceof Literal)) {
                    List<Term> sourceType = Utils.getObjectsFromQuads(store.getQuads(source, new NamedNode(NAMESPACES.RDF + "type"), null));

                    return sourceType.get(0).getValue().equals(NAMESPACES.D2RQ + "Database");
                }
            }
        }

        return false;
    }
}

package be.ugent.rml;

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
import java.util.stream.Collectors;

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
                        String template = getGenericTemplate(subjectmap);

                        if (template != null) {
                            HashMap<String, List<Template>> parameters = new HashMap<>();
                            ArrayList<Template> temp = new ArrayList<>();
                            temp.add(parseTemplate(getGenericTemplate(subjectmap)));
                            parameters.put("_TEMPLATE", temp);

                            this.subject = new BlankNodeGenerator(new ApplyTemplateFunction(parameters, true));
                        } else {
                            this.subject = new BlankNodeGenerator();
                        }
                    } else {
                        //we are not dealing with a Blank Node, so we create the template
                        HashMap<String, List<Template>> parameters = new HashMap<>();
                        ArrayList<Template> temp = new ArrayList<>();
                        temp.add(parseTemplate(getGenericTemplate(subjectmap)));
                        parameters.put("_TEMPLATE", temp);

                        this.subject = new NamedNodeGenerator(new ApplyTemplateFunction(parameters, true));
                    }
                } else {
                    Function function = parseFunctionTermMap(functionValues.get(0));

                    this.subject = new NamedNodeGenerator(function);
                }

                this.graphs = parseGraphMaps(subjectmap);

                //get classes
                List<Term> classes = Utils.getObjectsFromQuads(store.getQuads(subjectmap, new NamedNode(NAMESPACES.RR  + "class"), null));

                //we create predicateobjects for the classes
                for (Term c: classes) {
                    // configure predicategenerator
                    HashMap<String, List<Template>> parameters = new HashMap<>();
                    List<Template> temp2 = new ArrayList<>();
                    Template temp3 = new Template();
                    temp3.addElement(new TemplateElement(NAMESPACES.RDF + "type", TEMPLATETYPE.CONSTANT));
                    temp2.add(temp3);

                    parameters.put("_TEMPLATE", temp2);

                    // configure objectgenerator
                    HashMap<String, List<Template>> parameters2 = new HashMap<>();
                    temp2 = new ArrayList<>();
                    temp3 = new Template();
                    temp3.addElement(new TemplateElement(c.getValue(), TEMPLATETYPE.CONSTANT));
                    temp2.add(temp3);

                    parameters2.put("_TEMPLATE", temp2);

                    // Don't put in graph for rr:class, subject is already put in graph, otherwise double export
                    NamedNodeGenerator predicateGenerator = new NamedNodeGenerator(new ApplyTemplateFunction(parameters));
                    NamedNodeGenerator objectGenerator = new NamedNodeGenerator(new ApplyTemplateFunction(parameters2));
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
            List<Template> predicates = Utils.getObjectsFromQuads(store.getQuads(pom, new NamedNode(NAMESPACES.RR  + "predicate"), null)).stream().map(i -> {
                Template temp = new Template();
                temp.addElement(new TemplateElement(i.getValue(), TEMPLATETYPE.CONSTANT));
                return temp;
            }).collect(Collectors.toList());

            List<Term> predicatemaps = Utils.getObjectsFromQuads(store.getQuads(pom, new NamedNode(NAMESPACES.RR + "predicateMap"), null));

            for(Term pm : predicatemaps) {
                predicates.add(parseTemplate(getGenericTemplate(pm)));
            }

            ArrayList<TermGenerator> predicateGenerators = new ArrayList<>();

            predicates.forEach(predicate -> {
                HashMap<String, List<Template>> parameters = new HashMap<>();
                ArrayList<Template> temp = new ArrayList<>();
                temp.add(predicate);
                parameters.put("_TEMPLATE", temp);

                predicateGenerators.add(new NamedNodeGenerator(new ApplyTemplateFunction(parameters, true)));
            });

            List<TermGenerator> graphGenerators = parseGraphMaps(pom);
            List<Term> objectmaps = Utils.getObjectsFromQuads(store.getQuads(pom, new NamedNode(NAMESPACES.RR + "objectMap"), null));

            for (Term objectmap : objectmaps) {
                List<Term> functionValues = Utils.getObjectsFromQuads(store.getQuads(objectmap, new NamedNode(NAMESPACES.FNML + "functionValue"), null));
                Term termType = getTermType(objectmap);

                List<Term> datatypes = Utils.getObjectsFromQuads(store.getQuads(objectmap, new NamedNode(NAMESPACES.RR + "datatype"), null));
                List<Term> languages = Utils.getObjectsFromQuads(store.getQuads(objectmap, new NamedNode(NAMESPACES.RR + "language"), null));

                if (functionValues.isEmpty()) {
                    String genericTemplate = getGenericTemplate(objectmap);

                    if (genericTemplate != null) {
                        HashMap<String, List<Template>> parameters = new HashMap<>();
                        ArrayList<Template> temp = new ArrayList<>();
                        temp.add(parseTemplate(genericTemplate));
                        parameters.put("_TEMPLATE", temp);
                        Function fn = new ApplyTemplateFunction(parameters, termType.equals(new NamedNode(NAMESPACES.RR + "IRI")));
                        TermGenerator oGen;

                        if (termType.equals(new NamedNode(NAMESPACES.RR + "Literal"))) {
                            //check if we need to apply a datatype to the object
                            if (!datatypes.isEmpty()) {
                                oGen = new LiteralGenerator(fn, datatypes.get(0));
                                //check if we need to apply a language to the object
                            } else if (!languages.isEmpty()) {
                                oGen = new LiteralGenerator(fn, languages.get(0).getValue());
                            } else {
                                oGen = new LiteralGenerator(fn);
                            }
                        } else {
                            oGen = new NamedNodeGenerator(fn);
                        }

                        predicateGenerators.forEach(pGen -> {
                            if (graphGenerators.isEmpty()) {
                                predicateObjectGraphGenerators.add(new PredicateObjectGraphGenerator(pGen, oGen, null));
                            } else {
                                graphGenerators.forEach(gGen -> {
                                    predicateObjectGraphGenerators.add(new PredicateObjectGraphGenerator(pGen, oGen, gGen));
                                });
                            }

                        });
                    } else {
                        //look for parenttriplesmap
                        List<Term> parentTriplesMaps = Utils.getObjectsFromQuads(store.getQuads(objectmap, new NamedNode(NAMESPACES.RR + "parentTriplesMap"), null));

                        if (! parentTriplesMaps.isEmpty()) {
                            if (parentTriplesMaps.size() > 1) {
                                logger.warn(triplesMap + " has " + parentTriplesMaps.size() + " Parent Triples Maps. You can only have one. A random one is taken.");
                            }

                            Term parentTriplesMap = parentTriplesMaps.get(0);

                            List<Term> joinConditions = Utils.getObjectsFromQuads(store.getQuads(objectmap, new NamedNode(NAMESPACES.RR + "joinCondition"), null));
                            ArrayList<JoinConditionFunction> joinConditionFunctions = new ArrayList<>();

                            for (Term joinCondition : joinConditions) {

                                List<String> parents = Utils.getLiteralObjectsFromQuads(store.getQuads(joinCondition, new NamedNode(NAMESPACES.RR + "parent"), null));
                                List<String> childs = Utils.getLiteralObjectsFromQuads(store.getQuads(joinCondition, new NamedNode(NAMESPACES.RR + "child"), null));

                                if (parents.isEmpty()) {
                                    throw new Error("One of the join conditions of " + triplesMap + " is missing rr:parent.");
                                } else if (childs.isEmpty()) {
                                    throw new Error("One of the join conditions of " + triplesMap + " is missing rr:child.");
                                } else {
                                    FunctionModel equal = functionLoader.getFunction(new NamedNode("http://example.com/idlab/function/equal"));
                                    Map<String, Object[]> parameters = new HashMap<>();

                                    Template parent = new Template();
                                    parent.addElement(new TemplateElement(parents.get(0), TEMPLATETYPE.VARIABLE));
                                    List<Template> parentsList = new ArrayList<>();
                                    parentsList.add(parent);
                                    Object[] detailsParent = {"parent", parentsList};
                                    parameters.put("http://users.ugent.be/~bjdmeest/function/grel.ttl#valueParameter", detailsParent);

                                    Template child = new Template();
                                    child.addElement(new TemplateElement(childs.get(0), TEMPLATETYPE.VARIABLE));
                                    List<Template> childsList = new ArrayList<>();
                                    childsList.add(child);
                                    Object[] detailsChild = {"child", childsList};
                                    parameters.put("http://users.ugent.be/~bjdmeest/function/grel.ttl#valueParameter2", detailsChild);

                                    joinConditionFunctions.add(new JoinConditionFunction(equal, parameters));
                                }
                            }

                            predicateGenerators.forEach(pGen -> {
                                List<PredicateObjectGraphGenerator> pos = getPredicateObjectGraphGeneratorFromMultipleGraphGenerators(pGen, null, graphGenerators);

                                pos.forEach(pogGen -> {
                                    pogGen.setParentTriplesMap(parentTriplesMap);

                                    joinConditionFunctions.forEach(jcf -> {
                                        pogGen.addJoinCondition(jcf);
                                    });

                                    predicateObjectGraphGenerators.add(pogGen);
                                });
                            });
                        }
                    }
                } else {
                    Function fn = parseFunctionTermMap(functionValues.get(0));
                    TermGenerator gen;

                    //TODO is literal the default?
                    if (termType == null || termType.equals( new NamedNode(NAMESPACES.RR + "Literal"))) {
                        //check if we need to apply a datatype to the object
                        if (!datatypes.isEmpty()) {
                            gen = new LiteralGenerator(fn, datatypes.get(0));
                            //check if we need to apply a language to the object
                        } else if (!languages.isEmpty()) {
                            gen = new LiteralGenerator(fn, languages.get(0).getValue());
                        } else {
                            gen = new LiteralGenerator(fn);
                        }
                    } else {
                        gen = new NamedNodeGenerator(fn);
                    }

                    predicateGenerators.forEach(pGen -> {
                        List<PredicateObjectGraphGenerator> pogGens = getPredicateObjectGraphGeneratorFromMultipleGraphGenerators(pGen, gen, graphGenerators);
                        predicateObjectGraphGenerators.addAll(pogGens);
                    });
                }
            }

            //dealing with rr:object
            List<Term> objectsConstants = Utils.getObjectsFromQuads(store.getQuads(pom, new NamedNode(NAMESPACES.RR + "object"), null));

            for (Term o : objectsConstants) {
                TermGenerator gen;
                String oStr = o.getValue();

                HashMap<String, List<Template>> parameters = new HashMap<>();
                List<Template> temp2 = new ArrayList<>();
                Template temp3 = new Template();
                temp3.addElement(new TemplateElement(oStr, TEMPLATETYPE.CONSTANT));
                temp2.add(temp3);

                parameters.put("_TEMPLATE", temp2);

                Function fn = new ApplyTemplateFunction(parameters);

                if (o instanceof Literal) {
                    gen = new LiteralGenerator(fn);
                } else {
                    gen = new NamedNodeGenerator(fn);
                }

                predicateGenerators.forEach(pGen -> {
                    List<PredicateObjectGraphGenerator> pogGens = getPredicateObjectGraphGeneratorFromMultipleGraphGenerators(pGen, gen, graphGenerators);
                    predicateObjectGraphGenerators.addAll(pogGens);
                });
            }
        }
    }

    private List<TermGenerator> parseGraphMaps(Term termMap) {
        ArrayList<TermGenerator> graphs = new ArrayList<>();

        List<Term> graphMaps = Utils.getObjectsFromQuads(store.getQuads(termMap, new NamedNode(NAMESPACES.RR + "graphMap"), null));

        for (Term graphMap : graphMaps) {
            String genericTemplate = getGenericTemplate(graphMap);

            HashMap<String, List<Template>> parameters = new HashMap<>();
            ArrayList<Template> temp = new ArrayList<>();
            temp.add(parseTemplate(genericTemplate));
            parameters.put("_TEMPLATE", temp);

            graphs.add(new NamedNodeGenerator(new ApplyTemplateFunction(parameters, true)));
        }

        List<Term> graphShortCuts = Utils.getObjectsFromQuads(store.getQuads(termMap, new NamedNode(NAMESPACES.RR + "graph"), null));

        for (Term graph : graphShortCuts) {
            String gStr = graph.getValue();

            HashMap<String, List<Template>> parameters = new HashMap<>();
            List<Template> temp2 = new ArrayList<>();
            Template temp3 = new Template();
            temp3.addElement(new TemplateElement(gStr, TEMPLATETYPE.CONSTANT));
            temp2.add(temp3);

            parameters.put("_TEMPLATE", temp2);

            graphs.add(new NamedNodeGenerator(new ApplyTemplateFunction(parameters)));
        }

        return graphs;
    }

    private Function parseFunctionTermMap(Term functionValue) throws IOException {
        List<Term> functionPOMs = Utils.getObjectsFromQuads(store.getQuads(functionValue, new NamedNode(NAMESPACES.RR + "predicateObjectMap"), null));
        HashMap<String, List<Template>> params = new HashMap<>();

        for (Term pom : functionPOMs) {
            //process predicates
            List<Template> predicates = Utils.getObjectsFromQuads(store.getQuads(pom, new NamedNode(NAMESPACES.RR + "predicate"), null)).stream().map(i -> parseTemplate(i.getValue())).collect(Collectors.toList());
            List<Term> pms = Utils.getObjectsFromQuads(store.getQuads(pom, new NamedNode(NAMESPACES.RR + "predicateMap"), null));

            for (Term pm : pms) {
                predicates.add(parseTemplate(getGenericTemplate(pm)));
            }

            //process objects
            List<Template> objects = Utils.getObjectsFromQuads(store.getQuads(pom, new NamedNode(NAMESPACES.RR + "object"), null)).stream().map(i -> parseTemplate(i.getValue())).collect(Collectors.toList());
            List<Term> oms = Utils.getObjectsFromQuads(store.getQuads(pom, new NamedNode(NAMESPACES.RR + "objectMap"), null));

            for (Term om : oms) {
                objects.add(parseTemplate(getGenericTemplate(om)));
            }

            if (!objects.isEmpty()) {
                for (Template p : predicates) {
                    String predicate = Utils.applyTemplate(p, null).get(0);

                    if (!params.containsKey(predicate)) {
                        params.put(predicate, new ArrayList<>());
                    }

                    for (Template o : objects) {
                        params.get(predicate).add(o);
                    }
                }
            }
        }

        String fn = Utils.applyTemplate(params.get("http://w3id.org/function/ontology#executes").get(0), null).get(0);
        params.remove("http://w3id.org/function/ontology#executes");

        return new Function(functionLoader.getFunction(new NamedNode(fn)), params);
    }

    /**
     * This method parses reference, template, and constant of a given Term Map and return a generic template.
     **/
    private String getGenericTemplate(Term termMap) {
        List<Term> references = Utils.getObjectsFromQuads(store.getQuads(termMap, new NamedNode(NAMESPACES.RML + "reference"), null));
        List<Term> templates = Utils.getObjectsFromQuads(store.getQuads(termMap, new NamedNode(NAMESPACES.RR + "template"), null));
        List<Term> constants = Utils.getObjectsFromQuads(store.getQuads(termMap, new NamedNode(NAMESPACES.RR + "constant"), null));
        String genericTemplate = null;

        if (!references.isEmpty()) {
            genericTemplate = "{" + references.get(0).getValue() + "}";
        } else if (!templates.isEmpty()) {
            genericTemplate = templates.get(0).getValue();
        } else if (!constants.isEmpty()) {
            genericTemplate = constants.get(0).getValue();
            genericTemplate = genericTemplate.replaceAll("\\{", "\\\\{").replaceAll("}", "\\\\}");
        }

        return genericTemplate;
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

    /**
     * This method parse the generic template and returns an array
     * that can later be used by the executor (via applyTemplate)
     * to get the data values from the records.
     **/
    private Template parseTemplate(String template) {
        Template result = new Template();
        String current = "";
        boolean previousWasBackslash = false;
        boolean variableBusy = false;

        if (template != null) {
            for (Character c : template.toCharArray()) {

                if (c == '{') {
                    if (previousWasBackslash) {
                        current += c;
                        previousWasBackslash = false;
                    } else if(variableBusy) {
                        throw new Error("Parsing of template failed. Probably a { was followed by a second { without first closing the first {. Make sure that you use { and } correctly.");
                    } else {
                        variableBusy = true;

                        if (!current.equals("")) {
                            result.addElement(new TemplateElement(current, TEMPLATETYPE.CONSTANT));
                        }

                        current = "";
                    }
                } else if (c == '}') {
                    if (previousWasBackslash) {
                        current += c;
                        previousWasBackslash = false;
                    } else if (variableBusy){
                        result.addElement(new TemplateElement(current, TEMPLATETYPE.VARIABLE));
                        current = "";
                        variableBusy = false;
                    } else {
                        throw new Error("Parsing of template failed. Probably a } as used before a { was used. Make sure that you use { and } correctly.");
                    }
                } else if (c == '\\') {
                    if (previousWasBackslash) {
                        previousWasBackslash = false;
                        current += c;
                    } else {
                        previousWasBackslash = true;
                    }
                } else {
                    current += c;
                }
            }

            if (!current.equals("")) {
                result.addElement(new TemplateElement(current, TEMPLATETYPE.CONSTANT));
            }
        }

        return result;
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

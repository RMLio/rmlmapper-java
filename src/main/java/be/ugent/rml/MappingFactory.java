package be.ugent.rml;

import be.ugent.rml.functions.*;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.Literal;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
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
    private TripleElement subject;
    private Term triplesMap;
    private QuadStore store;
    private ArrayList<PredicateObjectGenerator> predicateObjectGenerators;
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public MappingFactory(FunctionLoader functionLoader) {
        this.functionLoader = functionLoader;
    }

    public Mapping createMapping(Term triplesMap, QuadStore store) throws IOException {
        this.triplesMap = triplesMap;
        this.store = store;
        this.subject = null;
        this.predicateObjectGenerators = new ArrayList<>();

        parseSubjectMap();
        parsePredicateObjectMaps();

        //return the mapping
        return new Mapping(subject, predicateObjectGenerators);
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
                        this.subject = new TripleElement(null, new NamedNode(NAMESPACES.RR  + "BlankNode"), null);
                        String template = getGenericTemplate(subjectmap);

                        if (template != null) {
                            HashMap<String, List<Template>> parameters = new HashMap<>();
                            ArrayList<Template> temp = new ArrayList<>();
                            temp.add(parseTemplate(getGenericTemplate(subjectmap)));
                            parameters.put("_TEMPLATE", temp);
                            this.subject.setFunction(new ApplyTemplateFunction(parameters, true));
                        }
                    } else {
                        //we are not dealing with a Blank Node, so we create the template
                        HashMap<String, List<Template>> parameters = new HashMap<>();
                        ArrayList<Template> temp = new ArrayList<>();
                        temp.add(parseTemplate(getGenericTemplate(subjectmap)));
                        parameters.put("_TEMPLATE", temp);
                        this.subject = new TripleElement(null, new NamedNode(NAMESPACES.RR + "IRI"), new ApplyTemplateFunction(parameters, true));
                    }
                } else {
                    Function function = parseFunctionTermMap(functionValues.get(0));

                    this.subject = new TripleElement(null, new NamedNode(NAMESPACES.RR + "IRI"), function);
                }

                this.subject.setGraphs(parseGraphMaps(subjectmap));

                //get classes
                List<Term> classes = Utils.getObjectsFromQuads(store.getQuads(subjectmap, new NamedNode(NAMESPACES.RR  + "class"), null));

                //we create predicateobjects for the classes
                for (Term c: classes) {
                    List<Template> predicates = new ArrayList<>();
                    Template temp = new Template();
                    temp.addElement(new TemplateElement(NAMESPACES.RDF + "type", TEMPLATETYPE.CONSTANT));
                    predicates.add(temp);

                    HashMap<String, List<Template>> parameters = new HashMap<>();
                    List<Template> temp2 = new ArrayList<>();
                    Template temp3 = new Template();
                    temp3.addElement(new TemplateElement(c.getValue(), TEMPLATETYPE.CONSTANT));
                    temp2.add(temp3);

                    parameters.put("_TEMPLATE", temp2);

                    // Don't put in graph for rr:class, subject is already put in graph, otherwise double export
                    predicateObjectGenerators.add(new PredicateObjectGenerator(predicates, null, new NamedNode(NAMESPACES.RR + "IRI"), new ApplyTemplateFunction(parameters)));
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

            List<Template> graphs = parseGraphMaps(pom);
            List<Term> objectmaps = Utils.getObjectsFromQuads(store.getQuads(pom, new NamedNode(NAMESPACES.RR + "objectMap"), null));

            for (Term objectmap : objectmaps) {
                List<Term> functionValues = Utils.getObjectsFromQuads(store.getQuads(objectmap, new NamedNode(NAMESPACES.FNML + "functionValue"), null));
                Term termType = getTermType(objectmap);
                Term datatype = null;
                String language = null;

                List<Term> datatypes = Utils.getObjectsFromQuads(store.getQuads(objectmap, new NamedNode(NAMESPACES.RR + "datatype"), null));
                List<Term> languages = Utils.getObjectsFromQuads(store.getQuads(objectmap, new NamedNode(NAMESPACES.RR + "language"), null));

                //check if we need to apply a datatype to the object
                if (!datatypes.isEmpty()) {
                    datatype = datatypes.get(0);
                }

                //check if we need to apply a language to the object
                if (!languages.isEmpty()) {
                    language = languages.get(0).getValue();
                }

                if (functionValues.isEmpty()) {
                    String genericTemplate = getGenericTemplate(objectmap);

                    if (genericTemplate != null) {
                        HashMap<String, List<Template>> parameters = new HashMap<>();
                        ArrayList<Template> temp = new ArrayList<>();
                        temp.add(parseTemplate(genericTemplate));
                        parameters.put("_TEMPLATE", temp);
                        predicateObjectGenerators.add(new PredicateObjectGenerator(predicates, graphs, termType, new ApplyTemplateFunction(parameters, termType.equals(NAMESPACES.RR + "IRI")), language, datatype));
                    } else {
                        //look for parenttriplesmap
                        List<Term> parentTriplesMaps = Utils.getObjectsFromQuads(store.getQuads(objectmap, new NamedNode(NAMESPACES.RR + "parentTriplesMap"), null));

                        if (! parentTriplesMaps.isEmpty()) {
                            if (parentTriplesMaps.size() > 1) {
                                logger.warn(triplesMap + " has " + parentTriplesMaps.size() + " Parent Triples Maps. You can only have one. A random one is taken.");
                            }

                            Term parentTriplesMap = parentTriplesMaps.get(0);
                            PredicateObjectGenerator po = new PredicateObjectGenerator(predicates, graphs, new NamedNode(NAMESPACES.RR + "IRI"), null);
                            po.setParentTriplesMap(parentTriplesMap);

                            List<Term> joinConditions = Utils.getObjectsFromQuads(store.getQuads(objectmap, new NamedNode(NAMESPACES.RR + "joinCondition"), null));

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

                                    JoinConditionFunction joinConditionFunction = new JoinConditionFunction(equal, parameters);

                                    po.addJoinCondition(joinConditionFunction);
                                }
                            }

                            predicateObjectGenerators.add(po);
                        }
                    }
                } else {
                    Function function = parseFunctionTermMap(functionValues.get(0));

                    if (termType == null) {
                        // TODO be smarter than this
                        termType = new NamedNode(NAMESPACES.RR + "Literal");
                    }

                    predicateObjectGenerators.add(new PredicateObjectGenerator(predicates, graphs, termType, function, language, datatype));
                }
            }

            //dealing with rr:object
            List<Term> objectsConstants = Utils.getObjectsFromQuads(store.getQuads(pom, new NamedNode(NAMESPACES.RR + "object"), null));

            for (Term o : objectsConstants) {
                String termType = NAMESPACES.RR + "Literal";
                String oStr = o.getValue();

                if (!(o instanceof Literal)) {
                    termType = NAMESPACES.RR + "IRI";
                }

                HashMap<String, List<Template>> parameters = new HashMap<>();
                List<Template> temp2 = new ArrayList<>();
                Template temp3 = new Template();
                temp3.addElement(new TemplateElement(oStr, TEMPLATETYPE.CONSTANT));
                temp2.add(temp3);

                parameters.put("_TEMPLATE", temp2);
                predicateObjectGenerators.add(new PredicateObjectGenerator(predicates, graphs, new NamedNode(termType), new ApplyTemplateFunction(parameters)));
            }
        }
    }

    private List<Template> parseGraphMaps(Term termMap) {
        ArrayList<Template> graphs = new ArrayList<>();

        List<Term> graphMaps = Utils.getObjectsFromQuads(store.getQuads(termMap, new NamedNode(NAMESPACES.RR + "graphMap"), null));

        for (Term graphMap : graphMaps) {
            graphs.add(parseTemplate(getGenericTemplate(graphMap)));
        }

        List<Term> graphShortCuts = Utils.getObjectsFromQuads(store.getQuads(termMap, new NamedNode(NAMESPACES.RR + "graph"), null));

        for (Term graph : graphShortCuts) {
            Template temp = new Template();
            temp.addElement(new TemplateElement(graph.getValue(), TEMPLATETYPE.CONSTANT));
            graphs.add(temp);
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

}

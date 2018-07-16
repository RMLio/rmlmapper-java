package be.ugent.rml;

import be.ugent.rml.functions.*;
import be.ugent.rml.store.QuadStore;
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
    private String triplesMap;
    private QuadStore store;
    private ArrayList<PredicateObject> predicateObjects;
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public MappingFactory(FunctionLoader functionLoader) {
        this.functionLoader = functionLoader;
    }

    public Mapping createMapping(String triplesMap, QuadStore store) throws IOException {
        this.triplesMap = triplesMap;
        this.store = store;
        this.subject = null;
        this.predicateObjects = new ArrayList<>();

        parseSubjectMap();
        parsePredicateObjectMaps();

        //return the mapping
        return new Mapping(subject, predicateObjects);
    }

    private void parseSubjectMap() throws IOException {
        if (this.subject == null) {
            List<String> subjectmaps = Utils.getObjectsFromQuads(store.getQuads(triplesMap, NAMESPACES.RR + "subjectMap", null));

            if (!subjectmaps.isEmpty()) {
                if (subjectmaps.size() > 1) {
                    logger.warn(triplesMap + " has " + subjectmaps.size() + "Subject Maps. You can only have one. A random one is taken.");
                }

                String subjectmap = subjectmaps.get(0);
                List<String> functionValues =  Utils.getObjectsFromQuads(store.getQuads(subjectmap, NAMESPACES.FNML + "functionValue", null));

                if (functionValues.isEmpty()) {
                    List<String> termTypes = Utils.getObjectsFromQuads(store.getQuads(subjectmap, NAMESPACES.RR  + "termType", null));

                    //checking if we are dealing with a Blank Node as subject
                    if (!termTypes.isEmpty() && termTypes.get(0).equals(NAMESPACES.RR  + "BlankNode")) {
                        this.subject = new TripleElement(null, termTypes.get(0), null);
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
                        this.subject = new TripleElement(null, NAMESPACES.RR + "IRI", new ApplyTemplateFunction(parameters, true));
                    }
                } else {
                    Function function = parseFunctionTermMap(functionValues.get(0));

                    this.subject = new TripleElement(null, NAMESPACES.RR + "IRI", function);
                }

                this.subject.setGraphs(parseGraphMaps(subjectmap));

                //get classes
                List<String> classes = Utils.getObjectsFromQuads(store.getQuads(subjectmap, NAMESPACES.RR  + "class", null));

                //we create predicateobjects for the classes
                for (String c: classes) {
                    List<Template> predicates = new ArrayList<>();
                    Template temp = new Template();
                    temp.addElement(new TemplateElement(NAMESPACES.RDF + "type", TEMPLATETYPE.CONSTANT));
                    predicates.add(temp);

                    HashMap<String, List<Template>> parameters = new HashMap<>();
                    List<Template> temp2 = new ArrayList<>();
                    Template temp3 = new Template();
                    temp3.addElement(new TemplateElement(c, TEMPLATETYPE.CONSTANT));
                    temp2.add(temp3);

                    parameters.put("_TEMPLATE", temp2);

                    // Don't put in graph for rr:class, subject is already put in graph, otherwise double export
                    predicateObjects.add(new PredicateObject(predicates, null, NAMESPACES.RR + "IRI", new ApplyTemplateFunction(parameters)));
                }
            } else {
                throw new Error(triplesMap + " has no Subject Map. Each Triples Map should have exactly one Subject Map.");
            }
        }
    }

    private void parsePredicateObjectMaps() throws IOException {
        List<String> predicateobjectmaps = Utils.getObjectsFromQuads(store.getQuads(triplesMap, NAMESPACES.RR + "predicateObjectMap", null));

        for (String pom : predicateobjectmaps) {
            List<Template> predicates = Utils.getObjectsFromQuads(store.getQuads(pom, NAMESPACES.RR  + "predicate", null)).stream().map(i -> {
                Template temp = new Template();
                temp.addElement(new TemplateElement(i, TEMPLATETYPE.CONSTANT));
                return temp;
            }).collect(Collectors.toList());

            List<String> predicatemaps = Utils.getObjectsFromQuads(store.getQuads(pom, NAMESPACES.RR + "predicateMap", null));

            for(String pm : predicatemaps) {
                predicates.add(parseTemplate(getGenericTemplate(pm)));
            }

            List<Template> graphs = parseGraphMaps(pom);
            List<String> objectmaps = Utils.getObjectsFromQuads(store.getQuads(pom, NAMESPACES.RR + "objectMap", null));

            for (String objectmap : objectmaps) {
                List<String> functionValues = Utils.getObjectsFromQuads(store.getQuads(objectmap, NAMESPACES.FNML + "functionValue", null));
                String termType = getTermType(objectmap);
                String datatype = null;
                String language = null;

                List<String> datatypes = Utils.getObjectsFromQuads(store.getQuads(objectmap, NAMESPACES.RR + "datatype", null));
                List<String> languages = Utils.getObjectsFromQuads(store.getQuads(objectmap, NAMESPACES.RR + "language", null));

                //check if we need to apply a datatype to the object
                if (!datatypes.isEmpty()) {
                    datatype = datatypes.get(0);
                }

                //check if we need to apply a language to the object
                if (!languages.isEmpty()) {
                    language = Utils.getLiteral(languages.get(0));
                }

                if (functionValues.isEmpty()) {
                    String genericTemplate = getGenericTemplate(objectmap);

                    if (genericTemplate != null) {
                        HashMap<String, List<Template>> parameters = new HashMap<>();
                        ArrayList<Template> temp = new ArrayList<>();
                        temp.add(parseTemplate(genericTemplate));
                        parameters.put("_TEMPLATE", temp);
                        predicateObjects.add(new PredicateObject(predicates, graphs, termType, new ApplyTemplateFunction(parameters, termType.equals(NAMESPACES.RR + "IRI")), language, datatype));
                    } else {
                        //look for parenttriplesmap
                        List<String> parentTriplesMaps = Utils.getObjectsFromQuads(store.getQuads(objectmap, NAMESPACES.RR + "parentTriplesMap", null));

                        if (! parentTriplesMaps.isEmpty()) {
                            if (parentTriplesMaps.size() > 1) {
                                logger.warn(triplesMap + " has " + parentTriplesMaps.size() + " Parent Triples Maps. You can only have one. A random one is taken.");
                            }

                            String parentTriplesMap = parentTriplesMaps.get(0);
                            PredicateObject po = new PredicateObject(predicates, graphs, NAMESPACES.RR + "IRI", null);
                            po.setParentTriplesMap(parentTriplesMap);

                            List<String> joinConditions = Utils.getObjectsFromQuads(store.getQuads(objectmap, NAMESPACES.RR + "joinCondition", null));

                            for (String joinCondition : joinConditions) {

                                List<String> parents = Utils.getLiteralObjectsFromQuads(store.getQuads(joinCondition, NAMESPACES.RR + "parent", null));
                                List<String> childs = Utils.getLiteralObjectsFromQuads(store.getQuads(joinCondition, NAMESPACES.RR + "child", null));

                                if (parents.isEmpty()) {
                                    throw new Error("One of the join conditions of " + triplesMap + " is missing rr:parent.");
                                } else if (childs.isEmpty()) {
                                    throw new Error("One of the join conditions of " + triplesMap + " is missing rr:child.");
                                } else {
                                    FunctionModel equal = functionLoader.getFunction("http://example.com/idlab/function/equal");
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

                            predicateObjects.add(po);
                        }
                    }
                } else {
                    Function function = parseFunctionTermMap(functionValues.get(0));

                    if (termType == null) {
                        // TODO be smarter than this
                        termType = NAMESPACES.RR + "Literal";
                    }

                    predicateObjects.add(new PredicateObject(predicates, graphs, termType, function, language, datatype));
                }
            }

            //dealing with rr:object
            List<String> objectsConstants = Utils.getObjectsFromQuads(store.getQuads(pom, NAMESPACES.RR + "object", null));

            for (String o : objectsConstants) {
                String termType = NAMESPACES.RR + "Literal";

                if (Utils.isLiteral(o)) {
                    o = Utils.getLiteral(o);
                } else {
                    termType = NAMESPACES.RR + "IRI";
                }

                HashMap<String, List<Template>> parameters = new HashMap<>();
                List<Template> temp2 = new ArrayList<>();
                Template temp3 = new Template();
                temp3.addElement(new TemplateElement(o, TEMPLATETYPE.CONSTANT));
                temp2.add(temp3);

                parameters.put("_TEMPLATE", temp2);
                predicateObjects.add(new PredicateObject(predicates, graphs, termType, new ApplyTemplateFunction(parameters)));
            }
        }
    }

    private List<Template> parseGraphMaps(String termMap) {
        ArrayList<Template> graphs = new ArrayList<>();

        List<String> graphMaps = Utils.getObjectsFromQuads(store.getQuads(termMap, NAMESPACES.RR + "graphMap", null));

        for (String graphMap : graphMaps) {
            graphs.add(parseTemplate(getGenericTemplate(graphMap)));
        }

        List<String> graphShortCuts = Utils.getObjectsFromQuads(store.getQuads(termMap, NAMESPACES.RR + "graph", null));

        for (String graph : graphShortCuts) {
            Template temp = new Template();
            temp.addElement(new TemplateElement(graph, TEMPLATETYPE.CONSTANT));
            graphs.add(temp);
        }

        return graphs;
    }

    private Function parseFunctionTermMap(String functionValue) throws IOException {
        List<String> functionPOMs = Utils.getObjectsFromQuads(store.getQuads(functionValue, NAMESPACES.RR + "predicateObjectMap", null));
        HashMap<String, List<Template>> params = new HashMap<>();

        for (String pom : functionPOMs) {
            //process predicates
            List<Template> predicates = Utils.getObjectsFromQuads(store.getQuads(pom, NAMESPACES.RR + "predicate", null)).stream().map(i -> parseTemplate(i)).collect(Collectors.toList());
            List<String> pms = Utils.getObjectsFromQuads(store.getQuads(pom, NAMESPACES.RR + "predicateMap", null));

            for (String pm : pms) {
                predicates.add(parseTemplate(getGenericTemplate(pm)));
            }

            //process objects
            List<Template> objects = Utils.getObjectsFromQuads(store.getQuads(pom, NAMESPACES.RR + "object", null)).stream().map(i -> parseTemplate(i)).collect(Collectors.toList());
            List<String> oms = Utils.getObjectsFromQuads(store.getQuads(pom, NAMESPACES.RR + "objectMap", null));

            for (String om : oms) {
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

        return new Function(functionLoader.getFunction(fn), params);
    }

    /**
     * This method parses reference, template, and constant of a given Term Map and return a generic template.
     **/
    private String getGenericTemplate(String str) {
        List<String> references = Utils.getObjectsFromQuads(store.getQuads(str, NAMESPACES.RML + "reference", null));
        List<String> templates = Utils.getObjectsFromQuads(store.getQuads(str, NAMESPACES.RR + "template", null));
        List<String> constants = Utils.getObjectsFromQuads(store.getQuads(str, NAMESPACES.RR + "constant", null));
        String genericTemplate = null;

        if (!references.isEmpty()) {
            genericTemplate = "{" + Utils.getLiteral(references.get(0)) + "}";
        } else if (!templates.isEmpty()) {
            genericTemplate = Utils.getLiteral(templates.get(0));
        } else if (!constants.isEmpty()) {
            if (Utils.isLiteral(constants.get(0))) {
                genericTemplate = Utils.getLiteral(constants.get(0));
            } else {
                genericTemplate = constants.get(0);
            }

            genericTemplate = genericTemplate.replaceAll("\\{", "\\\\{").replaceAll("}", "\\\\}");
        }

        return genericTemplate;
    }

    /**
     * This method returns the TermType of a given Term Map.
     * If no Term Type is found, a default Term Type is return based on the R2RML specification.
     **/
    private String getTermType(String map) {
        List<String> references = Utils.getObjectsFromQuads(store.getQuads(map, NAMESPACES.RML + "reference", null));
        List<String> templates = Utils.getObjectsFromQuads(store.getQuads(map, NAMESPACES.RR  + "template", null));
        List<String> constants = Utils.getObjectsFromQuads(store.getQuads(map, NAMESPACES.RR  + "constant", null));
        List<String> termTypes = Utils.getObjectsFromQuads(store.getQuads(map, NAMESPACES.RR  + "termType", null));

        String termType = null;

        if (!termTypes.isEmpty()) {
            termType = termTypes.get(0);
        } else {
            if (!references.isEmpty()) {
                termType = NAMESPACES.RR + "Literal";
            } else if (!templates.isEmpty()) {
                termType = NAMESPACES.RR + "IRI";
            } else if (!constants.isEmpty()) {
                termType = NAMESPACES.RR + "Literal";
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

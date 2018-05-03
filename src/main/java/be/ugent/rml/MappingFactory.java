package be.ugent.rml;

import be.ugent.rml.functions.ApplyTemplateFunction;
import be.ugent.rml.functions.Function;
import be.ugent.rml.functions.FunctionDetails;
import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.store.QuadStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MappingFactory {
    private final FunctionLoader functionLoader;
    private TripleElement subject;
    private String triplesMap;
    private QuadStore store;
    private ArrayList<PredicateObject> predicateObjects;

    public MappingFactory(FunctionLoader functionLoader) {
        this.functionLoader = functionLoader;
    }

    public Mapping createMapping(String triplesMap, QuadStore store) {
        this.triplesMap = triplesMap;
        this.store = store;
        this.subject = null;
        this.predicateObjects = new ArrayList<>();

        parseSubjectMap();
        parsePredicateObjectMaps();

        //return the mapping
        return new Mapping(subject, predicateObjects);
    }

    private void parseSubjectMap() {
        if (this.subject == null) {
            List<String> subjectmaps = Utils.getObjectsFromQuads(store.getQuads(triplesMap, NAMESPACES.RR + "subjectMap", null));

            if (!subjectmaps.isEmpty()) {
                if (subjectmaps.size() > 1) {
                    //TODO logger warn
                }

                String subjectmap = subjectmaps.get(0);
                List<String> functionValues =  Utils.getObjectsFromQuads(store.getQuads(subjectmap, NAMESPACES.FNML + "functionValue", null));

                if (functionValues.isEmpty()) {
                    List<String> termTypes = Utils.getObjectsFromQuads(store.getQuads(subjectmap, NAMESPACES.RR  + "termType", null));

                    //checking if we are dealing with a Blank Node as subject
                    if (!termTypes.isEmpty() && termTypes.get(0).equals(NAMESPACES.RR  + "BlankNode")) {
                        this.subject = new TripleElement(null, termTypes.get(0), null, null);
                        String template = getGenericTemplate(subjectmap);

                        if (template != null) {
                            this.subject.setFunction(new ApplyTemplateFunction());
                            HashMap<String, List<List<Element>>> parameters = new HashMap<>();
                            ArrayList<List<Element>> temp = new ArrayList<>();
                            temp.add(parseTemplate(getGenericTemplate(subjectmap)));
                            parameters.put("_TEMPLATE", temp);
                            this.subject.setParameters(parameters);
                        }
                    } else {
                        //we are not dealing with a Blank Node, so we create the template
                        HashMap<String, List<List<Element>>> parameters = new HashMap<>();
                        ArrayList<List<Element>> temp = new ArrayList<>();
                        temp.add(parseTemplate(getGenericTemplate(subjectmap)));
                        parameters.put("_TEMPLATE", temp);
                        this.subject = new TripleElement(null, NAMESPACES.RR  + "IRI", new ApplyTemplateFunction(), parameters);
                    }
                } else {
                    FunctionDetails functionDetails = parseFunctionTermMap(functionValues.get(0));

                    this.subject = new TripleElement(null, NAMESPACES.RR  + "IRI", functionDetails.getFunction(), functionDetails.getParameters());
                }

                this.subject.setGraphs(parseGraphMaps(subjectmap));

                //get classes
                List<String> classes = Utils.getObjectsFromQuads(store.getQuads(subjectmap, NAMESPACES.RR  + "class", null));

                //we create predicateobjects for the classes
                for (String c: classes) {
                    List<List<Element>> predicates = new ArrayList<>();
                    ArrayList<Element> temp = new ArrayList<>();
                    temp.add(new Element(NAMESPACES.RDF + "type", TEMPLATETYPE.CONSTANT));
                    predicates.add(temp);

                    HashMap<String, List<List<Element>>> parameters = new HashMap<>();
                    List<List<Element>> temp2 = new ArrayList<>();
                    ArrayList<Element> temp3 = new ArrayList<>();
                    temp3.add(new Element(c, TEMPLATETYPE.CONSTANT));
                    temp2.add(temp3);

                    parameters.put("_TEMPLATE", temp2);

                    predicateObjects.add(new PredicateObject(predicates, null, NAMESPACES.RR  + "IRI", new ApplyTemplateFunction(), parameters));
                }
            } else {
                throw new Error(triplesMap + " has no Subject Map. Each Triples Map should have exactly one Subject Map.");
            }
        }
    }

    private FunctionDetails parseFunctionTermMap(String functionValue) {
        List<String> functionPOMs = Utils.getObjectsFromQuads(store.getQuads(functionValue, NAMESPACES.RR  + "predicateObjectMap", null));
        HashMap<String, List<List<Element>>> params = new HashMap<>();

        for(String pom : functionPOMs) {
            //process predicates
            List<List<Element>> predicates = Utils.getObjectsFromQuads(store.getQuads(pom, NAMESPACES.RR  + "predicate", null)).stream().map(i -> parseTemplate(i)).collect(Collectors.toList());
            List<String> pms = Utils.getObjectsFromQuads(store.getQuads(pom, NAMESPACES.RR  + "predicateMap", null));

            for(String pm : pms) {
                predicates.add(parseTemplate(getGenericTemplate(pm)));
            }

            //process objects
            List<List<Element>> objects = Utils.getObjectsFromQuads(store.getQuads(pom, NAMESPACES.RR  + "object", null)).stream().map(i -> parseTemplate(i)).collect(Collectors.toList());
            List<String> oms = Utils.getObjectsFromQuads(store.getQuads(pom, NAMESPACES.RR  + "objectMap", null));

            for(String om : oms) {
                objects.add(parseTemplate(getGenericTemplate(om)));
            }

            if (!objects.isEmpty()) {
                for (List<Element> p : predicates) {
                    String predicate = Utils.applyTemplate(p, null).get(0);

                    if (!params.containsKey(predicate)) {
                        params.put(predicate, new ArrayList<>());
                    }

                    for(List<Element> o : objects) {
                        params.get(predicate).add(o);
                    }
                }
            }
        }

        String fn = Utils.applyTemplate(params.get("http://w3id.org/function/ontology#executes").get(0), null).get(0);
        params.remove("http://w3id.org/function/ontology#executes");

        return new FunctionDetails(getExecutableFunction(functionLoader.getFunction(fn)), params);
    }

    private void parsePredicateObjectMaps() {
        List<String> predicateobjectmaps = Utils.getObjectsFromQuads(store.getQuads(triplesMap, NAMESPACES.RR + "predicateObjectMap", null));

        for (String pom : predicateobjectmaps) {
            List<List<Element>> predicates = Utils.getObjectsFromQuads(store.getQuads(pom, NAMESPACES.RR  + "predicate", null)).stream().map(i -> {
                List<Element> temp = new ArrayList<>();
                temp.add(new Element(i, TEMPLATETYPE.CONSTANT));
                return temp;
            }).collect(Collectors.toList());

            List<String> predicatemaps = Utils.getObjectsFromQuads(store.getQuads(pom, NAMESPACES.RR + "predicateMap", null));

            for(String pm : predicatemaps) {
                predicates.add(parseTemplate(getGenericTemplate(pm)));
            }

            List<List<Element>> graphs = parseGraphMaps(pom);
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
                        HashMap<String, List<List<Element>>> parameters = new HashMap<>();
                        ArrayList<List<Element>> temp = new ArrayList<>();
                        temp.add(parseTemplate(genericTemplate));
                        parameters.put("_TEMPLATE", temp);
                        predicateObjects.add(new PredicateObject(predicates, graphs, termType, new ApplyTemplateFunction(), parameters, language, datatype));
                    } else {
                        //look for parenttriplesmap
                        List<String> parentTriplesMaps = Utils.getObjectsFromQuads(store.getQuads(objectmap, NAMESPACES.RR + "parentTriplesMap", null));

                        if (! parentTriplesMaps.isEmpty()) {
                            if (parentTriplesMaps.size() > 1) {
                                //todo logger warn
                            }

                            String parentTriplesMap = parentTriplesMaps.get(0);
                            PredicateObject po = new PredicateObject(predicates, graphs, NAMESPACES.RR + "IRI", null, null);
                            po.setParentTriplesMap(parentTriplesMap);

                            List<String> joinConditions = Utils.getObjectsFromQuads(store.getQuads(objectmap, NAMESPACES.RR + "joinCondition", null));

                            for (String joinCondition : joinConditions) {
                                List<String> parents = Utils.getObjectsFromQuads(store.getQuads(joinCondition, NAMESPACES.RR + "parent", null));
                                List<String> childs = Utils.getObjectsFromQuads(store.getQuads(joinCondition, NAMESPACES.RR + "child", null));

                                if (parents.isEmpty()) {
                                    throw new Error("One of the join conditions of " + triplesMap + " is missing rr:parent.");
                                } else if (childs.isEmpty()) {
                                    throw new Error("One of the join conditions of " + triplesMap + " is missing rr:child.");
                                } else {
                                    List<Element> parent = new ArrayList<>();
                                    parent.add(new Element(parents.get(0), TEMPLATETYPE.VARIABLE));
                                    List<Element> child = new ArrayList<>();
                                    child.add(new Element(childs.get(0), TEMPLATETYPE.VARIABLE));
                                    po.addJoinCondition(new JoinCondition(parent, child));
                                }
                            }

                            predicateObjects.add(po);
                        }
                    }
                } else {
                    FunctionDetails functionDetails = parseFunctionTermMap(functionValues.get(0));

                    predicateObjects.add(new PredicateObject(predicates, graphs, termType, functionDetails.getFunction(), functionDetails.getParameters(), language, datatype));
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

                HashMap<String, List<List<Element>>> parameters = new HashMap<>();
                List<List<Element>> temp2 = new ArrayList<>();
                ArrayList<Element> temp3 = new ArrayList<>();
                temp3.add(new Element(o, TEMPLATETYPE.CONSTANT));
                temp2.add(temp3);

                parameters.put("_TEMPLATE", temp2);
                predicateObjects.add(new PredicateObject(predicates, graphs, termType, new ApplyTemplateFunction(), parameters));
            }
        }
    }

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

    private Function getExecutableFunction(Object o) {
        return null;
    }

    private List<List<Element>> parseGraphMaps(String termMap) {
        ArrayList<List<Element>> graphs = new ArrayList<>();

        List<String> graphMaps = Utils.getObjectsFromQuads(store.getQuads(termMap, NAMESPACES.RR  + "graphMap", null));

        for (String graphMap : graphMaps) {
            graphs.add(parseTemplate(getGenericTemplate(graphMap)));
        }

        List<String> graphShortCuts = Utils.getObjectsFromQuads(store.getQuads(termMap, NAMESPACES.RR  + "graph", null));

        for (String graph : graphShortCuts) {
            ArrayList<Element> temp = new ArrayList<>();
            temp.add(new Element(graph, TEMPLATETYPE.CONSTANT));
            graphs.add(temp);
        }

        return graphs;
    }

    private List<Element> parseTemplate(String template) {
        ArrayList<Element> result = new ArrayList<>();
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
                        if (!current.equals("")) {
                            result.add(new Element(current, TEMPLATETYPE.CONSTANT));
                        }

                        current = "";
                    }

                    variableBusy = true;
                } else if (c == '}') {
                    if (previousWasBackslash) {
                        current += c;
                        previousWasBackslash = false;
                    } else if (variableBusy){
                        result.add(new Element(current, TEMPLATETYPE.VARIABLE));
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
                result.add(new Element(current, TEMPLATETYPE.CONSTANT));
            }
        }

        return result;
    }

    private String getGenericTemplate(String str) {
        List<String> references = Utils.getObjectsFromQuads(store.getQuads(str, NAMESPACES.RML + "reference", null));
        List<String> templates = Utils.getObjectsFromQuads(store.getQuads(str, NAMESPACES.RR  + "template", null));
        List<String> constants = Utils.getObjectsFromQuads(store.getQuads(str, NAMESPACES.RR  + "constant", null));
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
}

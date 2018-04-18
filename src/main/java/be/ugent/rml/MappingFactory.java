package be.ugent.rml;

import be.ugent.rml.functions.FunctionDetails;
import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.store.QuadStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MappingFactory {
    private final FunctionLoader functionLoader;
    private TripleElement subject;
    private String triplesMap;
    private QuadStore store;
    private ArrayList<PredicateObject> predicateObjects;
    private final String NS_RR = "";
    private final String NS_FNML = "";
    private final String NS_RDF = "";

    public MappingFactory(FunctionLoader functionLoader) {
        this.functionLoader = functionLoader;
    }

    public Mapping createMapping(String triplesMap, QuadStore store) {
        this.triplesMap = triplesMap;
        this.store = store;
        this.subject = null;
        this.predicateObjects = new ArrayList<PredicateObject>();

        parseSubjectMap();
        parsePredicateObjectMaps();

        //return the mapping
        return new Mapping(subject, predicateObjects);
    }

    private void parseSubjectMap() {
        if (this.subject == null) {
            List<String> subjectmaps = Utils.getObjectsFromQuads(store.getQuads(triplesMap, NS_RR + "subjectMap", null));

            if (!subjectmaps.isEmpty()) {
                if (subjectmaps.size() > 1) {
                    //TODO logger warn
                }

                String subjectmap = subjectmaps.get(0);
                List<String> functionValues =  Utils.getObjectsFromQuads(store.getQuads(subjectmap, NS_FNML + "functionValue", null));

                if (functionValues.isEmpty()) {
                    List<String> termTypes = Utils.getObjectsFromQuads(store.getQuads(subjectmap, NS_RR + "termType", null));

                    //checking if we are dealing with a Blank Node as subject
                    if (!termTypes.isEmpty() && termTypes.get(0).equals(NS_RR + "BlankNode")) {
                        this.subject = new TripleElement(null, termTypes.get(0), null, null);
                    } else {
                        //we are not dealing with a Blank Node, so we create the template
                        HashMap<String, Object> parameters = new HashMap<String, Object>();
                        parameters.put("_TEMPLATE", parseTemplate(getGenericTemplate(subjectmap)));
                        this.subject = new TripleElement(null, NS_RR + "IRI", getApplyTemplateFunction(), parameters);
                    }
                } else {
                    FunctionDetails functionDetails = parseFunctionTermMap(functionValues.get(0));

                    this.subject = new TripleElement(null, NS_RR + "IRI", functionDetails.getFunction(), functionDetails.getParameters());
                }

                this.subject.setGraphs(parseGraphMaps(subjectmap));

                //get classes
                List<String> classes = Utils.getObjectsFromQuads(store.getQuads(subjectmap, NS_RR + "class", null));

                //we create predicateobjects for the classes
                for (String c: classes) {
                    ArrayList<ArrayList<Element>> predicates = new ArrayList<ArrayList<Element>>();
                    ArrayList<Element> temp = new ArrayList<Element>();
                    temp.add(new Element(NS_RDF + "type", "constant"));
                    predicates.add(temp);

                    HashMap<String, Object> parameters = new HashMap<String, Object>();
                    temp = new ArrayList<Element>();
                    temp.add(new Element(c, "constant"));
                    parameters.put("_TEMPLATE", temp);

                    predicateObjects.add(new PredicateObject(predicates, this.subject.getGraphs(), NS_RR + "IRI", getApplyTemplateFunction(), parameters));
                }
            } else {
                throw new Error(triplesMap + " has no Subject Map. Each Triples Map should have exactly one Subject Map.");
            }
        }
    }

    private void parsePredicateObjectMaps() {

    }

    private List<List<Element>> parseGraphMaps(String termMap) {
        ArrayList<List<Element>> graphs = new ArrayList<List<Element>>();

        List<String> graphMaps = Utils.getObjectsFromQuads(store.getQuads(termMap, NS_RR + "graphMap", null));

        for (String graphMap : graphMaps) {
            graphs.add(parseTemplate(getGenericTemplate(graphMap)));
        }

        List<String> graphShortCuts = Utils.getLiteralObjectsFromQuads(store.getQuads(termMap, NS_RR + "graph", null));

        for (String graph : graphShortCuts) {
            ArrayList<Element> temp = new ArrayList<Element>();
            temp.add(new Element(graph, "constant"));
            graphs.add(temp);
        }

        return graphs;
    }
}

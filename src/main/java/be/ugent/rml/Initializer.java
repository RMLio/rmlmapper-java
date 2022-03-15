package be.ugent.rml;

import be.ugent.idlab.knows.functions.agent.Agent;
import be.ugent.idlab.knows.functions.agent.AgentFactory;
import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Initializer {

    private final MappingFactory factory;
    private final QuadStore rmlStore;
    private final FunctionLoader functionLoader;
    private final List<Term> triplesMaps;
    private final HashMap<Term, Mapping> mappings;

    public Initializer(QuadStore rmlStore, FunctionLoader functionLoader, final Agent functionAgent) throws Exception {
        this.rmlStore = rmlStore;
        //we get all the TriplesMaps from the mapping
        this.triplesMaps = this.getAllTriplesMaps();
        this.mappings = new HashMap<Term, Mapping>();

        if (functionLoader == null) {
            this.functionLoader = new FunctionLoader();
        } else {
            this.functionLoader = functionLoader;
        }

        final Agent initialisedFunctionAgent = functionAgent == null ?
                AgentFactory.createFromFnO("functions_idlab.ttl",
                        "https://users.ugent.be/~bjdmeest/function/grel.ttl",
                        "grel_java_mapping.ttl")
                : functionAgent;

        this.factory = new MappingFactory(this.functionLoader, initialisedFunctionAgent);
        extractMappings();
    }

    private void extractMappings() throws Exception {
        for (Term triplesMap : triplesMaps) {
            this.mappings.put(triplesMap, factory.createMapping(triplesMap, rmlStore));
        }
    }

    private List<Term> getAllTriplesMaps() {
        List<Term> maps = Utils.getSubjectsFromQuads(this.rmlStore.getQuads(null, new NamedNode(NAMESPACES.RML + "logicalSource"), null));

        //filter outer Triples Maps that are used for functions
        ArrayList<Term> temp = new ArrayList<>();

        for(Term map: maps) {
            if (this.rmlStore.getQuads(null, new NamedNode(NAMESPACES.FNML + "functionValue"), map).isEmpty()) {
                temp.add(map);
            }
        }

        maps = temp;

        if (maps.isEmpty()) {
            throw new Error("No Triples Maps found. The mapping document you should at least have one Triples Map.");
        } else {
            return maps;
        }
    }

    public HashMap<Term, Mapping> getMappings() {
        return this.mappings;
    }

    public List<Term> getTriplesMaps() {
        return this.triplesMaps;
    }

    public FunctionLoader getFunctionLoader() {
        return this.functionLoader;
    }
}

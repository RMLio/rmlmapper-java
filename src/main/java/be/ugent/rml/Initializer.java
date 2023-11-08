package be.ugent.rml;

import be.ugent.idlab.knows.functions.agent.Agent;
import be.ugent.idlab.knows.functions.agent.AgentFactory;
import be.ugent.rml.store.QuadStore;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Initializer {

    private static final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final MappingFactory factory;
    private final QuadStore rmlStore;
    private final List<Value> triplesMaps;
    private final HashMap<Value, Mapping> mappings;

    public Initializer(final QuadStore rmlStore, final Agent functionAgent, final String baseIRI, final StrictMode strictMode) throws Exception {
        this.rmlStore = rmlStore;
        //we get all the TriplesMaps from the mapping
        this.triplesMaps = this.getAllTriplesMaps();
        this.mappings = new HashMap<Value, Mapping>();


        final Agent initialisedFunctionAgent = functionAgent == null ?
                AgentFactory.createFromFnO("fno/functions_idlab.ttl",
                        "fno/functions_idlab_classes_java_mapping.ttl",
                        "functions_grel.ttl",
                        "grel_java_mapping.ttl")
                : functionAgent;

        this.factory = new MappingFactory(initialisedFunctionAgent, baseIRI, strictMode);
        extractMappings();
    }

    private void extractMappings() throws Exception {
        for (Value triplesMap : triplesMaps) {
            this.mappings.put(triplesMap, factory.createMapping(triplesMap, rmlStore));
        }
    }

    private List<Value> getAllTriplesMaps() {
        List<Value> maps = Utils.getSubjectsFromQuads(this.rmlStore.getQuads(null, valueFactory.createIRI(NAMESPACES.RML + "logicalSource"), null));

        //filter outer Triples Maps that are used for functions
        ArrayList<Value> temp = new ArrayList<>();

        for(Value map: maps) {
            if (this.rmlStore.getQuads(null, valueFactory.createIRI(NAMESPACES.FNML + "functionValue"), map).isEmpty()) {
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

    public HashMap<Value, Mapping> getMappings() {
        return this.mappings;
    }

    public List<Value> getTriplesMaps() {
        return this.triplesMaps;
    }

}

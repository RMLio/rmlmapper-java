package be.ugent.rml;

import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.store.QuadStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Initializer {

    private final MappingFactory factory;
    private QuadStore rmlStore;
    private FunctionLoader functionLoader;
    private List<String> triplesMaps;
    private HashMap<String, Mapping> mappings;

    public Initializer(QuadStore rmlStore, FunctionLoader functionLoader) {
        this.rmlStore = rmlStore;
        //we get all the TriplesMaps from the mapping
        this.triplesMaps = this.getAllTriplesMaps();
        this.mappings = new HashMap<String, Mapping>();

        if (functionLoader == null) {
            this.functionLoader = new FunctionLoader();
        } else {
            this.functionLoader = functionLoader;
        }

        this.factory = new MappingFactory(this.functionLoader);
        extractMappings();
    }

    private void extractMappings() {
        for (String triplesMap : triplesMaps) {
            this.mappings.put(triplesMap, factory.createMapping(triplesMap, rmlStore));
        }
    }

    private List<String> getAllTriplesMaps() {
        List<String> maps = Utils.getSubjectsFromQuads(this.rmlStore.getQuads(null, NAMESPACES.RML + "logicalSource", null));

        //filter outer Triples Maps that are used for functions
        ArrayList<String> temp = new ArrayList<String>();

        for(String map: maps) {
            if (this.rmlStore.getQuads(null, NAMESPACES.FNML + "functionValue", map).isEmpty()) {
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

    public HashMap<String, Mapping> getMappings() {
        return this.mappings;
    }

    public List<String> getTriplesMaps() {
        return this.triplesMaps;
    }
}

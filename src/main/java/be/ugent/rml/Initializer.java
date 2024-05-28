package be.ugent.rml;

import be.ugent.idlab.knows.functions.agent.Agent;
import be.ugent.idlab.knows.functions.agent.AgentFactory;
import be.ugent.rml.store.Quad;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
public class Initializer {

    private final MappingFactory factory;
    private final QuadStore rmlStore;
    private final List<Term> triplesMaps;
    private final Map<Term, Mapping> mappings;

    public Initializer(final QuadStore rmlStore, final Agent functionAgent, final String baseIRI, final StrictMode strictMode) throws Exception {
        this.rmlStore = rmlStore;
        //we get all the TriplesMaps from the mapping
        List<Quad> subjectMapQuads = rmlStore.getQuads(null, new NamedNode(NAMESPACES.RML2 + "subjectMap"), null);
        subjectMapQuads.addAll(rmlStore.getQuads(null, new NamedNode(NAMESPACES.RML2 + "subject"), null));

        this.triplesMaps = subjectMapQuads.stream().map(Quad::getSubject).collect(Collectors.toList());

        this.mappings = new HashMap<>();


        final Agent initialisedFunctionAgent = functionAgent == null ?
                AgentFactory.createFromFnO("fno/functions_idlab.ttl",
                        "fno/functions_idlab_classes_java_mapping.ttl",
                        "fno_idlab_old/functions_idlab.ttl", "fno_idlab_old/functions_idlab_classes_java_mapping.ttl",
                        "functions_grel.ttl",
                        "grel_java_mapping.ttl")
                : functionAgent;

        this.factory = new MappingFactory(initialisedFunctionAgent, baseIRI, strictMode);
        extractMappings();
    }

    private void extractMappings() throws Exception {
        for (Term triplesMap : triplesMaps) {
            this.mappings.put(triplesMap, factory.createMapping(triplesMap, rmlStore));
        }
    }

    public Map<Term, Mapping> getMappings() {
        return this.mappings;
    }
}

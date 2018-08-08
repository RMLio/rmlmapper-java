package be.ugent.rml;

import be.ugent.rml.termgenerator.TermGenerator;

import java.util.List;

public class Mapping {

    private TermGenerator subject;
    private List<PredicateObjectGraphGenerator> predicateObjectGraphGenerators;
    private List<TermGenerator> graphs;

    public Mapping(TermGenerator subject, List<PredicateObjectGraphGenerator> predicateObjectGraphGenerators, List<TermGenerator> graphs) {
        this.subject = subject;
        this.predicateObjectGraphGenerators = predicateObjectGraphGenerators;
        this.graphs = graphs;
    }

    public TermGenerator getSubject() {
        return subject;
    }

    public List<PredicateObjectGraphGenerator> getPredicateObjectGraphGenerators() {
        return predicateObjectGraphGenerators;
    }

    public List<TermGenerator> getGraphs() {
        return graphs;
    }
}

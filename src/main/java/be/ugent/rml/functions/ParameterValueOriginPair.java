package be.ugent.rml.functions;

import be.ugent.rml.termgenerator.TermGenerator;

import java.util.List;

public class ParameterValueOriginPair {

    private List<TermGenerator> parameterGenerators;
    private List<TermGeneratorOriginPair> valueGeneratorPairs;

    public ParameterValueOriginPair(List<TermGenerator> parameterGenerators, List<TermGeneratorOriginPair> valueGeneratorPairs) {
        this.parameterGenerators = parameterGenerators;
        this.valueGeneratorPairs = valueGeneratorPairs;
    }

    public List<TermGenerator> getParameterGenerators() {
        return parameterGenerators;
    }

    public List<TermGeneratorOriginPair> getValueGeneratorPairs() {
        return valueGeneratorPairs;
    }
}

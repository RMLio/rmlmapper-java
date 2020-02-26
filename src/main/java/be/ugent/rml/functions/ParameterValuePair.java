package be.ugent.rml.functions;

import be.ugent.rml.termgenerator.TermGenerator;

import java.util.List;

public class ParameterValuePair {

    private List<TermGenerator> parameterGenerators;
    private List<TermGenerator> valueGenerators;

    public ParameterValuePair(List<TermGenerator> parameterGenerators, List<TermGenerator> valueGenerators) {
        this.parameterGenerators = parameterGenerators;
        this.valueGenerators = valueGenerators;
    }

    public List<TermGenerator> getParameterGenerators() {
        return parameterGenerators;
    }

    public List<TermGenerator> getValueGenerators() {
        return valueGenerators;
    }

    /**
     * To string method
     *
     * @return string
     */
    @Override
    public String toString() {
        return parameterGenerators + "=" + valueGenerators;
    }
}

package be.ugent.rml.functions;

import be.ugent.rml.termgenerator.TermGenerator;

public class TermGeneratorOriginPair {

    private TermGenerator termGenerator;
    private String origin;

    public TermGeneratorOriginPair(TermGenerator termGenerator, String origin) {
        this.termGenerator = termGenerator;
        this.origin = origin;
    }

    public TermGenerator getTermGenerator() {
        return termGenerator;
    }

    public String getOrigin() {
        return origin;
    }
}

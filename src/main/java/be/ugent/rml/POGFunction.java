package be.ugent.rml;

import be.ugent.rml.term.ProvenancedTerm;

@FunctionalInterface
public interface POGFunction {
    void generateQuad(ProvenancedTerm subject, ProvenancedTerm predicate, ProvenancedTerm object, ProvenancedTerm graph, boolean checkMagicValue);
}

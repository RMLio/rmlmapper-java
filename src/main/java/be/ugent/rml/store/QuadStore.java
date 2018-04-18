package be.ugent.rml.store;

import java.util.List;

public interface QuadStore {
    void removeDuplicates();

    void addTriple(String subject, String predicate, String object);

    void addQuad(String subject, String predicate, String object, String graph);

    List<Quad> getQuads(String subject, String predicate, String object, String graph);
    List<Quad> getQuads(String subject, String predicate, String object);
}

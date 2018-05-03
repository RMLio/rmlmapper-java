package be.ugent.rml.store;

import java.util.List;

public class TriplesQuads {

    private List<Quad> triples, quads;

    public TriplesQuads(List<Quad> triples, List<Quad> quads) {
        this.triples = triples;
        this.quads = quads;
    }

    public List<Quad> getTriples() {
        return triples;
    }

    public List<Quad> getQuads() {
        return quads;
    }
}

package be.ugent.rml;

import be.ugent.rml.term.Term;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.store.Quad;
import be.ugent.rml.store.SimpleQuadStore;

import org.junit.Test;
import static org.junit.Assert.*;

public class SimpleQuadStore_Test {
    @Test
    public void get_filtered_quads() {
        String s = "http://example.com/s";
        String p1 = "http://example.com/p1";
        String p2 = "http://example.com/p2";
        String o = "http://example.com/o";

        SimpleQuadStore store = new SimpleQuadStore();

        store.addQuad(new NamedNode(s), new NamedNode(p1), new NamedNode(o), null);
        store.addQuad(new NamedNode(s), new NamedNode(p2), new NamedNode(o), null);

        // get all quads
        assertEquals(2, store.getQuads(null, null, null).size());

        // get quads matching the predicate
        assertEquals(1, store.getQuads(null, new NamedNode(p1), null).size());
    }
}

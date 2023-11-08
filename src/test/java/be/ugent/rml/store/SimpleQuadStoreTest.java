package be.ugent.rml.store;

import be.ugent.rml.term.NamedNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimpleQuadStoreTest {
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

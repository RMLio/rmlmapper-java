package be.ugent.rml.store;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimpleQuadStoreTest {

    private static final ValueFactory valueFactory = SimpleValueFactory.getInstance();


    @Test
    public void get_filtered_quads() {
        String s = "http://example.com/s";
        String p1 = "http://example.com/p1";
        String p2 = "http://example.com/p2";
        String o = "http://example.com/o";

        SimpleQuadStore store = new SimpleQuadStore();

        store.addQuad(valueFactory.createIRI(s), valueFactory.createIRI(p1), valueFactory.createIRI(o), null);
        store.addQuad(valueFactory.createIRI(s), valueFactory.createIRI(p2), valueFactory.createIRI(o), null);

        // get all quads
        assertEquals(2, store.getQuads(null, null, null).size());

        // get quads matching the predicate
        assertEquals(1, store.getQuads(null, valueFactory.createIRI(p1), null).size());
    }
}

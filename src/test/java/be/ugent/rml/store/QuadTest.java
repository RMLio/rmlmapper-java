package be.ugent.rml.store;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QuadTest {

    private static final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Test
    public void compareQuadsStrict() {
        String s = "http://example.com/s";
        String p = "http://example.com/p";
        String o = "http://example.com/o";
        String g = "http://example.com/g";

        Quad q1 = new Quad(valueFactory.createIRI(s), valueFactory.createIRI(p), valueFactory.createIRI(o), valueFactory.createIRI(g));
        Quad q2 = new Quad(valueFactory.createIRI(s), valueFactory.createIRI(p), valueFactory.createIRI(o), valueFactory.createIRI(g));

        assertEquals(0, q1.compareTo(q2));
    }

    @Test
    public void compareQuadsDifferentGraphs() {
        String s = "http://example.com/s";
        String p = "http://example.com/p";
        String o = "http://example.com/o";
        String g1 = "http://example.com/g1";
        String g2 = "http://example.com/g2";

        Quad q1 = new Quad(valueFactory.createIRI(s), valueFactory.createIRI(p), valueFactory.createIRI(o), valueFactory.createIRI(g1));
        Quad q2 = new Quad(valueFactory.createIRI(s), valueFactory.createIRI(p), valueFactory.createIRI(o), valueFactory.createIRI(g2));

        assertEquals(-1, q1.compareTo(q2));
    }

    @Test
    public void compareQuadsDifferentSubjects() {
        String s1 = "http://example.com/s1";
        String s2 = "http://example.com/s2";
        String p = "http://example.com/p";
        String o = "http://example.com/o";
        String g = "http://example.com/g";

        Quad q1 = new Quad(valueFactory.createIRI(s1), valueFactory.createIRI(p), valueFactory.createIRI(o), valueFactory.createIRI(g));
        Quad q2 = new Quad(valueFactory.createIRI(s2), valueFactory.createIRI(p), valueFactory.createIRI(o), valueFactory.createIRI(g));

        assertEquals(-1, q1.compareTo(q2));
    }

    @Test
    public void compareQuadsNullTerms() {
        String s = "http://example.com/s";
        String p = "http://example.com/p";
        String o = "http://example.com/o";
        String g = "http://example.com/g";

        Quad q1 = new Quad(null,             valueFactory.createIRI(p), valueFactory.createIRI(o), valueFactory.createIRI(g));
        Quad q2 = new Quad(valueFactory.createIRI(s), valueFactory.createIRI(p), null,             valueFactory.createIRI(g));

        assertEquals(0, q1.compareTo(q2));
    }

    @Test
    public void compareQuadsOneGraphNull() {
        String s = "http://example.com/s";
        String p = "http://example.com/p";
        String o = "http://example.com/o";
        String g = "http://example.com/g";

        Quad q1 = new Quad(valueFactory.createIRI(s), valueFactory.createIRI(p), valueFactory.createIRI(o), null);
        Quad q2 = new Quad(valueFactory.createIRI(s), valueFactory.createIRI(p), valueFactory.createIRI(o), valueFactory.createIRI(g));

        assertEquals(0, q1.compareTo(q2));
    }
}

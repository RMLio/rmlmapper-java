package be.ugent.rml.store;

import be.ugent.rml.term.NamedNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QuadTest {
    @Test
    public void compareQuadsStrict() {
        String s = "http://example.com/s";
        String p = "http://example.com/p";
        String o = "http://example.com/o";
        String g = "http://example.com/g";

        Quad q1 = new Quad(new NamedNode(s), new NamedNode(p), new NamedNode(o), new NamedNode(g));
        Quad q2 = new Quad(new NamedNode(s), new NamedNode(p), new NamedNode(o), new NamedNode(g));

        assertEquals(0, q1.compareTo(q2));
    }

    @Test
    public void compareQuadsDifferentGraphs() {
        String s = "http://example.com/s";
        String p = "http://example.com/p";
        String o = "http://example.com/o";
        String g1 = "http://example.com/g1";
        String g2 = "http://example.com/g2";

        Quad q1 = new Quad(new NamedNode(s), new NamedNode(p), new NamedNode(o), new NamedNode(g1));
        Quad q2 = new Quad(new NamedNode(s), new NamedNode(p), new NamedNode(o), new NamedNode(g2));

        assertEquals(-1, q1.compareTo(q2));
    }

    @Test
    public void compareQuadsDifferentSubjects() {
        String s1 = "http://example.com/s1";
        String s2 = "http://example.com/s2";
        String p = "http://example.com/p";
        String o = "http://example.com/o";
        String g = "http://example.com/g";

        Quad q1 = new Quad(new NamedNode(s1), new NamedNode(p), new NamedNode(o), new NamedNode(g));
        Quad q2 = new Quad(new NamedNode(s2), new NamedNode(p), new NamedNode(o), new NamedNode(g));

        assertEquals(-1, q1.compareTo(q2));
    }

    @Test
    public void compareQuadsNullTerms() {
        String s = "http://example.com/s";
        String p = "http://example.com/p";
        String o = "http://example.com/o";
        String g = "http://example.com/g";

        Quad q1 = new Quad(null,             new NamedNode(p), new NamedNode(o), new NamedNode(g));
        Quad q2 = new Quad(new NamedNode(s), new NamedNode(p), null,             new NamedNode(g));

        assertEquals(0, q1.compareTo(q2));
    }

    @Test
    public void compareQuadsOneGraphNull() {
        String s = "http://example.com/s";
        String p = "http://example.com/p";
        String o = "http://example.com/o";
        String g = "http://example.com/g";

        Quad q1 = new Quad(new NamedNode(s), new NamedNode(p), new NamedNode(o), null);
        Quad q2 = new Quad(new NamedNode(s), new NamedNode(p), new NamedNode(o), new NamedNode(g));

        assertEquals(0, q1.compareTo(q2));
    }
}

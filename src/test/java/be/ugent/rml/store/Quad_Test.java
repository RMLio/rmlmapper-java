package be.ugent.rml;

import be.ugent.rml.term.Term;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.store.Quad;

import org.junit.Test;
import static org.junit.Assert.*;

public class Quad_Test {
    @Test
    public void compare_quads_strict() {
        String s = "http://example.com/s";
        String p = "http://example.com/p";
        String o = "http://example.com/o";
        String g = "http://example.com/g";

        Quad q1 = new Quad(new NamedNode(s), new NamedNode(p), new NamedNode(o), new NamedNode(g));
        Quad q2 = new Quad(new NamedNode(s), new NamedNode(p), new NamedNode(o), new NamedNode(g));

        assertEquals(0, q1.compareTo(q2));
    }

    @Test
    public void compare_quads_different_graphs() {
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
    public void compare_quads_different_subjects() {
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
    public void compare_quads_null_terms() {
        String s = "http://example.com/s";
        String p = "http://example.com/p";
        String o = "http://example.com/o";
        String g = "http://example.com/g";

        Quad q1 = new Quad(null,             new NamedNode(p), new NamedNode(o), new NamedNode(g));
        Quad q2 = new Quad(new NamedNode(s), new NamedNode(p), null,             new NamedNode(g));

        assertEquals(0, q1.compareTo(q2));
    }
}

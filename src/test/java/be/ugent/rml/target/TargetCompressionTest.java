package be.ugent.rml.target;

import be.ugent.rml.NAMESPACES;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.RDF4JStore;
import be.ugent.rml.term.BlankNode;
import be.ugent.rml.term.Literal;
import be.ugent.rml.term.NamedNode;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * RML-IO gives the compression of a target to the target (rml:Target), as RMLTTC0006b of
 * kg-construct/rml-io does since 0cb26a2.
 */
public class TargetCompressionTest {

    @Test
    public void compressionOnTheTarget() throws Exception {
        Path dir = Files.createTempDirectory("rmlmapper-target");
        QuadStore store = new RDF4JStore();
        NamedNode logicalTarget = new NamedNode("http://example.com/rules/#TargetDump1");
        BlankNode target = new BlankNode("t");
        store.addQuad(logicalTarget, new NamedNode(NAMESPACES.RML2 + "target"), target);
        store.addQuad(logicalTarget, new NamedNode(NAMESPACES.RML2 + "serialization"), new NamedNode(NAMESPACES.FORMATS + "N-Quads"));
        store.addQuad(target, new NamedNode(NAMESPACES.RDF + "type"), new NamedNode(NAMESPACES.RML2 + "FilePath"));
        store.addQuad(target, new NamedNode(NAMESPACES.RML2 + "root"), new NamedNode(NAMESPACES.RML2 + "CurrentWorkingDirectory"));
        store.addQuad(target, new NamedNode(NAMESPACES.RML2 + "path"), new Literal("dump1.nq.gz"));
        store.addQuad(target, new NamedNode(NAMESPACES.RML2 + "compression"), new NamedNode(NAMESPACES.RML2 + "gzip"));

        Target t = new TargetFactory(dir.toString(), dir.toString()).getTarget(logicalTarget, store, new RDF4JStore());
        try (OutputStream out = t.getOutputStream()) {
            out.write("<http://ex/s> <http://ex/p> <http://ex/o> .\n".getBytes());
        }
        byte[] written = Files.readAllBytes(dir.resolve("dump1.nq.gz"));
        // the gzip magic number
        assertEquals((byte) 0x1f, written[0]);
        assertEquals((byte) 0x8b, written[1]);
    }
}

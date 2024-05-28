package be.ugent.rml;

import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class MapperNewRMLIOTargetTest extends TestCore {
    @Test
    public void evaluate_RMLTTC0000() {
        doMapping("./new-test-cases/io/RMLTTC0000/mapping.ttl", "./new-test-cases/io/RMLTTC0000/default.nq");
    }

    @Test
    public void evaluate_RMLTTC0001a() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0001a/dump1.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0001a/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0001a/mapping.ttl", outPaths);
    }

    @Test
    public void evaluate_RMLTTC0001b() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0001b/dump1.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0001b/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0001b/mapping.ttl", outPaths);
    }

    @Test
    public void evaluate_RMLTTC0001c() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0001c/dump1.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0001c/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0001c/mapping.ttl", outPaths);
    }

    @Test
    @Disabled("Output wrong")
    public void evaluate_RMLTTC0001d() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0001d/dump1.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0001d/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0001d/mapping.ttl", outPaths);
    }

    @Test
    public void evaluate_RMLTTC0001e() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0001e/dump1.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0001e/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0001e/mapping.ttl", outPaths);
    }

    @Test
    @Disabled("DatatypeMap not supported yet")
    public void evaluate_RMLTTC0001f() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0001f/dump1.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0001f/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0001f/mapping.ttl", outPaths);
    }

    @Test
    public void evaluate_RMLTTC0002a() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0002a/dump1.nq");
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump2"), "./new-test-cases/io/RMLTTC0002a/dump2.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0002a/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0002a/mapping.ttl", outPaths);
    }

    @Test
    public void evaluate_RMLTTC0002b() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0002b/dump1.nq");
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump2"), "./new-test-cases/io/RMLTTC0002b/dump2.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0002b/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0002b/mapping.ttl", outPaths);
    }

    @Test
    public void evaluate_RMLTTC0002c() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0002c/dump1.nq");
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump2"), "./new-test-cases/io/RMLTTC0002c/dump2.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0002c/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0002c/mapping.ttl", outPaths);
    }

    @Test
    public void evaluate_RMLTTC0002d() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0002d/dump1.nq");
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump2"), "./new-test-cases/io/RMLTTC0002d/dump2.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0002d/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0002d/mapping.ttl", outPaths);
    }

    @Test
    public void evaluate_RMLTTC0002e() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0002e/dump1.nq");
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump2"), "./new-test-cases/io/RMLTTC0002e/dump2.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0002e/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0002e/mapping.ttl", outPaths);
    }

    @Test
    @Disabled("Crashes")
    public void evaluate_RMLTTC0002f() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0002f/dump1.nq");
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump2"), "./new-test-cases/io/RMLTTC0002f/dump2.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0002f/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0002f/mapping.ttl", outPaths);
    }

    @Test
    @Disabled("Output wrong")
    public void evaluate_RMLTTC0002g() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0002g/dump1.nq");
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump2"), "./new-test-cases/io/RMLTTC0002g/dump2.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0002g/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0002g/mapping.ttl", outPaths);
    }

    @Test
    @Disabled("Output wrong")
    public void evaluate_RMLTTC0002h() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0002h/dump1.nq");
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump2"), "./new-test-cases/io/RMLTTC0002h/dump2.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0002h/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0002h/mapping.ttl", outPaths);
    }

    @Test
    @Disabled("Output wrong")
    public void evaluate_RMLTTC0002i() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0002i/dump1.nq");
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump2"), "./new-test-cases/io/RMLTTC0002i/dump2.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0002i/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0002i/mapping.ttl", outPaths);
    }

    @Test
    public void evaluate_RMLTTC0002j() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0002j/dump1.nq");
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump2"), "./new-test-cases/io/RMLTTC0002j/dump2.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0002j/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0002j/mapping.ttl", outPaths);
    }

    @Test
    @Disabled("DatatypeMap not supported yet")
    public void evaluate_RMLTTC0002k() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0002k/dump1.nq");
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump2"), "./new-test-cases/io/RMLTTC0002k/dump2.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0002k/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0002k/mapping.ttl", outPaths);
    }

    @Test
    @Disabled("Output wrong")
    public void evaluate_RMLTTC0002l() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0002l/dump1.nq");
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump2"), "./new-test-cases/io/RMLTTC0002l/dump2.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0002l/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0002l/mapping.ttl", outPaths);
    }

    @Test
    public void evaluate_RMLTTC0002m() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0002m/dump1.nq");
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump2"), "./new-test-cases/io/RMLTTC0002m/dump2.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0002m/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0002m/mapping.ttl", outPaths);
    }

    @Test
    public void evaluate_RMLTTC0002n() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0002n/dump1.nq");
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump2"), "./new-test-cases/io/RMLTTC0002n/dump2.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0002n/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0002n/mapping.ttl", outPaths);
    }

    @Test
    @Disabled("Output wrong")
    public void evaluate_RMLTTC0002o() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0002o/dump1.nq");
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump2"), "./new-test-cases/io/RMLTTC0002o/dump2.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0002o/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0002o/mapping.ttl", outPaths);
    }

    @Test
    public void evaluate_RMLTTC0002p() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0002p/dump1.nq");
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump2"), "./new-test-cases/io/RMLTTC0002p/dump2.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0002p/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0002p/mapping.ttl", outPaths);
    }

    @Test
    @Disabled("DatatypeMap not supported yet")
    public void evaluate_RMLTTC0002q() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0002q/dump1.nq");
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump2"), "./new-test-cases/io/RMLTTC0002q/dump2.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0002q/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0002q/mapping.ttl", outPaths);
    }

    @Test
    public void evaluate_RMLTTC0002r() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0002r/dump1.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0002r/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0002r/mapping.ttl", outPaths);
    }

    @Test
    @Disabled("Output wrong")
    public void evaluate_RMLTTC0003a() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0003a/dump1.nq");
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump2"), "./new-test-cases/io/RMLTTC0003a/dump2.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0003a/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0003a/mapping.ttl", outPaths);
    }

    @Test
    public void evaluate_RMLTTC0004a() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0004a/dump1.jsonld");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0004a/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0004a/mapping.ttl", outPaths);
    }

    @Test
    @Disabled("N3 serialization not implemented")
    public void evaluate_RMLTTC0004b() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0004b/dump1.n3");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0004b/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0004b/mapping.ttl", outPaths);
    }

    @Test
    public void evaluate_RMLTTC0004c() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0004c/dump1.nt");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0004c/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0004c/mapping.ttl", outPaths);
    }

    @Test
    public void evaluate_RMLTTC0004d() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0004d/dump1.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0004d/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0004d/mapping.ttl", outPaths);
    }

    @Test
    @Disabled("RDF/JSON serialization not implemented")
    public void evaluate_RMLTTC0004e() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0004e/dump1.rdfjson");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0004e/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0004e/mapping.ttl", outPaths);
    }

    @Test
    @Disabled("RDF/XML serialization not implemented")
    public void evaluate_RMLTTC0004f() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0004f/dump1.rdfxml");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0004f/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0004f/mapping.ttl", outPaths);
    }

    @Test
    public void evaluate_RMLTTC0004g() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0004g/dump1.ttl");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0004g/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0004g/mapping.ttl", outPaths);
    }

    @Test
    public void evaluate_RMLTTC0005a() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0005a/dump1.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0005a/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0005a/mapping.ttl", outPaths);
    }

    @Test
    public void evaluate_RMLTTC0005b() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0005b/dump1.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0005b/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0005b/mapping.ttl", outPaths);
    }

    @Test
    public void evaluate_RMLTTC0006a() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0006a/dump1.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0006a/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0006a/mapping.ttl", outPaths);
    }

    @Test
    public void evaluate_RMLTTC0006b() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0006b/dump1.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0006b/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0006b/mapping.ttl", outPaths);
    }

    @Test
    public void evaluate_RMLTTC0006c() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0006c/dump1.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0006c/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0006c/mapping.ttl", outPaths);
    }

    @Test
    @Disabled("Compression Tar XZ not implemented")
    public void evaluate_RMLTTC0006d() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0006d/dump1.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0006d/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0006d/mapping.ttl", outPaths);
    }

    @Test
    @Disabled("Compression Tar GZ not implemented")
    public void evaluate_RMLTTC0006e() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0006e/dump1.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0006e/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0006e/mapping.ttl", outPaths);
    }

    @Test
    public void evaluate_RMLTTC0007a() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0007a/dump1.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0007a/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0007a/mapping.ttl", outPaths);
    }

    @Test
    public void evaluate_RMLTTC0007b() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0007b/dump1.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0007b/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0007b/mapping.ttl", outPaths);
    }

    @Test
    @Disabled("TODO")
    public void evaluate_RMLTTC0007c() {
        // TODO: SPARQL endpoint
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0007c/dump1.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0007c/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0007c/mapping.ttl", outPaths);
    }

    @Test
    @Disabled("Target td:Thing not supported")
    public void evaluate_RMLTTC0007d() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump1"), "./new-test-cases/io/RMLTTC0007d/dump1.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0007d/default.nq");
        doMapping("./new-test-cases/io/RMLTTC0007d/mapping.ttl", outPaths);
    }
}

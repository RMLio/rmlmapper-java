package be.ugent.rml;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class MapperNewRMLIOSourceTest extends TestCore {
    @Test
    public void evaluate_RMLSTC0001a() {
        doMapping("./new-test-cases/io/RMLSTC0001a/mapping.ttl", "./new-test-cases/io/RMLSTC0001a/default.nq");
    }

    @Test
    public void evaluate_RMLSTC0001b() {
        doMapping("./new-test-cases/io/RMLSTC0001b/mapping.ttl", "./new-test-cases/io/RMLSTC0001b/default.nq");
    }

    @Test
    public void evaluate_RMLSTC0002a() {
        doMapping("./new-test-cases/io/RMLSTC0002a/mapping.ttl", "./new-test-cases/io/RMLSTC0002a/default.nq");
    }

    @Test
    @Disabled("RML Logical Source compression not supported yet")
    public void evaluate_RMLSTC0002b() {
        doMapping("./new-test-cases/io/RMLSTC0002b/mapping.ttl", "./new-test-cases/io/RMLSTC0002b/default.nq");
    }

    @Test
    @Disabled("RML Logical Source compression not supported yet")
    public void evaluate_RMLSTC0002c() {
        doMapping("./new-test-cases/io/RMLSTC0002c/mapping.ttl", "./new-test-cases/io/RMLSTC0002c/default.nq");
    }

    @Test
    @Disabled("RML Logical Source compression not supported yet")
    public void evaluate_RMLSTC0002d() {
        doMapping("./new-test-cases/io/RMLSTC0002e/mapping.ttl", "./new-test-cases/io/RMLSTC0002e/default.nq");
    }

    @Test
    @Disabled("RML Logical Source compression not supported yet")
    public void evaluate_RMLSTC0002e() {
        doMapping("./new-test-cases/io/RMLSTC0002e/mapping.ttl", "./new-test-cases/io/RMLSTC0002e/default.nq");
    }

    @Test
    @Disabled("RML Logical Source cannot SPARQL local files yet")
    public void evaluate_RMLSTC0003() {
        doMapping("./new-test-cases/io/RMLSTC0003/mapping.ttl", "./new-test-cases/io/RMLSTC0003/default.nq");
    }

    @Test
    @Disabled("RML Logical Source handles CSVW null values incorrect")
    public void evaluate_RMLSTC0004a() {
        doMapping("./new-test-cases/io/RMLSTC0004a/mapping.ttl", "./new-test-cases/io/RMLSTC0004a/default.nq");
    }

    @Test
    public void evaluate_RMLSTC0004b() {
        doMapping("./new-test-cases/io/RMLSTC0004b/mapping.ttl", "./new-test-cases/io/RMLSTC0004b/default.nq");
    }

    @Test
    @Disabled("RML Logical Source handles CSVW null values incorrect")
    public void evaluate_RMLSTC0004c() {
        doMapping("./new-test-cases/io/RMLSTC0004c/mapping.ttl", "./new-test-cases/io/RMLSTC0004c/default.nq");
    }

    @Test
    @Disabled("RML Logical Source cannot find 'id', parsing CSV goes wrong")
    public void evaluate_RMLSTC0005a() {
        doMapping("./new-test-cases/io/RMLSTC0005a/mapping.ttl", "./new-test-cases/io/RMLSTC0005a/default.nq");
    }

    @Test
    @Disabled("RML Logical Source cannot find 'id', parsing CSV goes wrong")
    public void evaluate_RMLSTC0005b() {
        doMapping("./new-test-cases/io/RMLSTC0005b/mapping.ttl", "./new-test-cases/io/RMLSTC0005b/default.nq");
    }

    @Test
    public void evaluate_RMLSTC0006a() {
        doMapping("./new-test-cases/io/RMLSTC0006a/mapping.ttl", "./new-test-cases/io/RMLSTC0006a/default.nq");
    }

    @Test
    @Disabled("RML Logical Source does not support VOID Dataset yet")
    public void evaluate_RMLSTC0006b() {
        doMapping("./new-test-cases/io/RMLSTC0006b/mapping.ttl", "./new-test-cases/io/RMLSTC0006b/default.nq");
    }

    @Test
    @Disabled("java.lang.Error: Unsupported rml:referenceFormulation for a SPARQL source.")
    public void evaluate_RMLSTC0006c() {
        doMapping("./new-test-cases/io/RMLSTC0006c/mapping.ttl", "./new-test-cases/io/RMLSTC0006c/default.nq");
    }

    @Test
    public void evaluate_RMLSTC0006d() {
        doMapping("./new-test-cases/io/RMLSTC0006d/mapping.ttl", "./new-test-cases/io/RMLSTC0006d/default.nq");
    }

    @Test
    public void evaluate_RMLSTC0006e() {
        doMapping("./new-test-cases/io/RMLSTC0006e/mapping.ttl", "./new-test-cases/io/RMLSTC0006e/default.nq");
    }

    @Test
    @Disabled("Turtle parsing failure")
    public void evaluate_RMLSTC0006f() {
        // TODO: driver
        doMapping("./new-test-cases/io/RMLSTC0006f/mapping.ttl", "./new-test-cases/io/RMLSTC0006f/default.nq");
    }

    @Test
    public void evaluate_RMLSTC0007a() {
        doMapping("./new-test-cases/io/RMLSTC0007a/mapping.ttl", "./new-test-cases/io/RMLSTC0007a/default.nq");
    }

    @Test
    @Disabled("RML Logical Source fails parsing CSV with spaces in table, test case problem?")
    public void evaluate_RMLSTC0007b() {
        doMapping("./new-test-cases/io/RMLSTC0007b/mapping.ttl", "./new-test-cases/io/RMLSTC0007b/default.nq");
    }

    @Test
    @Disabled("RML Logical Source fails parsing XML")
    public void evaluate_RMLSTC0007c() {
        doMapping("./new-test-cases/io/RMLSTC0007c/mapping.ttl", "./new-test-cases/io/RMLSTC0007c/default.nq");
    }

    @Test
    @Disabled("RML Logical Source does not support the XML reference formulation with namespaces")
    public void evaluate_RMLSTC0007d() {
        doMapping("./new-test-cases/io/RMLSTC0007d/mapping.ttl", "./new-test-cases/io/RMLSTC0007d/default.nq");
    }
}

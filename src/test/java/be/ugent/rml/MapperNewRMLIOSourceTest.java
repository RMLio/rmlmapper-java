package be.ugent.rml;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class MapperNewRMLIOSourceTest extends TestCore {
    /*
     - Total number of test cases: 32
     - Failures: 10
     - Passes: 22
     */
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
    @Disabled("RML Logical Source n-triples file not supported yet")
    public void evaluate_RMLSTC0003() {
        doMapping("./new-test-cases/io/RMLSTC0003/mapping.ttl", "./new-test-cases/io/RMLSTC0003/default.nq");
    }

    @Test
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
    @Disabled("Needs to configure SQL support")
    public void evaluate_RMLSTC0006a() {
        doMapping("./new-test-cases/io/RMLSTC0006a/mapping.ttl", "./new-test-cases/io/RMLSTC0006a/default.nq");
    }

    @Test
    public void evaluate_RMLSTC0006b() {
        doMapping("./new-test-cases/io/RMLSTC0006b/mapping.ttl", "./new-test-cases/io/RMLSTC0006b/default.nq");
    }

    @Test
    public void evaluate_RMLSTC0007a() {
        doMapping("./new-test-cases/io/RMLSTC0007a/mapping.ttl", "./new-test-cases/io/RMLSTC0007a/default.nq");
    }

    @Test
    public void evaluate_RMLSTC0007b() {
        doMapping("./new-test-cases/io/RMLSTC0007b/mapping.ttl", "./new-test-cases/io/RMLSTC0007b/default.nq");
    }

    @Test
    public void evaluate_RMLSTC0007c() {
        doMapping("./new-test-cases/io/RMLSTC0007c/mapping.ttl", "./new-test-cases/io/RMLSTC0007c/default.nq");
    }

    @Test
    @Disabled("RML Logical Source does not support the XML reference formulation with namespaces")
    public void evaluate_RMLSTC0007d() {
        doMapping("./new-test-cases/io/RMLSTC0007d/mapping.ttl", "./new-test-cases/io/RMLSTC0007d/default.nq");
    }

    @Test
    public void evaluate_RMLSTC0008a() {
        doMapping("./new-test-cases/io/RMLSTC0008a/mapping.ttl", "./new-test-cases/io/RMLSTC0008a/default.nq");
    }

    @Test
    public void evaluate_RMLSTC0008b() {
        doMapping("./new-test-cases/io/RMLSTC0008b/mapping.ttl", "./new-test-cases/io/RMLSTC0008b/default.nq");
    }

    @Test
    public void evaluate_RMLSTC0009a() {
        doMapping("./new-test-cases/io/RMLSTC0009a/mapping.ttl", "./new-test-cases/io/RMLSTC0009a/output.nq");
    }

    @Test
    @Disabled("Error expected but RMLMapper does not.")
    public void evaluate_RMLSTC0010a() {
        doMappingExpectError("./new-test-cases/io/RMLSTC0010a/mapping.ttl", StrictMode.STRICT);
    }

    @Test
    @Disabled("Error expected but RMLMapper does not.")
    public void evaluate_RMLSTC0010b() {
        doMappingExpectError("./new-test-cases/io/RMLSTC0010b/mapping.ttl", StrictMode.STRICT);
    }

    @Test
    public void evaluate_RMLSTC0011a() {
        doMapping("./new-test-cases/io/RMLSTC0011a/mapping.ttl", "./new-test-cases/io/RMLSTC0011a/default.nq");
    }

    @Test
    public void evaluate_RMLSTC0011b() {
        doMapping("./new-test-cases/io/RMLSTC0011b/mapping.ttl", "./new-test-cases/io/RMLSTC0011b/default.nq");
    }

    @Test
    public void evaluate_RMLSTC0011c() {
        doMapping("./new-test-cases/io/RMLSTC0011c/mapping.ttl", "./new-test-cases/io/RMLSTC0011c/default.nq");
    }

    @Test
    public void evaluate_RMLSTC0011d() {
        doMapping("./new-test-cases/io/RMLSTC0011d/mapping.ttl", "./new-test-cases/io/RMLSTC0011d/default.nq");
    }

    @Test
    public void evaluate_RMLSTC0011e() {
        doMapping("./new-test-cases/io/RMLSTC0011e/mapping.ttl", "./new-test-cases/io/RMLSTC0011e/default.nq");
    }

    @Test
    public void evaluate_RMLSTC0012a() {
        doMapping("./new-test-cases/io/RMLSTC0012a/mapping.ttl", "./new-test-cases/io/RMLSTC0012a/default.nq");
    }

    @Test
    public void evaluate_RMLSTC0012b() {
        doMapping("./new-test-cases/io/RMLSTC0012b/mapping.ttl", "./new-test-cases/io/RMLSTC0012b/default.nq");
    }

    @Test
    public void evaluate_RMLSTC0012c() {
        doMapping("./new-test-cases/io/RMLSTC0012c/mapping.ttl", "./new-test-cases/io/RMLSTC0012c/default.nq");
    }

    @Test
    public void evaluate_RMLSTC0012d() {
        doMapping("./new-test-cases/io/RMLSTC0012d/mapping.ttl", "./new-test-cases/io/RMLSTC0012d/default.nq");
    }

    @Test
    public void evaluate_RMLSTC0012e() {
        doMapping("./new-test-cases/io/RMLSTC0012e/mapping.ttl", "./new-test-cases/io/RMLSTC0012e/default.nq");
    }
}

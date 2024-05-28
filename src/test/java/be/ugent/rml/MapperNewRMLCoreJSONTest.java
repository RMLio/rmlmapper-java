package be.ugent.rml;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class MapperNewRMLCoreJSONTest extends TestCore {
    @Test
    public void evaluate_new_0000_JSON() {
        doMapping("./new-test-cases/core/RMLTC0000-JSON/mapping.ttl", "./new-test-cases/core/RMLTC0000-JSON/output.nq");
    }

    @Test
    public void evaluate_new_0001a_JSON() {
        doMapping("./new-test-cases/core/RMLTC0001a-JSON/mapping.ttl", "./new-test-cases/core/RMLTC0001a-JSON/output.nq");
    }

    @Test
    public void evaluate_new_0001b_JSON() {
        doMapping("./new-test-cases/core/RMLTC0001b-JSON/mapping.ttl", "./new-test-cases/core/RMLTC0001b-JSON/output.nq");
    }

    @Test
    public void evaluate_new_0002a_JSON() {
        doMapping("./new-test-cases/core/RMLTC0002a-JSON/mapping.ttl", "./new-test-cases/core/RMLTC0002a-JSON/output.nq");
    }

    @Test
    public void evaluate_new_0002b_JSON() {
        doMapping("./new-test-cases/core/RMLTC0002b-JSON/mapping.ttl", "./new-test-cases/core/RMLTC0002b-JSON/output.nq");
    }

    @Test
    public void evaluate_new_0002e_JSON() {
        doMappingExpectError("./new-test-cases/core/RMLTC0002e-JSON/mapping.ttl");
    }

    @Test
    public void evaluate_new_0003c_JSON() {
        doMapping("./new-test-cases/core/RMLTC0003c-JSON/mapping.ttl", "./new-test-cases/core/RMLTC0003c-JSON/output.nq");
    }

    @Test
    public void evaluate_new_0004a_JSON() {
        doMapping("./new-test-cases/core/RMLTC0004a-JSON/mapping.ttl", "./new-test-cases/core/RMLTC0004a-JSON/output.nq");
    }

    @Test
    public void evaluate_new_0004b_JSON() {
        doMappingExpectError("./new-test-cases/core/RMLTC0004b-JSON/mapping.ttl");
    }

    @Test
    public void evaluate_new_0005a_JSON() {
        doMapping("./new-test-cases/core/RMLTC0005a-JSON/mapping.ttl", "./new-test-cases/core/RMLTC0005a-JSON/output.nq");
    }

    @Test
    public void evaluate_new_0006a_JSON() {
        doMapping("./new-test-cases/core/RMLTC0006a-JSON/mapping.ttl", "./new-test-cases/core/RMLTC0006a-JSON/output.nq");
    }

    @Test
    public void evaluate_new_0007a_JSON() {
        doMapping("./new-test-cases/core/RMLTC0007a-JSON/mapping.ttl", "./new-test-cases/core/RMLTC0007a-JSON/output.nq");
    }

    @Test
    public void evaluate_new_0007b_JSON() {
        doMapping("./new-test-cases/core/RMLTC0007b-JSON/mapping.ttl", "./new-test-cases/core/RMLTC0007b-JSON/output.nq");
    }

    @Test
    public void evaluate_new_0007c_JSON() {
        doMapping("./new-test-cases/core/RMLTC0007c-JSON/mapping.ttl", "./new-test-cases/core/RMLTC0007c-JSON/output.nq");
    }

    @Test
    public void evaluate_new_0007d_JSON() {
        doMapping("./new-test-cases/core/RMLTC0007d-JSON/mapping.ttl", "./new-test-cases/core/RMLTC0007d-JSON/output.nq");
    }

    @Test
    public void evaluate_new_0007e_JSON() {
        doMapping("./new-test-cases/core/RMLTC0007e-JSON/mapping.ttl", "./new-test-cases/core/RMLTC0007e-JSON/output.nq");
    }

    @Test
    public void evaluate_new_0007f_JSON() {
        doMapping("./new-test-cases/core/RMLTC0007f-JSON/mapping.ttl", "./new-test-cases/core/RMLTC0007f-JSON/output.nq");
    }

    @Test
    public void evaluate_new_0007g_JSON() {
        doMapping("./new-test-cases/core/RMLTC0007g-JSON/mapping.ttl", "./new-test-cases/core/RMLTC0007g-JSON/output.nq");
    }

    @Test
    public void evaluate_new_0007h_JSON() {
        doMappingExpectError("./new-test-cases/core/RMLTC0007h-JSON/mapping.ttl");
    }

    @Test
    public void evaluate_new_0008a_JSON() {
        doMapping("./new-test-cases/core/RMLTC0008a-JSON/mapping.ttl", "./new-test-cases/core/RMLTC0008a-JSON/output.nq");
    }

    @Test
    public void evaluate_new_0008b_JSON() {
        doMapping("./new-test-cases/core/RMLTC0008b-JSON/mapping.ttl", "./new-test-cases/core/RMLTC0008b-JSON/output.nq");
    }

    @Test
    public void evaluate_new_0008c_JSON() {
        doMapping("./new-test-cases/core/RMLTC0008c-JSON/mapping.ttl", "./new-test-cases/core/RMLTC0008c-JSON/output.nq");
    }

    @Test
    public void evaluate_new_0009a_JSON() {
        doMapping("./new-test-cases/core/RMLTC0009a-JSON/mapping.ttl", "./new-test-cases/core/RMLTC0009a-JSON/output.nq");
    }

    @Test
    public void evaluate_new_0009b_JSON() {
        doMapping("./new-test-cases/core/RMLTC0009b-JSON/mapping.ttl", "./new-test-cases/core/RMLTC0009b-JSON/output.nq");
    }

    // Needs latest JSONPath IETF support
    @Test
    @Disabled
    public void evaluate_new_0010a_JSON() {
        doMapping("./new-test-cases/core/RMLTC0010a-JSON/mapping.ttl", "./new-test-cases/core/RMLTC0010a-JSON/output.nq");
    }

    // Needs latest JSONPath IETF support
    @Test
    @Disabled
    public void evaluate_new_0010b_JSON() {
        doMapping("./new-test-cases/core/RMLTC0010b-JSON/mapping.ttl", "./new-test-cases/core/RMLTC0010b-JSON/output.nq");
    }

    // Needs latest JSONPath IETF support
    @Test
    @Disabled
    public void evaluate_new_0010c_JSON() {
        doMapping("./new-test-cases/core/RMLTC0010c-JSON/mapping.ttl", "./new-test-cases/core/RMLTC0010c-JSON/output.nq");
    }

    @Test
    public void evaluate_new_0011b_JSON() {
        doMapping("./new-test-cases/core/RMLTC0011b-JSON/mapping.ttl", "./new-test-cases/core/RMLTC0011b-JSON/output.nq");
    }

    @Test
    public void evaluate_new_0012a_JSON() {
        doMapping("./new-test-cases/core/RMLTC0012a-JSON/mapping.ttl", "./new-test-cases/core/RMLTC0012a-JSON/output.nq");
    }

    @Test
    public void evaluate_new_0012b_JSON() {
        doMapping("./new-test-cases/core/RMLTC0012b-JSON/mapping.ttl", "./new-test-cases/core/RMLTC0012b-JSON/output.nq");
    }

    @Test
    public void evaluate_new_0012c_JSON() {
        doMappingExpectError("./new-test-cases/core/RMLTC0012c-JSON/mapping.ttl");
    }

    @Test
    public void evaluate_new_0012d_JSON() {
        doMappingExpectError("./new-test-cases/core/RMLTC0012d-JSON/mapping.ttl");
    }

    @Test
    public void evaluate_new_0013a_JSON() {
        doMapping("./new-test-cases/core/RMLTC0013a-JSON/mapping.ttl", "./new-test-cases/core/RMLTC0013a-JSON/output.nq");
    }

    @Test
    public void evaluate_new_0015a_JSON() {
        doMapping("./new-test-cases/core/RMLTC0015a-JSON/mapping.ttl", "./new-test-cases/core/RMLTC0015a-JSON/output.nq");
    }

    @Test
    public void evaluate_new_0015b_JSON() {
        doMappingExpectError("./new-test-cases/core/RMLTC0015b-JSON/mapping.ttl");
    }

    @Test
    public void evaluate_new_0019a_JSON() {
        doMapping("./new-test-cases/core/RMLTC0019a-JSON/mapping.ttl", "./new-test-cases/core/RMLTC0019a-JSON/output.nq");
    }

    @Test
    public void evaluate_new_0019b_JSON() {
        doMappingExpectError("./new-test-cases/core/RMLTC0019b-JSON/mapping.ttl", StrictMode.STRICT);
    }

    @Test
    public void evaluate_new_0020a_JSON() {
        doMapping("./new-test-cases/core/RMLTC0020a-JSON/mapping.ttl", "./new-test-cases/core/RMLTC0020a-JSON/output.nq");
    }

    @Test
    public void evaluate_new_0021a_JSON() {
        doMapping("./new-test-cases/core/RMLTC0021a-JSON/mapping.ttl", "./new-test-cases/core/RMLTC0021a-JSON/output.nq");
    }
}

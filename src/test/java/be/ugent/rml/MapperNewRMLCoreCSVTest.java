package be.ugent.rml;

import org.junit.jupiter.api.Test;

public class MapperNewRMLCoreCSVTest extends TestCore {
    @Test
    public void evaluate_new_0000_CSV() {
        doMapping("./new-test-cases/core/RMLTC0000-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0000-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0001a_CSV() {
        doMapping("./new-test-cases/core/RMLTC0001a-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0001a-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0001b_CSV() {
        doMapping("./new-test-cases/core/RMLTC0001b-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0001b-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0002a_CSV() {
        doMapping("./new-test-cases/core/RMLTC0002a-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0002a-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0002b_CSV() {
        doMapping("./new-test-cases/core/RMLTC0002b-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0002b-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0002c_CSV() {
        doMappingExpectError("./new-test-cases/core/RMLTC0002c-CSV/mapping.ttl");
    }

    @Test
    public void evaluate_new_0002e_CSV() {
        doMappingExpectError("./new-test-cases/core/RMLTC0002e-CSV/mapping.ttl");
    }

    @Test
    public void evaluate_new_0003c_CSV() {
        doMapping("./new-test-cases/core/RMLTC0003c-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0003c-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0004a_CSV() {
        doMapping("./new-test-cases/core/RMLTC0004a-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0004a-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0004b_CSV() {
        doMappingExpectError("./new-test-cases/core/RMLTC0004b-CSV/mapping.ttl");
    }

    @Test
    public void evaluate_new_0005a_CSV() {
        doMapping("./new-test-cases/core/RMLTC0005a-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0005a-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0006a_CSV() {
        doMapping("./new-test-cases/core/RMLTC0006a-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0006a-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0007a_CSV() {
        doMapping("./new-test-cases/core/RMLTC0007a-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0007a-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0007b_CSV() {
        doMapping("./new-test-cases/core/RMLTC0007b-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0007b-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0007c_CSV() {
        doMapping("./new-test-cases/core/RMLTC0007c-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0007c-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0007d_CSV() {
        doMapping("./new-test-cases/core/RMLTC0007d-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0007d-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0007e_CSV() {
        doMapping("./new-test-cases/core/RMLTC0007e-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0007e-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0007f_CSV() {
        doMapping("./new-test-cases/core/RMLTC0007f-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0007f-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0007g_CSV() {
        doMapping("./new-test-cases/core/RMLTC0007g-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0007g-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0007h_CSV() {
        doMappingExpectError("./new-test-cases/core/RMLTC0007h-CSV/mapping.ttl");
    }

    @Test
    public void evaluate_new_0008a_CSV() {
        doMapping("./new-test-cases/core/RMLTC0008a-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0008a-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0008b_CSV() {
        doMapping("./new-test-cases/core/RMLTC0008b-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0008b-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0008c_CSV() {
        doMapping("./new-test-cases/core/RMLTC0008c-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0008c-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0009a_CSV() {
        doMapping("./new-test-cases/core/RMLTC0009a-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0009a-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0009b_CSV() {
        doMapping("./new-test-cases/core/RMLTC0009b-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0009b-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0010a_CSV() {
        doMapping("./new-test-cases/core/RMLTC0010a-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0010a-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0010b_CSV() {
        doMapping("./new-test-cases/core/RMLTC0010b-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0010b-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0010c_CSV() {
        doMapping("./new-test-cases/core/RMLTC0010c-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0010c-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0011b_CSV() {
        doMapping("./new-test-cases/core/RMLTC0011b-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0011b-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0012a_CSV() {
        doMapping("./new-test-cases/core/RMLTC0012a-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0012a-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0012b_CSV() {
        doMapping("./new-test-cases/core/RMLTC0012b-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0012b-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0012c_CSV() {
        doMappingExpectError("./new-test-cases/core/RMLTC0012c-CSV/mapping.ttl");
    }

    @Test
    public void evaluate_new_0012d_CSV() {
        doMappingExpectError("./new-test-cases/core/RMLTC0012d-CSV/mapping.ttl");
    }

    @Test
    public void evaluate_new_0015a_CSV() {
        doMapping("./new-test-cases/core/RMLTC0015a-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0015a-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0015b_CSV() {
        doMappingExpectError("./new-test-cases/core/RMLTC0015b-CSV/mapping.ttl");
    }

    @Test
    public void evaluate_new_0019a_CSV() {
        doMapping("./new-test-cases/core/RMLTC0019a-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0019a-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0019b_CSV() {
        doMappingExpectError("./new-test-cases/core/RMLTC0019b-CSV/mapping.ttl", StrictMode.STRICT);
    }

    @Test
    public void evaluate_new_0020a_CSV() {
        doMapping("./new-test-cases/core/RMLTC0020a-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0020a-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0021a_CSV() {
        doMapping("./new-test-cases/core/RMLTC0021a-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0021a-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0022a_CSV() {
        doMapping("./new-test-cases/core/RMLTC0022a-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0022a-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0022b_CSV() {
        doMapping("./new-test-cases/core/RMLTC0022b-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0022b-CSV/output.nq");
    }

    @Test
    public void evaluate_new_0022c_CSV() {
        doMapping("./new-test-cases/core/RMLTC0022c-CSV/mapping.ttl", "./new-test-cases/core/RMLTC0022c-CSV/output.nq");
    }
}

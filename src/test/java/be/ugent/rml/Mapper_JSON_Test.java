package be.ugent.rml;

import org.junit.Test;

public class Mapper_JSON_Test extends TestCore {
    @Test
    public void evaluate_0000_JSON() {
        doMapping("./test-cases/RMLTC0000-JSON/mapping.ttl", "./test-cases/RMLTC0000-JSON/output.ttl");
    }

    @Test
    public void evaluate_0001a_JSON() {
        doMapping("./test-cases/RMLTC0001a-JSON/mapping.ttl", "./test-cases/RMLTC0001a-JSON/output.ttl");
    }

    @Test
    public void evaluate_0001b_JSON() {
        doMapping("./test-cases/RMLTC0001b-JSON/mapping.ttl", "./test-cases/RMLTC0001b-JSON/output.ttl");
    }

    @Test
    public void evaluate_0002a_JSON() {
        doMapping("./test-cases/RMLTC0002a-JSON/mapping.ttl", "./test-cases/RMLTC0002a-JSON/output.ttl");
    }

    @Test
    public void evaluate_0002b_JSON() {
        doMapping("./test-cases/RMLTC0002b-JSON/mapping.ttl", "./test-cases/RMLTC0002b-JSON/output.ttl");
    }

    @Test
    public void evaluate_0002c_JSON() {
        doMapping("./test-cases/RMLTC0002c-JSON/mapping.ttl", "./test-cases/RMLTC0002c-JSON/output.ttl");
    }

    @Test
    public void evaluate_0002e_JSON() {
        doMappingExpectError("./test-cases/RMLTC0002e-JSON/mapping.ttl");
    }

    @Test
    public void evaluate_0003c_JSON() {
        doMapping("./test-cases/RMLTC0003c-JSON/mapping.ttl", "./test-cases/RMLTC0003c-JSON/output.ttl");
    }

    @Test
    public void evaluate_0004a_JSON() {
        doMapping("./test-cases/RMLTC0004a-JSON/mapping.ttl", "./test-cases/RMLTC0004a-JSON/output.ttl");
    }

    @Test
    public void evaluate_0004b_JSON() {
        doMapping("./test-cases/RMLTC0004b-JSON/mapping.ttl", "./test-cases/RMLTC0004b-JSON/output.ttl");
    }

    @Test
    public void evaluate_0006a_JSON() {
        doMapping("./test-cases/RMLTC0006a-JSON/mapping.ttl", "./test-cases/RMLTC0006a-JSON/output.nq");
    }

    @Test
    public void evaluate_0007a_JSON() {
        doMapping("./test-cases/RMLTC0007a-JSON/mapping.ttl", "./test-cases/RMLTC0007a-JSON/output.ttl");
    }

    @Test
    public void evaluate_0007b_JSON() {
        doMapping("./test-cases/RMLTC0007b-JSON/mapping.ttl", "./test-cases/RMLTC0007b-JSON/output.nq");
    }

    @Test
    public void evaluate_0007c_JSON() {
        doMapping("./test-cases/RMLTC0007c-JSON/mapping.ttl", "./test-cases/RMLTC0007c-JSON/output.ttl");
    }

    @Test
    public void evaluate_0007d_JSON() {
        doMapping("./test-cases/RMLTC0007d-JSON/mapping.ttl", "./test-cases/RMLTC0007d-JSON/output.ttl");
    }

    @Test
    public void evaluate_0007e_JSON() {
        doMapping("./test-cases/RMLTC0007e-JSON/mapping.ttl", "./test-cases/RMLTC0007e-JSON/output.nq");
    }

    @Test
    public void evaluate_0007f_JSON() {
        doMapping("./test-cases/RMLTC0007f-JSON/mapping.ttl", "./test-cases/RMLTC0007f-JSON/output.nq");
    }

    @Test
    public void evaluate_0007g_JSON() {
        doMapping("./test-cases/RMLTC0007g-JSON/mapping.ttl", "./test-cases/RMLTC0007g-JSON/output.ttl");
    }

    @Test
    public void evaluate_0007h_JSON() {
        doMapping("./test-cases/RMLTC0007h-JSON/mapping.ttl", "./test-cases/RMLTC0007h-JSON/output.ttl");
    }

    @Test
    public void evaluate_0008a_JSON() {
        doMapping("./test-cases/RMLTC0008a-JSON/mapping.ttl", "./test-cases/RMLTC0008a-JSON/output.ttl");
    }
}
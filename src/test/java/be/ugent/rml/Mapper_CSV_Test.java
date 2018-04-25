package be.ugent.rml;

import org.junit.Test;

public class Mapper_CSV_Test extends TestCore {
    @Test
    public void evaluate_0000_CSV() {
        doMapping("./test-cases/RMLTC0000-CSV/mapping.ttl", "./test-cases/RMLTC0000-CSV/output.ttl");
    }

    @Test
    public void evaluate_0001a_CSV() {
        doMapping("./test-cases/RMLTC0001a-CSV/mapping.ttl", "./test-cases/RMLTC0001a-CSV/output.ttl");
    }

    @Test
    public void evaluate_0001b_CSV() {
        doMapping("./test-cases/RMLTC0001b-CSV/mapping.ttl", "./test-cases/RMLTC0001b-CSV/output.ttl");
    }

    @Test
    public void evaluate_0002a_CSV() {
        doMapping("./test-cases/RMLTC0002a-CSV/mapping.ttl", "./test-cases/RMLTC0002a-CSV/output.ttl");
    }

    @Test
    public void evaluate_0002b_CSV() {
        doMapping("./test-cases/RMLTC0002b-CSV/mapping.ttl", "./test-cases/RMLTC0002b-CSV/output.ttl");
    }

    @Test
    public void evaluate_0002c_CSV() {
        doMapping("./test-cases/RMLTC0002c-CSV/mapping.ttl", "./test-cases/RMLTC0002c-CSV/output.ttl");
    }

    @Test
    public void evaluate_0002e_CSV() {
        doMapping("./test-cases/RMLTC0002e-CSV/mapping.ttl", "./test-cases/RMLTC0002e-CSV/output.ttl");
    }

    @Test
    public void evaluate_0003c_CSV() {
        doMapping("./test-cases/RMLTC0003c-CSV/mapping.ttl", "./test-cases/RMLTC0003c-CSV/output.ttl");
    }

    @Test
    public void evaluate_0004a_CSV() {
        doMapping("./test-cases/RMLTC0004a-CSV/mapping.ttl", "./test-cases/RMLTC0004a-CSV/output.ttl");
    }

    @Test
    public void evaluate_0004b_CSV() {
        doMapping("./test-cases/RMLTC0004b-CSV/mapping.ttl", "./test-cases/RMLTC0004b-CSV/output.ttl");
    }

    @Test
    public void evaluate_0006a_CSV() {
        doMapping("./test-cases/RMLTC0006a-CSV/mapping.ttl", "./test-cases/RMLTC0006a-CSV/output.nq");
    }

    @Test
    public void evaluate_0007a_CSV() {
        doMapping("./test-cases/RMLTC0007a-CSV/mapping.ttl", "./test-cases/RMLTC0007a-CSV/output.ttl");
    }

    @Test
    public void evaluate_0007b_CSV() {
        doMapping("./test-cases/RMLTC0007b-CSV/mapping.ttl", "./test-cases/RMLTC0007b-CSV/output.nq");
    }

    @Test
    public void evaluate_0007c_CSV() {
        doMapping("./test-cases/RMLTC0007c-CSV/mapping.ttl", "./test-cases/RMLTC0007c-CSV/output.ttl");
    }

    @Test
    public void evaluate_0007d_CSV() {
        doMapping("./test-cases/RMLTC0007d-CSV/mapping.ttl", "./test-cases/RMLTC0007d-CSV/output.ttl");
    }
}
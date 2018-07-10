package be.ugent.rml;

import org.junit.Test;

public class Mapper_RDBs_Test extends TestCore {

    @Test
    public void evaluate_0001a_RDBs() {
        doMapping("./test-cases/RMLTC0001a-MySQL/mapping.ttl", "test-cases/RMLTC0001a-MySQL/output.ttl");
    }

    @Test
    public void evaluate_0001b_RDBs() {
        doMapping("test-cases/RMLTC0001b-MySQL/mapping.ttl", "test-cases/RMLTC0001b-MySQL/output.ttl");
    }
}

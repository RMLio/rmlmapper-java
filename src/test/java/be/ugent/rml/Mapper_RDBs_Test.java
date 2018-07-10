package be.ugent.rml;

import org.junit.Test;

public class Mapper_RDBs_Test extends TestCore {

    @Test
    public void evaluate_0000_RDBs() {
        doMapping("./test-cases/RMLTC0000-XML/mapping.ttl", "./test-cases/RMLTC0000-XML/output.ttl");
    }
}

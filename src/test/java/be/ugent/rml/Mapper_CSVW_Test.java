package be.ugent.rml;

import org.junit.Test;

public class Mapper_CSVW_Test extends TestCore {
    @Test
    public void evaluate_0002a_CSVW() {
        doMapping("./test-cases/RMLTC0002a-CSVW/mapping.ttl", "./test-cases/RMLTC0002a-CSVW/output.nq");
    }
}
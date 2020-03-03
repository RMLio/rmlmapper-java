package be.ugent.rml;

import org.junit.Test;

public class Custom_RML_FnO_Mapper_JSON_Test extends TestFunctionCore {
    @Test
    public void evaluate_0020_JSON() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0020-JSON/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0020-JSON/output.ttl");
    }

    @Test
    public void evaluate_0021_JSON() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0021-JSON/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0021-JSON/output.ttl");
    }
}

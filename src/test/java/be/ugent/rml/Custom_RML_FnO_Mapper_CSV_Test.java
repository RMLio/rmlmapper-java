package be.ugent.rml;

import org.junit.Ignore;
import org.junit.Test;

public class Custom_RML_FnO_Mapper_CSV_Test extends TestFunctionCore {
    @Test
    public void evaluate_0000_CSV() {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0000-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0000-CSV/output.ttl");
    }

    @Test
    public void evaluate_0001_CSV() {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0001-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0001-CSV/output.ttl");
    }

    @Test
    public void evaluate_0001_CSV_defaultLoad() {
        doMapping("./rml-fno-test-cases/RMLFNOTC0001-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0001-CSV/output.ttl");
    }

    @Test
    public void evaluate_0002_CSV() {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0002-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0002-CSV/output.ttl");
    }

    @Test
    public void evaluate_0003_CSV() {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0003-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0003-CSV/output.ttl");
    }

    @Test
    @Ignore
    public void evaluate_0004_CSV() {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0004-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0004-CSV/output.ttl");
    }
    
    @Test
    public void evaluate_0005_CSV() {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0005-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0005-CSV/output.ttl");
    }

    @Test
    @Ignore
    public void evaluate_0006_CSV() {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0006-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0006-CSV/output.nq");
    }
}
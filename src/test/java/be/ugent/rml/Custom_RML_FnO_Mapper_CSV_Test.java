package be.ugent.rml;

import org.junit.Ignore;
import org.junit.Test;

public class Custom_RML_FnO_Mapper_CSV_Test extends TestFunctionCore {
    @Test
    public void evaluate_0000_CSV() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0000-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0000-CSV/output.ttl");
    }

    @Test
    public void evaluate_0001_CSV() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0001-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0001-CSV/output.ttl");
    }

    @Test
    public void evaluate_0001_CSV_defaultLoad() {
        doMapping("./rml-fno-test-cases/RMLFNOTC0001-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0001-CSV/output.ttl");
    }

    @Test
    public void evaluate_0002_CSV() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0002-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0002-CSV/output.ttl");
    }

    @Test
    public void evaluate_0003_CSV() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0003-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0003-CSV/output.ttl");
    }

    @Test
    public void evaluate_0004_CSV() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0004-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0004-CSV/output.ttl");
    }

    @Test
    public void evaluate_0005_CSV() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0005-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0005-CSV/output.ttl");
    }

    @Test
    public void evaluate_0006_CSV() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0006-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0006-CSV/output.nq");
    }

    @Test
    public void evaluate_0007_CSV() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0007-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0007-CSV/output.ttl");
    }

    @Test
    public void evaluate_0008_CSV() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0008-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0008-CSV/output.ttl");
    }

    @Test
    @Ignore
    public void evaluate_0009_CSV() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0009-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0009-CSV/output.ttl");
    }

    @Test
    @Ignore
    public void evaluate_0010_CSV() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0010-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0010-CSV/output.ttl");
    }

    @Test
    @Ignore
    public void evaluate_0011_CSV() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0011-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0011-CSV/output.ttl");
    }

    @Test
    public void evaluate_0012_CSV() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0012-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0012-CSV/output.ttl");
    }

    @Test
    public void evaluate_0013_CSV() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0013-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0013-CSV/output.ttl");
    }

    @Test
    @Ignore
    public void evaluate_0014_CSV() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0014-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0014-CSV/output.ttl");
    }

    @Test
    @Ignore
    public void evaluate_0015_CSV() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0015-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0015-CSV/output.ttl");
    }

    @Test
    @Ignore
    public void evaluate_0016_CSV() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0016-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0016-CSV/output.nq");
    }

    @Test
    @Ignore
    public void evaluate_0017_CSV() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0017-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0017-CSV/output.ttl");
    }

    @Test
    public void evaluate_0018_CSV() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0018-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0018-CSV/output.ttl");
    }

    @Test
    public void evaluate_0019_CSV() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0019-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0019-CSV/output.ttl");
    }

    @Test
    public void evaluate_0022_CSV() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0022-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0022-CSV/output.nq");
    }

    /**
     * Tests that list-style parameters are handled
     */
    @Test
    public void evaluate_0023_CSV() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0023-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0023-CSV/output.nq");
    }
}

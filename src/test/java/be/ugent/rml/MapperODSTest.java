package be.ugent.rml;

import org.junit.jupiter.api.Test;

public class MapperODSTest extends TestCore {
    @Test
    public void evaluate_0000_ODS() {
        doMapping("./test-cases/RMLTC0000-ODS/mapping.ttl", "./test-cases/RMLTC0000-ODS/output.nq");
    }

    @Test
    public void evaluate_0001a_ODS() {
        doMapping("./test-cases/RMLTC0001a-ODS/mapping.ttl", "./test-cases/RMLTC0001a-ODS/output.nq");
    }

    @Test
    public void evaluate_0001b_ODS() {
        doMapping("./test-cases/RMLTC0001b-ODS/mapping.ttl", "./test-cases/RMLTC0001b-ODS/output.nq");
    }

    @Test
    public void evaluate_0002a_ODS() {
        doMapping("./test-cases/RMLTC0002a-ODS/mapping.ttl", "./test-cases/RMLTC0002a-ODS/output.nq");
    }

    @Test
    public void evaluate_0002b_ODS() {
        doMapping("./test-cases/RMLTC0002b-ODS/mapping.ttl", "./test-cases/RMLTC0002b-ODS/output.nq");
    }

    @Test
    public void evaluate_0002c_ODS() {
        doMapping("./test-cases/RMLTC0002c-ODS/mapping.ttl", "./test-cases/RMLTC0002c-ODS/output.nq");
    }

    @Test
    public void evaluate_0002e_ODS() {
        doMappingExpectError("./test-cases/RMLTC0002e-ODS/mapping.ttl");
    }

    @Test
    public void evaluate_0003c_ODS() {
        doMapping("./test-cases/RMLTC0003c-ODS/mapping.ttl", "./test-cases/RMLTC0003c-ODS/output.nq");
    }

    @Test
    public void evaluate_0004a_ODS() {
        doMapping("./test-cases/RMLTC0004a-ODS/mapping.ttl", "./test-cases/RMLTC0004a-ODS/output.nq");
    }

    @Test
    public void evaluate_0004b_ODS() {
        doMappingExpectError("./test-cases/RMLTC0004b-ODS/mapping.ttl");
    }

    @Test
    public void evaluate_0005a_ODS() {
        doMapping("./test-cases/RMLTC0005a-ODS/mapping.ttl", "./test-cases/RMLTC0005a-ODS/output.nq");
    }

    @Test
    public void evaluate_0006a_ODS() {
        doMapping("./test-cases/RMLTC0006a-ODS/mapping.ttl", "./test-cases/RMLTC0006a-ODS/output.nq");
    }

    @Test
    public void evaluate_0007a_ODS() {
        doMapping("./test-cases/RMLTC0007a-ODS/mapping.ttl", "./test-cases/RMLTC0007a-ODS/output.nq");
    }

    @Test
    public void evaluate_0007b_ODS() {
        doMapping("./test-cases/RMLTC0007b-ODS/mapping.ttl", "./test-cases/RMLTC0007b-ODS/output.nq");
    }

    @Test
    public void evaluate_0007c_ODS() {
        doMapping("./test-cases/RMLTC0007c-ODS/mapping.ttl", "./test-cases/RMLTC0007c-ODS/output.nq");
    }

    @Test
    public void evaluate_0007d_ODS() {
        doMapping("./test-cases/RMLTC0007d-ODS/mapping.ttl", "./test-cases/RMLTC0007d-ODS/output.nq");
    }

    @Test
    public void evaluate_0007e_ODS() {
        doMapping("./test-cases/RMLTC0007e-ODS/mapping.ttl", "./test-cases/RMLTC0007e-ODS/output.nq");
    }

    @Test
    public void evaluate_0007f_ODS() {
        doMapping("./test-cases/RMLTC0007f-ODS/mapping.ttl", "./test-cases/RMLTC0007f-ODS/output.nq");
    }

    @Test
    public void evaluate_0007g_ODS() {
        doMapping("./test-cases/RMLTC0007g-ODS/mapping.ttl", "./test-cases/RMLTC0007g-ODS/output.nq");
    }

    @Test
    public void evaluate_0007h_ODS() {
        doMapping("./test-cases/RMLTC0007h-ODS/mapping.ttl", "./test-cases/RMLTC0007h-ODS/output.nq");
    }

    @Test
    public void evaluate_0008a_ODS() {
        doMapping("./test-cases/RMLTC0008a-ODS/mapping.ttl", "./test-cases/RMLTC0008a-ODS/output.nq");
    }

    @Test
    public void evaluate_0008b_ODS() {
        doMapping("./test-cases/RMLTC0008b-ODS/mapping.ttl", "./test-cases/RMLTC0008b-ODS/output.nq");
    }

    @Test
    public void evaluate_0008c_ODS() {
        doMapping("./test-cases/RMLTC0008c-ODS/mapping.ttl", "./test-cases/RMLTC0008c-ODS/output.nq");
    }

    @Test
    public void evaluate_0009a_ODS() {
        doMapping("./test-cases/RMLTC0009a-ODS/mapping.ttl", "./test-cases/RMLTC0009a-ODS/output.nq");
    }

    @Test
    public void evaluate_0009b_ODS() {
        doMapping("./test-cases/RMLTC0009b-ODS/mapping.ttl", "./test-cases/RMLTC0009b-ODS/output.nq");
    }

    @Test
    public void evaluate_0010a_ODS() {
        doMapping("./test-cases/RMLTC0010a-ODS/mapping.ttl", "./test-cases/RMLTC0010a-ODS/output.nq");
    }

    @Test
    public void evaluate_0010b_ODS() {
        doMapping("./test-cases/RMLTC0010b-ODS/mapping.ttl", "./test-cases/RMLTC0010b-ODS/output.nq");
    }

    @Test
    public void evaluate_0010c_ODS() {
        doMapping("./test-cases/RMLTC0010c-ODS/mapping.ttl", "./test-cases/RMLTC0010c-ODS/output.nq");
    }

    @Test
    public void evaluate_0011b_ODS() {
        doMapping("./test-cases/RMLTC0011b-ODS/mapping.ttl", "./test-cases/RMLTC0011b-ODS/output.nq");
    }

    @Test
    public void evaluate_0012a_ODS() {
        doMapping("./test-cases/RMLTC0012a-ODS/mapping.ttl", "./test-cases/RMLTC0012a-ODS/output.nq");
    }

    @Test
    public void evaluate_0012b_ODS() {
        doMapping("./test-cases/RMLTC0012b-ODS/mapping.ttl", "./test-cases/RMLTC0012b-ODS/output.nq");
    }

    @Test
    public void evaluate_0012c_ODS() {
        doMappingExpectError("./test-cases/RMLTC0012c-ODS/mapping.ttl");
    }

    @Test
    public void evaluate_0012d_ODS() {
        doMappingExpectError("./test-cases/RMLTC0012d-ODS/mapping.ttl");
    }

    @Test
    public void evaluate_0015a_ODS() {
        doMapping("./test-cases/RMLTC0015a-ODS/mapping.ttl", "./test-cases/RMLTC0015a-ODS/output.nq");
    }

    @Test
    public void evaluate_0015b_ODS() {
        doMappingExpectError("./test-cases/RMLTC0015b-ODS/mapping.ttl");
    }

    @Test
    public void evaluate_0019a_ODS() {
        doMapping("./test-cases/RMLTC0019a-ODS/mapping.ttl", "./test-cases/RMLTC0019a-ODS/output.nq");
    }

    @Test
    public void evaluate_0019b_ODS() {
        doMapping("./test-cases/RMLTC0019b-ODS/mapping.ttl", "./test-cases/RMLTC0019b-ODS/output.nq");
    }

    @Test
    public void evaluate_0020a_ODS() {
        doMapping("./test-cases/RMLTC0020a-ODS/mapping.ttl", "./test-cases/RMLTC0020a-ODS/output.nq");
    }

    @Test
    public void evaluate_0020b_ODS() {
        doMapping("./test-cases/RMLTC0020b-ODS/mapping.ttl", "./test-cases/RMLTC0020b-ODS/output.nq");
    }

    @Test
    public void evaluate_1003_ODS() {
        doMapping("./test-cases/RMLTC1003-ODS/mapping.ttl", "./test-cases/RMLTC1003-ODS/output.nq");
    }

    @Test
    public void evaluate_1005a_ODS() {
        doMapping("test-cases/RMLTC1005a-ODS/mapping.ttl", "test-cases/RMLTC1005a-ODS/output.nq");
    }

    @Test
    public void evaluate_1005b_ODS() {
        doMapping("test-cases/RMLTC1005b-ODS/mapping.ttl", "test-cases/RMLTC1005b-ODS/output.nq");
    }

    @Test
    public void evaluate_1007_ODS() {
        doMapping("test-cases/RMLTC1007-ODS/mapping.ttl", "test-cases/RMLTC1007-ODS/output.nq");
    }

    @Test
    public void evaluate_1008_ODS() {
        doMapping("test-cases/RMLTC1008-ODS/mapping.ttl", "test-cases/RMLTC1008-ODS/output.nq");
    }

    @Test
    public void evaluate_1010_ODS() {
        doMapping("test-cases/RMLTC1010-ODS/mapping.ttl", "test-cases/RMLTC1010-ODS/output.nq");
    }

    @Test
    public void evaluate_1012_ODS() {
        doMapping("test-cases/RMLTC1012-ODS/mapping.ttl", "test-cases/RMLTC1012-ODS/output.nq");
    }

    @Test
    public void evaluate_1013_ODS() {
        doMapping("test-cases/RMLTC1013-ODS/mapping.ttl", "test-cases/RMLTC1013-ODS/output.nq");
    }

    @Test
    public void evaluate_1014_ODS() {
        doMapping("test-cases/RMLTC1014-ODS/mapping.ttl", "test-cases/RMLTC1014-ODS/output.nq");
    }

    @Test
    public void evaluate_1015_ODS() {
        doMapping("test-cases/RMLTC1015-ODS/mapping.ttl", "test-cases/RMLTC1015-ODS/output.nq");
    }
}
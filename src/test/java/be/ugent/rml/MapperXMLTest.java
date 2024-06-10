package be.ugent.rml;

import org.junit.jupiter.api.Test;

public class MapperXMLTest extends TestCore {
    @Test
    public void evaluate_0000_XML() {
        doMapping("./test-cases/RMLTC0000-XML/mapping.ttl", "./test-cases/RMLTC0000-XML/output.nq");
    }

    @Test
    public void evaluate_0001a_XML() {
        doMapping("./test-cases/RMLTC0001a-XML/mapping.ttl", "./test-cases/RMLTC0001a-XML/output.nq");
    }

    @Test
    public void evaluate_0001b_XML() {
        doMapping("./test-cases/RMLTC0001b-XML/mapping.ttl", "./test-cases/RMLTC0001b-XML/output.nq");
    }

    @Test
    public void evaluate_0002a_XML() {
        doMapping("./test-cases/RMLTC0002a-XML/mapping.ttl", "./test-cases/RMLTC0002a-XML/output.nq");
    }

    @Test
    public void evaluate_0002b_XML() {
        doMapping("./test-cases/RMLTC0002b-XML/mapping.ttl", "./test-cases/RMLTC0002b-XML/output.nq");
    }

    @Test
    public void evaluate_0002c_XML() {
        doMapping("./test-cases/RMLTC0002c-XML/mapping.ttl", "./test-cases/RMLTC0002c-XML/output.nq");
    }

    @Test
    public void evaluate_0002e_XML() {
        doMappingExpectError("./test-cases/RMLTC0002e-XML/mapping.ttl");
    }

    @Test
    public void evaluate_0003c_XML() {
        doMapping("./test-cases/RMLTC0003c-XML/mapping.ttl", "./test-cases/RMLTC0003c-XML/output.nq");
    }

    @Test
    public void evaluate_0004a_XML() {
        doMapping("./test-cases/RMLTC0004a-XML/mapping.ttl", "./test-cases/RMLTC0004a-XML/output.nq");
    }

    @Test
    public void evaluate_0004b_XML() {
        doMappingExpectError("./test-cases/RMLTC0004b-XML/mapping.ttl");
    }

    @Test
    public void evaluate_0005a_XML() {
        doMapping("./test-cases/RMLTC0005a-XML/mapping.ttl", "./test-cases/RMLTC0005a-XML/output.nq");
    }

    @Test
    public void evaluate_0006a_XML() {
        doMapping("./test-cases/RMLTC0006a-XML/mapping.ttl", "./test-cases/RMLTC0006a-XML/output.nq");
    }

    @Test
    public void evaluate_0007a_XML() {
        doMapping("./test-cases/RMLTC0007a-XML/mapping.ttl", "./test-cases/RMLTC0007a-XML/output.nq");
    }

    @Test
    public void evaluate_0007b_XML() {
        doMapping("./test-cases/RMLTC0007b-XML/mapping.ttl", "./test-cases/RMLTC0007b-XML/output.nq");
    }

    @Test
    public void evaluate_0007c_XML() {
        doMapping("./test-cases/RMLTC0007c-XML/mapping.ttl", "./test-cases/RMLTC0007c-XML/output.nq");
    }

    @Test
    public void evaluate_0007d_XML() {
        doMapping("./test-cases/RMLTC0007d-XML/mapping.ttl", "./test-cases/RMLTC0007d-XML/output.nq");
    }

    @Test
    public void evaluate_0007e_XML() {
        doMapping("./test-cases/RMLTC0007e-XML/mapping.ttl", "./test-cases/RMLTC0007e-XML/output.nq");
    }

    @Test
    public void evaluate_0007f_XML() {
        doMapping("./test-cases/RMLTC0007f-XML/mapping.ttl", "./test-cases/RMLTC0007f-XML/output.nq");
    }

    @Test
    public void evaluate_0007g_XML() {
        doMapping("./test-cases/RMLTC0007g-XML/mapping.ttl", "./test-cases/RMLTC0007g-XML/output.nq");
    }

    @Test
    public void evaluate_0007h_XML() {
        doMappingExpectError("./test-cases/RMLTC0007h-XML/mapping.ttl");
    }

    @Test
    public void evaluate_0008a_XML() {
        doMapping("./test-cases/RMLTC0008a-XML/mapping.ttl", "./test-cases/RMLTC0008a-XML/output.nq");
    }

    @Test
    public void evaluate_0008b_XML() {
        doMapping("./test-cases/RMLTC0008b-XML/mapping.ttl", "./test-cases/RMLTC0008b-XML/output.nq");
    }

    @Test
    public void evaluate_0008c_XML() {
        doMapping("./test-cases/RMLTC0008c-XML/mapping.ttl", "./test-cases/RMLTC0008c-XML/output.nq");
    }

    @Test
    public void evaluate_0009a_XML() {
        doMapping("./test-cases/RMLTC0009a-XML/mapping.ttl", "./test-cases/RMLTC0009a-XML/output.nq");
    }

    @Test
    public void evaluate_0009b_XML() {
        doMapping("./test-cases/RMLTC0009b-XML/mapping.ttl", "./test-cases/RMLTC0009b-XML/output.nq");
    }
    
    @Test
    public void evaluate_0010b_XML() {
        doMapping("./test-cases/RMLTC0010b-XML/mapping.ttl", "./test-cases/RMLTC0010b-XML/output.nq");
    }

    @Test
    public void evaluate_0010c_XML() {
        doMapping("./test-cases/RMLTC0010c-XML/mapping.ttl", "./test-cases/RMLTC0010c-XML/output.nq");
    }

    @Test
    public void evaluate_0011b_XML() {
        doMapping("./test-cases/RMLTC0011b-XML/mapping.ttl", "./test-cases/RMLTC0011b-XML/output.nq");
    }

    @Test
    public void evaluate_0012a_XML() {
        doMapping("./test-cases/RMLTC0012a-XML/mapping.ttl", "./test-cases/RMLTC0012a-XML/output.nq");
    }

    @Test
    public void evaluate_0012b_XML() {
        doMapping("./test-cases/RMLTC0012b-XML/mapping.ttl", "./test-cases/RMLTC0012b-XML/output.nq");
    }

    @Test
    public void evaluate_0012c_XML() {
        doMappingExpectError("./test-cases/RMLTC0012c-XML/mapping.ttl");
    }

    @Test
    public void evaluate_0012d_XML() {
        doMappingExpectError("./test-cases/RMLTC0012d-XML/mapping.ttl");
    }

    @Test
    public void evaluate_0015a_XML() {
        doMapping("./test-cases/RMLTC0015a-XML/mapping.ttl", "./test-cases/RMLTC0015a-XML/output.nq");
    }

    @Test
    public void evaluate_0015b_XML() {
        doMappingExpectError("./test-cases/RMLTC0015b-XML/mapping.ttl");
    }

    @Test
    public void evaluate_0019a_XML() {
        doMapping("./test-cases/RMLTC0019a-XML/mapping.ttl", "./test-cases/RMLTC0019a-XML/output.nq");
    }

    @Test
    public void evaluate_0019b_XML() {
        doMapping("./test-cases/RMLTC0019b-XML/mapping.ttl", "./test-cases/RMLTC0019b-XML/output.nq");
    }

    @Test
    public void evaluate_0020a_XML() {
        doMapping("./test-cases/RMLTC0020a-XML/mapping.ttl", "./test-cases/RMLTC0020a-XML/output.nq");
    }

    @Test
    public void evaluate_0020b_XML() {
        doMapping("./test-cases/RMLTC0020b-XML/mapping.ttl", "./test-cases/RMLTC0020b-XML/output.nq");
    }

    @Test
    public void evaluate_1006_XML() {
        doMapping("./test-cases/RMLTC1006-XML/mapping.ttl", "./test-cases/RMLTC1006-XML/output.nq");
    }

    @Test
    public void evaluate_1011_XML() {
        doMapping("./test-cases/RMLTC1011-XML/mapping.ttl", "./test-cases/RMLTC1011-XML/output.nq");
    }

    @Test
    public void evaluate_1025_XML() {
        doMapping("./test-cases/RMLTC1025-XML/mapping.ttl", "./test-cases/RMLTC1025-XML/output.nq");
    }

    @Test
    public void evaluate_1026_XML() {
        doMapping("./test-cases/RMLTC1026-XML/mapping.ttl", "./test-cases/RMLTC1026-XML/output.nq");
    }

    @Test
    public void evaluate_1027_XML() {
        doMapping("./test-cases/RMLTC1027-XML/mapping.ttl", "./test-cases/RMLTC1027-XML/output.nq");
    }

    @Test
    public void evaluate_1032_XML() {
        doMapping("./test-cases/RMLTC1032-XML/mapping.ttl", "./test-cases/RMLTC1032-XML/output.nq");
    }

    @Test
    public void evaluate_1033_XML() {
        doMapping("./test-cases/RMLTC1033-XML/mapping.ttl", "./test-cases/RMLTC1033-XML/output.nq");
    }

    @Test
    public void evaluate_1034_XML() {
        doMapping("./test-cases/RMLTC1034-XML/mapping.ttl", "./test-cases/RMLTC1034-XML/output.nq");
    }

    @Test
    public void evaluate_1035_XML() {
        doMapping("./test-cases/RMLTC1035-XML/mapping.ttl", "./test-cases/RMLTC1035-XML/output.nq");
    }
}

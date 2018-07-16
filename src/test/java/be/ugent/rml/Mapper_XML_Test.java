package be.ugent.rml;

import org.junit.Test;

public class Mapper_XML_Test extends TestCore {
    @Test
    public void evaluate_0000_XML() {
        doMapping("./test-cases/RMLTC0000-XML/mapping.ttl", "./test-cases/RMLTC0000-XML/output.ttl");
    }

    @Test
    public void evaluate_0001a_XML() {
        doMapping("./test-cases/RMLTC0001a-XML/mapping.ttl", "./test-cases/RMLTC0001a-XML/output.ttl");
    }

    @Test
    public void evaluate_0001b_XML() {
        doMapping("./test-cases/RMLTC0001b-XML/mapping.ttl", "./test-cases/RMLTC0001b-XML/output.ttl");
    }

    @Test
    public void evaluate_0002a_XML() {
        doMapping("./test-cases/RMLTC0002a-XML/mapping.ttl", "./test-cases/RMLTC0002a-XML/output.ttl");
    }

    @Test
    public void evaluate_0002b_XML() {
        doMapping("./test-cases/RMLTC0002b-XML/mapping.ttl", "./test-cases/RMLTC0002b-XML/output.ttl");
    }

    @Test
    public void evaluate_0002c_XML() {
        doMapping("./test-cases/RMLTC0002c-XML/mapping.ttl", "./test-cases/RMLTC0002c-XML/output.ttl");
    }

    @Test
    public void evaluate_0002e_XML() {
        doMappingExpectError("./test-cases/RMLTC0002e-XML/mapping.ttl");
    }

    @Test
    public void evaluate_0003c_XML() {
        doMapping("./test-cases/RMLTC0003c-XML/mapping.ttl", "./test-cases/RMLTC0003c-XML/output.ttl");
    }

    @Test
    public void evaluate_0004a_XML() {
        doMapping("./test-cases/RMLTC0004a-XML/mapping.ttl", "./test-cases/RMLTC0004a-XML/output.ttl");
    }

    @Test
    public void evaluate_0004b_XML() {
        doMapping("./test-cases/RMLTC0004b-XML/mapping.ttl", "./test-cases/RMLTC0004b-XML/output.ttl");
    }

    @Test
    public void evaluate_0006a_XML() {
        doMapping("./test-cases/RMLTC0006a-XML/mapping.ttl", "./test-cases/RMLTC0006a-XML/output.nq");
    }

    @Test
    public void evaluate_0007a_XML() {
        doMapping("./test-cases/RMLTC0007a-XML/mapping.ttl", "./test-cases/RMLTC0007a-XML/output.ttl");
    }

    @Test
    public void evaluate_0007b_XML() {
        doMapping("./test-cases/RMLTC0007b-XML/mapping.ttl", "./test-cases/RMLTC0007b-XML/output.nq");
    }

    @Test
    public void evaluate_0007c_XML() {
        doMapping("./test-cases/RMLTC0007c-XML/mapping.ttl", "./test-cases/RMLTC0007c-XML/output.ttl");
    }

    @Test
    public void evaluate_0007d_XML() {
        doMapping("./test-cases/RMLTC0007d-XML/mapping.ttl", "./test-cases/RMLTC0007d-XML/output.ttl");
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
        doMapping("./test-cases/RMLTC0007g-XML/mapping.ttl", "./test-cases/RMLTC0007g-XML/output.ttl");
    }

    @Test
    public void evaluate_0007h_XML() {
        doMapping("./test-cases/RMLTC0007h-XML/mapping.ttl", "./test-cases/RMLTC0007h-XML/output.nq");
    }

    @Test
    public void evaluate_0008a_XML() {
        doMapping("./test-cases/RMLTC0008a-XML/mapping.ttl", "./test-cases/RMLTC0008a-XML/output.nq");
    }

    @Test
    public void evaluate_0008b_XML() {
        doMapping("./test-cases/RMLTC0008b-XML/mapping.ttl", "./test-cases/RMLTC0008b-XML/output.ttl");
    }

    @Test
    public void evaluate_0008c_XML() {
        doMapping("./test-cases/RMLTC0008c-XML/mapping.ttl", "./test-cases/RMLTC0008c-XML/output.ttl");
    }

    @Test
    public void evaluate_0009a_XML() {
        doMapping("./test-cases/RMLTC0009a-XML/mapping.ttl", "./test-cases/RMLTC0009a-XML/output.ttl");
    }

    @Test
    public void evaluate_0009b_XML() {
        doMapping("./test-cases/RMLTC0009b-XML/mapping.ttl", "./test-cases/RMLTC0009b-XML/output.nq");
    }
    
    @Test
    public void evaluate_0010b_XML() {
        doMapping("./test-cases/RMLTC0010b-XML/mapping.ttl", "./test-cases/RMLTC0010b-XML/output.ttl");
    }

    @Test
    public void evaluate_0010c_XML() {
        doMapping("./test-cases/RMLTC0010c-XML/mapping.ttl", "./test-cases/RMLTC0010c-XML/output.ttl");
    }

    @Test
    public void evaluate_0011b_XML() {
        doMapping("./test-cases/RMLTC0011b-XML/mapping.ttl", "./test-cases/RMLTC0011b-XML/output.ttl");
    }

    @Test
    public void evaluate_0012a_XML() {
        doMapping("./test-cases/RMLTC0012a-XML/mapping.ttl", "./test-cases/RMLTC0012a-XML/output.ttl");
    }

    @Test
    public void evaluate_0012b_XML() {
        doMapping("./test-cases/RMLTC0012b-XML/mapping.ttl", "./test-cases/RMLTC0012b-XML/output.ttl");
    }

    @Test
    public void evaluate_0013_XML() {
        doMapping("./test-cases/RMLTC0013-XML/mapping.ttl", "./test-cases/RMLTC0013-XML/output.nt");
    }
}
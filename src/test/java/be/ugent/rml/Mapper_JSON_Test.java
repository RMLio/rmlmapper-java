package be.ugent.rml;

import be.ugent.rml.store.Quad;
import be.ugent.rml.term.Literal;
import be.ugent.rml.term.NamedNode;
import org.junit.Test;

import java.net.URL;
import java.util.ArrayList;

import static org.junit.Assert.fail;

public class Mapper_JSON_Test extends TestCore {
    @Test
    public void evaluate_0000_JSON() {
        doMapping("./test-cases/RMLTC0000-JSON/mapping.ttl", "./test-cases/RMLTC0000-JSON/output.nq");
    }

    @Test
    public void evaluate_0001a_JSON() {
        doMapping("./test-cases/RMLTC0001a-JSON/mapping.ttl", "./test-cases/RMLTC0001a-JSON/output.nq");
    }

    @Test
    public void evaluate_0001b_JSON() {
        doMapping("./test-cases/RMLTC0001b-JSON/mapping.ttl", "./test-cases/RMLTC0001b-JSON/output.nq");
    }

    @Test
    public void evaluate_0002a_JSON() {
        doMapping("./test-cases/RMLTC0002a-JSON/mapping.ttl", "./test-cases/RMLTC0002a-JSON/output.nq");
    }

    @Test
    public void evaluate_0002b_JSON() {
        doMapping("./test-cases/RMLTC0002b-JSON/mapping.ttl", "./test-cases/RMLTC0002b-JSON/output.nq");
    }

    @Test
    public void evaluate_0002c_JSON() {
        doMapping("./test-cases/RMLTC0002c-JSON/mapping.ttl", "./test-cases/RMLTC0002c-JSON/output.nq");
    }

    @Test
    public void evaluate_0002e_JSON() {
        doMappingExpectError("./test-cases/RMLTC0002e-JSON/mapping.ttl");
    }

    @Test
    public void evaluate_0003c_JSON() {
        doMapping("./test-cases/RMLTC0003c-JSON/mapping.ttl", "./test-cases/RMLTC0003c-JSON/output.nq");
    }

    @Test
    public void evaluate_0004a_JSON() {
        doMapping("./test-cases/RMLTC0004a-JSON/mapping.ttl", "./test-cases/RMLTC0004a-JSON/output.nq");
    }

    @Test
    public void evaluate_0004b_JSON() {
        doMapping("./test-cases/RMLTC0004b-JSON/mapping.ttl", "./test-cases/RMLTC0004b-JSON/output.nq");
    }

    @Test
    public void evaluate_0005a_JSON() {
        doMapping("./test-cases/RMLTC0005a-JSON/mapping.ttl", "./test-cases/RMLTC0005a-JSON/output.nq");
    }

    @Test
    public void evaluate_0006a_JSON() {
        doMapping("./test-cases/RMLTC0006a-JSON/mapping.ttl", "./test-cases/RMLTC0006a-JSON/output.nq");
    }

    @Test
    public void evaluate_0007a_JSON() {
        doMapping("./test-cases/RMLTC0007a-JSON/mapping.ttl", "./test-cases/RMLTC0007a-JSON/output.nq");
    }

    @Test
    public void evaluate_0007b_JSON() {
        doMapping("./test-cases/RMLTC0007b-JSON/mapping.ttl", "./test-cases/RMLTC0007b-JSON/output.nq");
    }

    @Test
    public void evaluate_0007c_JSON() {
        doMapping("./test-cases/RMLTC0007c-JSON/mapping.ttl", "./test-cases/RMLTC0007c-JSON/output.nq");
    }

    @Test
    public void evaluate_0007d_JSON() {
        doMapping("./test-cases/RMLTC0007d-JSON/mapping.ttl", "./test-cases/RMLTC0007d-JSON/output.nq");
    }

    @Test
    public void evaluate_0007e_JSON() {
        doMapping("./test-cases/RMLTC0007e-JSON/mapping.ttl", "./test-cases/RMLTC0007e-JSON/output.nq");
    }

    @Test
    public void evaluate_0007f_JSON() {
        doMapping("./test-cases/RMLTC0007f-JSON/mapping.ttl", "./test-cases/RMLTC0007f-JSON/output.nq");
    }

    @Test
    public void evaluate_0007g_JSON() {
        doMapping("./test-cases/RMLTC0007g-JSON/mapping.ttl", "./test-cases/RMLTC0007g-JSON/output.nq");
    }

    @Test
    public void evaluate_0007h_JSON() {
        doMappingExpectError("./test-cases/RMLTC0007h-JSON/mapping.ttl");
    }

    @Test
    public void evaluate_0008a_JSON() {
        doMapping("./test-cases/RMLTC0008a-JSON/mapping.ttl", "./test-cases/RMLTC0008a-JSON/output.nq");
    }

    @Test
    public void evaluate_0008b_JSON() {
        doMapping("./test-cases/RMLTC0008b-JSON/mapping.ttl", "./test-cases/RMLTC0008b-JSON/output.nq");
    }

    @Test
    public void evaluate_0008c_JSON() {
        doMapping("./test-cases/RMLTC0008c-JSON/mapping.ttl", "./test-cases/RMLTC0008c-JSON/output.nq");
    }

    @Test
    public void evaluate_0009a_JSON() {
        doMapping("./test-cases/RMLTC0009a-JSON/mapping.ttl", "./test-cases/RMLTC0009a-JSON/output.nq");
    }

    @Test
    public void evaluate_0009b_JSON() {
        doMapping("./test-cases/RMLTC0009b-JSON/mapping.ttl", "./test-cases/RMLTC0009b-JSON/output.nq");
    }

    @Test
    public void evaluate_0010a_JSON() {
        doMapping("./test-cases/RMLTC0010a-JSON/mapping.ttl", "./test-cases/RMLTC0010a-JSON/output.nq");
    }
    
    @Test
    public void evaluate_0010b_JSON() {
        doMapping("./test-cases/RMLTC0010b-JSON/mapping.ttl", "./test-cases/RMLTC0010b-JSON/output.nq");
    }

    @Test
    public void evaluate_0010c_JSON() {
        doMapping("./test-cases/RMLTC0010c-JSON/mapping.ttl", "./test-cases/RMLTC0010c-JSON/output.nq");
    }

    @Test
    public void evaluate_0011b_JSON() {
        doMapping("./test-cases/RMLTC0011b-JSON/mapping.ttl", "./test-cases/RMLTC0011b-JSON/output.nq");
    }

    @Test
    public void evaluate_0012a_JSON() {
        doMapping("./test-cases/RMLTC0012a-JSON/mapping.ttl", "./test-cases/RMLTC0012a-JSON/output.nq");
    }

    @Test
    public void evaluate_0012b_JSON() {
        doMapping("./test-cases/RMLTC0012b-JSON/mapping.ttl", "./test-cases/RMLTC0012b-JSON/output.nq");
    }

    @Test
    public void evaluate_0012c_JSON() {
        doMappingExpectError("./test-cases/RMLTC0012c-JSON/mapping.ttl");
    }

    @Test
    public void evaluate_0012d_JSON() {
        doMappingExpectError("./test-cases/RMLTC0012d-JSON/mapping.ttl");
    }

    @Test
    public void evaluate_0013a_JSON() {
        doMapping("./test-cases/RMLTC0013a-JSON/mapping.ttl", "./test-cases/RMLTC0013a-JSON/output.nq");
    }

    @Test
    public void evaluate_0015a_JSON() {
        doMapping("./test-cases/RMLTC0015a-JSON/mapping.ttl", "./test-cases/RMLTC0015a-JSON/output.nq");
    }

    @Test
    public void evaluate_0015b_JSON() {
        doMappingExpectError("./test-cases/RMLTC0015b-JSON/mapping.ttl");
    }

    @Test
    public void evaluate_0019a_JSON() {
        doMapping("./test-cases/RMLTC0019a-JSON/mapping.ttl", "./test-cases/RMLTC0019a-JSON/output.nq");
    }

    @Test
    public void evaluate_0019b_JSON() {
        doMapping("./test-cases/RMLTC0019b-JSON/mapping.ttl", "./test-cases/RMLTC0019b-JSON/output.nq");
    }

    @Test
    public void evaluate_0020a_JSON() {
        doMapping("./test-cases/RMLTC0020a-JSON/mapping.ttl", "./test-cases/RMLTC0020a-JSON/output.nq");
    }

    @Test
    public void evaluate_0020b_JSON() {
        doMapping("./test-cases/RMLTC0020b-JSON/mapping.ttl", "./test-cases/RMLTC0020b-JSON/output.nq");
    }

    @Test
    public void evaluate_1009_JSON() {
        doMapping("./test-cases/RMLTC1009-JSON/mapping.ttl", "./test-cases/RMLTC1009-JSON/output.nq");
    }

    @Test
    public void evaluate_1016_JSON() {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            URL url = classLoader.getResource("./test-cases/RMLTC1016-JSON/data.json");

            ArrayList<Quad> extraQuads = new ArrayList<>();
            extraQuads.add(new Quad(
                    new NamedNode("http://mapping.example.com/source_0"),
                    new NamedNode("http://semweb.mmlab.be/ns/rml#source"),
                    new Literal(url.getFile())));

            Executor executor = createExecutor("./test-cases/RMLTC1016-JSON/mapping.ttl", extraQuads);
            doMapping(executor, "./test-cases/RMLTC1016-JSON/output.nq");
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void evaluate_1021_JSON() {
        doMapping("./test-cases/RMLTC1021-JSON/mapping.ttl", "./test-cases/RMLTC1021-JSON/output.nq");
    }

    @Test
    public void evaluate_1023_JSON() {
        doMapping("./test-cases/RMLTC1023-JSON/mapping.ttl", "./test-cases/RMLTC1023-JSON/output.nq");
    }

    @Test
    public void evaluate_1024_JSON() {
        doMapping("./test-cases/RMLTC1024-JSON/mapping.ttl", "./test-cases/RMLTC1024-JSON/output.nq");
    }
}

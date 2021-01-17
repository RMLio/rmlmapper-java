package be.ugent.rml;

import org.junit.Test;

public class Mapper_Compression extends TestCore {
    @Test
    public void evaluate_compression() {
        doMapping("./web-of-things/compression/mapping.ttl", "./web-of-things/compression/out.nq");
    }
}

package be.ugent.rml;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.Test;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class Mapper_CSV_Test extends TestCore {
    @Test
    public void evaluate_0000_CSV() {
        doMapping("./test-cases/RMLTC0000-CSV/mapping.ttl", "./test-cases/RMLTC0000-CSV/output.nq");
    }

    @Test
    public void evaluate_0001a_CSV() {
        doMapping("./test-cases/RMLTC0001a-CSV/mapping.ttl", "./test-cases/RMLTC0001a-CSV/output.nq");
    }

    @Test
    public void evaluate_0001b_CSV() {
        doMapping("./test-cases/RMLTC0001b-CSV/mapping.ttl", "./test-cases/RMLTC0001b-CSV/output.nq");
    }

    @Test
    public void evaluate_0002a_CSV() {
        doMapping("./test-cases/RMLTC0002a-CSV/mapping.ttl", "./test-cases/RMLTC0002a-CSV/output.nq");
    }

    @Test
    public void evaluate_0002b_CSV() {
        doMapping("./test-cases/RMLTC0002b-CSV/mapping.ttl", "./test-cases/RMLTC0002b-CSV/output.nq");
    }

    @Test
    public void evaluate_0002c_CSV() {
        doMappingExpectError("./test-cases/RMLTC0002c-CSV/mapping.ttl");
    }

    @Test
    public void evaluate_0002e_CSV() {
        doMappingExpectError("./test-cases/RMLTC0002e-CSV/mapping.ttl");
    }

    @Test
    public void evaluate_0003c_CSV() {
        doMapping("./test-cases/RMLTC0003c-CSV/mapping.ttl", "./test-cases/RMLTC0003c-CSV/output.nq");
    }

    @Test
    public void evaluate_0004a_CSV() {
        doMapping("./test-cases/RMLTC0004a-CSV/mapping.ttl", "./test-cases/RMLTC0004a-CSV/output.nq");
    }

    @Test
    public void evaluate_0004b_CSV() {
        doMappingExpectError("./test-cases/RMLTC0004b-CSV/mapping.ttl");
    }

    @Test
    public void evaluate_0005a_CSV() {
        doMapping("./test-cases/RMLTC0005a-CSV/mapping.ttl", "./test-cases/RMLTC0005a-CSV/output.nq");
    }

    @Test
    public void evaluate_0006a_CSV() {
        doMapping("./test-cases/RMLTC0006a-CSV/mapping.ttl", "./test-cases/RMLTC0006a-CSV/output.nq");
    }

    @Test
    public void evaluate_0007a_CSV() {
        doMapping("./test-cases/RMLTC0007a-CSV/mapping.ttl", "./test-cases/RMLTC0007a-CSV/output.nq");
    }

    @Test
    public void evaluate_0007b_CSV() {
        doMapping("./test-cases/RMLTC0007b-CSV/mapping.ttl", "./test-cases/RMLTC0007b-CSV/output.nq");
    }

    @Test
    public void evaluate_0007c_CSV() {
        doMapping("./test-cases/RMLTC0007c-CSV/mapping.ttl", "./test-cases/RMLTC0007c-CSV/output.nq");
    }

    @Test
    public void evaluate_0007d_CSV() {
        doMapping("./test-cases/RMLTC0007d-CSV/mapping.ttl", "./test-cases/RMLTC0007d-CSV/output.nq");
    }

    @Test
    public void evaluate_0007e_CSV() {
        doMapping("./test-cases/RMLTC0007e-CSV/mapping.ttl", "./test-cases/RMLTC0007e-CSV/output.nq");
    }

    @Test
    public void evaluate_0007f_CSV() {
        doMapping("./test-cases/RMLTC0007f-CSV/mapping.ttl", "./test-cases/RMLTC0007f-CSV/output.nq");
    }

    @Test
    public void evaluate_0007g_CSV() {
        doMapping("./test-cases/RMLTC0007g-CSV/mapping.ttl", "./test-cases/RMLTC0007g-CSV/output.nq");
    }

    @Test
    public void evaluate_0007h_CSV() {
        doMappingExpectError("./test-cases/RMLTC0007h-CSV/mapping.ttl");
    }

    @Test
    public void evaluate_0008a_CSV() {
        doMapping("./test-cases/RMLTC0008a-CSV/mapping.ttl", "./test-cases/RMLTC0008a-CSV/output.nq");
    }

    @Test
    public void evaluate_0008b_CSV() {
        doMapping("./test-cases/RMLTC0008b-CSV/mapping.ttl", "./test-cases/RMLTC0008b-CSV/output.nq");
    }

    @Test
    public void evaluate_0008c_CSV() {
        doMapping("./test-cases/RMLTC0008c-CSV/mapping.ttl", "./test-cases/RMLTC0008c-CSV/output.nq");
    }

    @Test
    public void evaluate_0009a_CSV() {
        doMapping("./test-cases/RMLTC0009a-CSV/mapping.ttl", "./test-cases/RMLTC0009a-CSV/output.nq");
    }

    @Test
    public void evaluate_0009b_CSV() {
        doMapping("./test-cases/RMLTC0009b-CSV/mapping.ttl", "./test-cases/RMLTC0009b-CSV/output.nq");
    }

    @Test
    public void evaluate_0010a_CSV() {
        doMapping("./test-cases/RMLTC0010a-CSV/mapping.ttl", "./test-cases/RMLTC0010a-CSV/output.nq");
    }

    @Test
    public void evaluate_0010b_CSV() {
        doMapping("./test-cases/RMLTC0010b-CSV/mapping.ttl", "./test-cases/RMLTC0010b-CSV/output.nq");
    }

    @Test
    public void evaluate_0010c_CSV() {
        doMapping("./test-cases/RMLTC0010c-CSV/mapping.ttl", "./test-cases/RMLTC0010c-CSV/output.nq");
    }

    @Test
    public void evaluate_0011b_CSV() {
        doMapping("./test-cases/RMLTC0011b-CSV/mapping.ttl", "./test-cases/RMLTC0011b-CSV/output.nq");
    }

    @Test
    public void evaluate_0012a_CSV() {
        doMapping("./test-cases/RMLTC0012a-CSV/mapping.ttl", "./test-cases/RMLTC0012a-CSV/output.nq");
    }

    @Test
    public void evaluate_0012b_CSV() {
        doMapping("./test-cases/RMLTC0012b-CSV/mapping.ttl", "./test-cases/RMLTC0012b-CSV/output.nq");
    }

    @Test
    public void evaluate_0012c_CSV() {
        doMappingExpectError("./test-cases/RMLTC0012c-CSV/mapping.ttl");
    }

    @Test
    public void evaluate_0012d_CSV() {
        doMappingExpectError("./test-cases/RMLTC0012d-CSV/mapping.ttl");
    }

    @Test
    public void evaluate_0015a_CSV() {
        doMapping("./test-cases/RMLTC0015a-CSV/mapping.ttl", "./test-cases/RMLTC0015a-CSV/output.nq");
    }

    @Test
    public void evaluate_0015b_CSV() {
        doMappingExpectError("./test-cases/RMLTC0015b-CSV/mapping.ttl");
    }

    @Test
    public void evaluate_0019a_CSV() {
        doMapping("./test-cases/RMLTC0019a-CSV/mapping.ttl", "./test-cases/RMLTC0019a-CSV/output.nq");
    }

    @Test
    public void evaluate_0019b_CSV() {
        doMapping("./test-cases/RMLTC0019b-CSV/mapping.ttl", "./test-cases/RMLTC0019b-CSV/output.nq");
    }

    @Test
    public void evaluate_0020a_CSV() {
        doMapping("./test-cases/RMLTC0020a-CSV/mapping.ttl", "./test-cases/RMLTC0020a-CSV/output.nq");
    }

    @Test
    public void evaluate_0020b_CSV() {
        doMapping("./test-cases/RMLTC0020b-CSV/mapping.ttl", "./test-cases/RMLTC0020b-CSV/output.nq");
    }

    @Test
    public void evaluate_1003_CSV() {
        doMapping("./test-cases/RMLTC1003-CSV/mapping.ttl", "./test-cases/RMLTC1003-CSV/output.nq");
    }

    @Test
    public void evaluate_1003_CSV_mocked() {
        WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .withRootDirectory("src/test/resources/mockedURLs").port(8080));
        wireMockServer.start();
        wireMockServer.stubFor(get(urlMatching("/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("Airport.csv")));
        doMapping("./test-cases/RMLTC1003-CSV/mapping_mocked.ttl", "./test-cases/RMLTC1003-CSV/output.nq");
        wireMockServer.shutdown();
    }

    @Test
    public void evaluate_1005a_CSV() {
        doMapping("test-cases/RMLTC1005a-CSV/mapping.ttl", "test-cases/RMLTC1005a-CSV/output.nq");
    }

    @Test
    public void evaluate_1005b_CSV() {
        doMapping("test-cases/RMLTC1005b-CSV/mapping.ttl", "test-cases/RMLTC1005b-CSV/output.nq");
    }

    @Test
    public void evaluate_1007_CSV() {
        doMapping("test-cases/RMLTC1007-CSV/mapping.ttl", "test-cases/RMLTC1007-CSV/output.nq");
    }

    @Test
    public void evaluate_1008_CSV() {
        doMapping("test-cases/RMLTC1008-CSV/mapping.ttl", "test-cases/RMLTC1008-CSV/output.nq");
    }

    @Test
    public void evaluate_1010_CSV() {
        doMapping("test-cases/RMLTC1010-CSV/mapping.ttl", "test-cases/RMLTC1010-CSV/output.nq");
    }

    @Test
    public void evaluate_1012_CSV() {
        doMapping("test-cases/RMLTC1012-CSV/mapping.ttl", "test-cases/RMLTC1012-CSV/output.nq");
    }

    @Test
    public void evaluate_1013_CSV() {
        doMapping("test-cases/RMLTC1013-CSV/mapping.ttl", "test-cases/RMLTC1013-CSV/output.nq");
    }

    @Test
    public void evaluate_1014_CSV() {
        doMapping("test-cases/RMLTC1014-CSV/mapping.ttl", "test-cases/RMLTC1014-CSV/output.nq");
    }

    @Test
    public void evaluate_1015_CSV() {
        doMapping("test-cases/RMLTC1015-CSV/mapping.ttl", "test-cases/RMLTC1015-CSV/output.nq");
    }

    @Test
    public void evaluate_1017_CSV() {
        doMapping("test-cases/RMLTC1017-CSV/mapping.ttl", "test-cases/RMLTC1017-CSV/output.nq");
    }

    @Test
    public void evaluate_1018_CSV() {
        doMapping("test-cases/RMLTC1018-CSV/mapping.ttl", "test-cases/RMLTC1018-CSV/output.nq");
    }

    @Test
    public void evaluate_1019_CSV() {
        doMapping("test-cases/RMLTC1019-CSV/mapping.ttl", "test-cases/RMLTC1019-CSV/output.nq");
    }

    @Test
    public void evaluate_1021_CSV() {
        doMapping("test-cases/RMLTC1021-CSV/mapping.ttl", "test-cases/RMLTC1021-CSV/output.nq");
    }

    @Test
    public void evaluate_1022_CSV() {
        doMapping("test-cases/RMLTC1022-CSV/mapping.ttl", "test-cases/RMLTC1022-CSV/output.nq");
    }

    @Test
    public void evaluate_1025_CSV() {
        doMapping("test-cases/RMLTC1025-CSV/mapping.ttl", "test-cases/RMLTC1025-CSV/output.nq");
    }

    @Test
    public void evaluate_1027_CSV() {
        doMapping("test-cases/RMLTC1027-CSV/mapping.ttl", "test-cases/RMLTC1027-CSV/output.nq");
    }

    @Test
    public void evaluate_1030_CSV() {
        doMapping("test-cases/RMLTC1030-CSV/mapping.ttl", "test-cases/RMLTC1030-CSV/output.nq");
    }

    @Test
    public void evaluate_1031_CSV() {
        doMapping("test-cases/RMLTC1031-CSV/mapping.ttl", "test-cases/RMLTC1031-CSV/output.nq");
    }
}

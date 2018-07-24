package be.ugent.rml;

import com.googlecode.zohhak.api.TestWith;
import com.googlecode.zohhak.api.runners.ZohhakRunner;

import org.apache.jena.fuseki.embedded.FusekiServer;
import org.apache.jena.query.Dataset;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ZohhakRunner.class)
public class Mapper_SPARQL_Test extends TestCore {

    FusekiServer server;

    private static final int PORT = 3332;

    private void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @TestWith({
            "RMLTC0000-SPARQL, ttl",
            "RMLTC0001a-SPARQL, ttl",
            "RMLTC0001b-SPARQL, ttl",
            "RMLTC0002a-SPARQL, ttl",
            "RMLTC0002b-SPARQL, ttl",
            "RMLTC0002h-SPARQL, ttl",
            "RMLTC0003c-SPARQL, ttl",
            "RMLTC0004a-SPARQL, ttl",
            "RMLTC0004b-SPARQL, ttl",
            "RMLTC0006a-SPARQL, nq",
            "RMLTC0007a-SPARQL, ttl",
            "RMLTC0007b-SPARQL, nq",
            "RMLTC0007c-SPARQL, ttl",
            "RMLTC0007d-SPARQL, ttl",
            "RMLTC0007e-SPARQL, nq",
            "RMLTC0007f-SPARQL, nq",
            "RMLTC0007g-SPARQL, ttl",
            "RMLTC0007h-SPARQL, nq",
            "RMLTC0008a-SPARQL, nq",
            "RMLTC0008b-SPARQL, ttl",
            "RMLTC0008c-SPARQL, ttl",
            "RMLTC0012a-SPARQL, ttl",
    })
    public void evaluate_XXXX_SPARQL(String resourceDir, String outputExtension) throws Exception {
        stopServer();
        String resourcePath = "test-cases/" + resourceDir + "/resource.ttl";
        String mappingPath = "./test-cases/" + resourceDir + "/mapping.ttl";
        String outputPath = "test-cases/" + resourceDir + "/output." + outputExtension;

        Dataset ds = RDFDataMgr.loadDataset(resourcePath);

        server = FusekiServer.create()
                .setPort(PORT)
                .add("/ds", ds, true)
                .build();
        server.start();

        doMapping(mappingPath, outputPath);

        stopServer();
    }

    @Test(expected = Error.class)
    public void evaluate_0002g_SPARQL() {
        stopServer();
        Dataset ds = RDFDataMgr.loadDataset("test-cases/RMLTC0002g-SPARQL/resource.ttl");

        server = FusekiServer.create()
                .setPort(PORT)
                .add("/ds", ds, true)
                .build();
        server.start();

        doMapping("test-cases/RMLTC0002g-SPARQL/mapping.ttl", "test-cases/RMLTC0002g-SPARQL/output.ttl");

        stopServer();
    }

    @Test
    public void evaluate_0009a_SPARQL() throws Exception {
        stopServer();

        Dataset ds1 = RDFDataMgr.loadDataset("test-cases/RMLTC0009a-SPARQL/resource1.ttl");
        Dataset ds2 = RDFDataMgr.loadDataset("test-cases/RMLTC0009a-SPARQL/resource2.ttl");
        server = FusekiServer.create()
                .setPort(PORT)
                .add("/ds1", ds1, true)
                .add("/ds2", ds2, true)
                .build();
        server.start();

        doMapping("test-cases/RMLTC0009a-SPARQL/mapping.ttl", "test-cases/RMLTC0009a-SPARQL/output.ttl");

        stopServer();
    }

    @Test
    public void evaluate_0009b_SPARQL() throws Exception {
        stopServer();

        Dataset ds1 = RDFDataMgr.loadDataset("test-cases/RMLTC0009b-SPARQL/resource1.ttl");
        Dataset ds2 = RDFDataMgr.loadDataset("test-cases/RMLTC0009b-SPARQL/resource2.ttl");
        server = FusekiServer.create()
                .setPort(PORT - 1) // STRANGE ERROR: THIS CASE KEEPS RUNNING INTO "BindException: Address already in use"
                .add("/ds1", ds1, true)
                .add("/ds2", ds2, true)
                .build();
        server.start();

        doMapping("test-cases/RMLTC0009b-SPARQL/mapping.ttl", "test-cases/RMLTC0009b-SPARQL/output.nq");

        stopServer();
    }

    @Test
    public void evaluate_00012b_SPARQL() throws Exception {
        stopServer();

        Dataset ds1 = RDFDataMgr.loadDataset("test-cases/RMLTC0012b-SPARQL/resource1.ttl");
        Dataset ds2 = RDFDataMgr.loadDataset("test-cases/RMLTC0012b-SPARQL/resource2.ttl");
        server = FusekiServer.create()
                .setPort(PORT)
                .add("/ds1", ds1, true)
                .add("/ds2", ds2, true)
                .build();
        server.start();

        doMapping("test-cases/RMLTC0012b-SPARQL/mapping.ttl", "test-cases/RMLTC0012b-SPARQL/output.ttl");

        stopServer();
    }
}

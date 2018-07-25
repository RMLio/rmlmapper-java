package be.ugent.rml;

import com.googlecode.zohhak.api.TestWith;
import com.googlecode.zohhak.api.runners.ZohhakRunner;

import org.apache.commons.io.FileUtils;
import org.apache.jena.fuseki.embedded.FusekiServer;
import org.apache.jena.query.Dataset;
import org.apache.jena.riot.RDFDataMgr;
import org.h2.tools.Server;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;

@RunWith(ZohhakRunner.class)
public class Mapper_SPARQL_Test extends TestCore {

    FusekiServer server;
    HashMap<String, ServerSocket> openPorts = new HashMap<>();

    private static final int PORT = 3332;

    private void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    private ServerSocket findRandomOpenPortOnAllLocalInterfaces() {
        try ( ServerSocket socket = new ServerSocket(0) ) {
            return socket;
        } catch (IOException ex) {
            throw new Error("Couldn't find an available port for the SPARQL tests.");
        }
    }

    private String replacePortInMappingFile(String path) {
        try {
            String mapping = Utils.readFile(path, null);

            ServerSocket openPort = findRandomOpenPortOnAllLocalInterfaces();
            String port = Integer.toString(openPort.getLocalPort());
            mapping.replace("PORT", port);

            String fileName = port + ".txt";
            Path file = Paths.get(port + ".txt");
            Files.write(file, Arrays.asList(mapping.split("\n")));

            openPorts.put(fileName, openPort);
            return fileName;

        } catch (IOException ex) {
            throw new Error(ex.getMessage());
        }
    }

    private void closePort(String fileName) {
        if (openPorts.containsKey(fileName)) {
            try {
                openPorts.get(fileName).close();
            } catch (IOException ex) {
                String withoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
                throw new Error("Couldn't close port " + withoutExtension + " for the SPARQL tests.");
            }
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

        logger.info("TESTING MAPPING FILE: " + mappingPath);

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

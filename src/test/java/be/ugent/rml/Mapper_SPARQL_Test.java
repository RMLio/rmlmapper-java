package be.ugent.rml;

import com.googlecode.zohhak.api.TestWith;
import com.googlecode.zohhak.api.runners.ZohhakRunner;

import org.apache.commons.io.FileUtils;
import org.apache.jena.fuseki.embedded.FusekiServer;
import org.apache.jena.query.Dataset;
import org.apache.jena.riot.RDFDataMgr;
import org.h2.tools.Server;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;

@RunWith(ZohhakRunner.class)
public class Mapper_SPARQL_Test extends TestCore {

    FusekiServer server;
    static HashMap<String, ServerSocket> openPorts = new HashMap<>();
    
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

    /*
        Replaces the "PORT" in the given mapping file to an available port and saves this in a new temp file
        Returns the absolute path to the temp mapping file
     */
    private String replacePortInMappingFile(String path) {
        try {
            // Read mapping file
            String mapping = new String(Files.readAllBytes(Paths.get(Utils.getFile(path, null).getAbsolutePath())), StandardCharsets.UTF_8);

            // Open a new port
            ServerSocket openPort = findRandomOpenPortOnAllLocalInterfaces();
            String port = Integer.toString(openPort.getLocalPort());

            // Replace "PORT" in mapping file by new port
            mapping = mapping.replace("PORT", port);

            // Write to temp mapping file
            String fileName = port + ".txt";
            Path file = Paths.get(port + ".txt");
            Files.write(file, Arrays.asList(mapping.split("\n")));

            String absolutePath = Paths.get(Utils.getFile(fileName, null).getAbsolutePath()).toString();

            openPorts.put(absolutePath, openPort);

            return absolutePath;

        } catch (IOException ex) {
            throw new Error(ex.getMessage());
        }
    }

    /*
        Closes the port used by the given temp mapping file.
        Deletes the temp mapping file.
     */
    private static void closePort(String absolutePath) {
        String portNumber = absolutePath.substring(absolutePath.lastIndexOf('/'), absolutePath.lastIndexOf('.'));
        if (openPorts.containsKey(absolutePath)) {
            try {
                // Close port and remove from map
                openPorts.remove(absolutePath).close();

                // Delete the file
                File file = new File(absolutePath);
                file.delete();
            } catch (IOException ex) {
                throw new Error("Couldn't close port " + portNumber + " for the SPARQL tests.");
            }
        }
    }

    /*
        Makes sure that every opened port is closed and every temp mapping file is deleted.
     */
    @AfterClass
    public static void deleteRemainingFiles() {
        for (String filePath: openPorts.keySet()) {
            closePort(filePath);
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

        String tempMapping = replacePortInMappingFile(mappingPath);

        server = FusekiServer.create()
                .setPort(openPorts.get(tempMapping).getLocalPort())
                .add("/ds", ds, true)
                .build();
        server.start();

        doMapping(tempMapping, outputPath);

        closePort(tempMapping);
        stopServer();
    }

    @Test(expected = Error.class)
    public void evaluate_0002g_SPARQL() {
        stopServer();

        Dataset ds = RDFDataMgr.loadDataset("test-cases/RMLTC0002g-SPARQL/resource.ttl");

        String tempMapping = replacePortInMappingFile("test-cases/RMLTC0002g-SPARQL/mapping.ttl");

        server = FusekiServer.create()
                .setPort(openPorts.get(tempMapping).getLocalPort())
                .add("/ds", ds, true)
                .build();
        server.start();

        doMapping(tempMapping, "test-cases/RMLTC0002g-SPARQL/output.ttl");

        closePort(tempMapping);
        stopServer();
    }

    @Test
    public void evaluate_0009a_SPARQL() throws Exception {
        stopServer();

        Dataset ds1 = RDFDataMgr.loadDataset("test-cases/RMLTC0009a-SPARQL/resource1.ttl");
        Dataset ds2 = RDFDataMgr.loadDataset("test-cases/RMLTC0009a-SPARQL/resource2.ttl");

        String tempMapping = replacePortInMappingFile("test-cases/RMLTC0009a-SPARQL/mapping.ttl");

        server = FusekiServer.create()
                .setPort(openPorts.get(tempMapping).getLocalPort())
                .add("/ds1", ds1, true)
                .add("/ds2", ds2, true)
                .build();
        server.start();

        doMapping(tempMapping, "test-cases/RMLTC0009a-SPARQL/output.ttl");

        closePort(tempMapping);
        stopServer();
    }

    @Test
    public void evaluate_0009b_SPARQL() throws Exception {
        stopServer();

        Dataset ds1 = RDFDataMgr.loadDataset("test-cases/RMLTC0009b-SPARQL/resource1.ttl");
        Dataset ds2 = RDFDataMgr.loadDataset("test-cases/RMLTC0009b-SPARQL/resource2.ttl");

        String tempMapping = replacePortInMappingFile("test-cases/RMLTC0009b-SPARQL/mapping.ttl");

        server = FusekiServer.create()
                .setPort(openPorts.get(tempMapping).getLocalPort())
                .add("/ds1", ds1, true)
                .add("/ds2", ds2, true)
                .build();
        server.start();

        doMapping(tempMapping, "test-cases/RMLTC0009b-SPARQL/output.nq");

        closePort(tempMapping);
        stopServer();
    }

    @Test
    public void evaluate_00012b_SPARQL() throws Exception {
        stopServer();

        Dataset ds1 = RDFDataMgr.loadDataset("test-cases/RMLTC0012b-SPARQL/resource1.ttl");
        Dataset ds2 = RDFDataMgr.loadDataset("test-cases/RMLTC0012b-SPARQL/resource2.ttl");

        String tempMapping = replacePortInMappingFile("test-cases/RMLTC0012b-SPARQL/mapping.ttl");

        server = FusekiServer.create()
                .setPort(openPorts.get(tempMapping).getLocalPort())
                .add("/ds1", ds1, true)
                .add("/ds2", ds2, true)
                .build();
        server.start();

        doMapping(tempMapping, "test-cases/RMLTC0012b-SPARQL/output.ttl");

        closePort(tempMapping);
        stopServer();
    }
}

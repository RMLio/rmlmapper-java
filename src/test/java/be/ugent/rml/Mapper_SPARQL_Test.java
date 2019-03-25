package be.ugent.rml;

import com.googlecode.zohhak.api.TestWith;
import com.googlecode.zohhak.api.runners.ZohhakRunner;

import org.apache.commons.io.FilenameUtils;
import org.apache.jena.fuseki.embedded.FusekiServer;
import org.apache.jena.riot.RDFDataMgr;
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
import java.util.*;

@RunWith(ZohhakRunner.class)
public class Mapper_SPARQL_Test extends TestCore {

    FusekiServer server;
    static HashMap<String, ServerSocket> openPorts = new HashMap<>();
    
    private void stopServer() {
        if (server != null) {
            server.stop();
        }
        System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.StdErrLog");
        System.setProperty("org.eclipse.jetty.LEVEL", "OFF");
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
            String fileName = port + ".ttl";
            Path file = Paths.get(fileName);
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
        String portNumber = FilenameUtils.getBaseName(absolutePath);
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
            "RMLTC0000-SPARQL, nq",
            "RMLTC0001a-SPARQL, nq",
            "RMLTC0001b-SPARQL, nq",
            "RMLTC0002a-SPARQL, nq",
            "RMLTC0002b-SPARQL, nq",
            "RMLTC0002h-SPARQL, nq",
            "RMLTC0003c-SPARQL, nq",
            "RMLTC0004a-SPARQL, nq",
            "RMLTC0004b-SPARQL, nq",
            "RMLTC0006a-SPARQL, nq",
            "RMLTC0007a-SPARQL, nq",
            "RMLTC0007b-SPARQL, nq",
            "RMLTC0007c-SPARQL, nq",
            "RMLTC0007d-SPARQL, nq",
            "RMLTC0007e-SPARQL, nq",
            "RMLTC0007f-SPARQL, nq",
            "RMLTC0007g-SPARQL, nq",
            "RMLTC0007h-SPARQL, nq",
            "RMLTC0008a-SPARQL, nq",
            "RMLTC0008b-SPARQL, nq",
            "RMLTC0008c-SPARQL, nq",
            "RMLTC0012a-SPARQL, nq",
    })
    public void evaluate_XXXX_SPARQL(String resourceDir, String outputExtension) {
        String resourcePath = "test-cases/" + resourceDir + "/resource.ttl";
        String mappingPath = "./test-cases/" + resourceDir + "/mapping.ttl";
        String outputPath = "test-cases/" + resourceDir + "/output." + outputExtension;

        List<String> resources = new ArrayList<>();
        resources.add(resourcePath);

        doTest(mappingPath, resources, outputPath);

    }

    @Test(expected = Error.class)
    public void evaluate_0002g_SPARQL() {
        evaluate_XXXX_SPARQL("RMLTC0002g-SPARQL", "ttl");
    }

    @Test
    public void evaluate_0009a_SPARQL() throws Exception {
        String mappingPath = "test-cases/RMLTC0009a-SPARQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0009a-SPARQL/output.nq";

        List<String> resources = new ArrayList<>();
        resources.add("test-cases/RMLTC0009a-SPARQL/resource1.ttl");
        resources.add("test-cases/RMLTC0009a-SPARQL/resource2.ttl");

        doTest(mappingPath, resources, outputPath);
    }

    @Test
    public void evaluate_0009b_SPARQL() throws Exception {
        String mappingPath = "test-cases/RMLTC0009b-SPARQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0009b-SPARQL/output.nq";

        List<String> resources = new ArrayList<>();
        resources.add("test-cases/RMLTC0009b-SPARQL/resource1.ttl");
        resources.add("test-cases/RMLTC0009b-SPARQL/resource2.ttl");

        doTest(mappingPath, resources, outputPath);
    }

    @Test
    public void evaluate_00012b_SPARQL() throws Exception {
        String mappingPath = "test-cases/RMLTC0012b-SPARQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0012b-SPARQL/output.nq";

        List<String> resources = new ArrayList<>();
        resources.add("test-cases/RMLTC0012b-SPARQL/resource1.ttl");
        resources.add("test-cases/RMLTC0012b-SPARQL/resource2.ttl");

        doTest(mappingPath, resources, outputPath);
    }

    public void doTest(String mappingPath, List<String> resourceFiles, String outputPath) {
        stopServer();
        String tempMapping = replacePortInMappingFile(mappingPath);

        FusekiServer.Builder builder = FusekiServer.create();
        builder.setPort(openPorts.get(tempMapping).getLocalPort());

        for (int i = 0; i < resourceFiles.size(); i++) {
            builder.add("/ds"+(i+1), RDFDataMgr.loadDataset(resourceFiles.get(i)), true);
        }
        this.server = builder.build();
        this.server.start();

        doMapping(tempMapping, outputPath);

        closePort(tempMapping);
        stopServer();
    }
}

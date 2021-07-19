package be.ugent.rml;

import org.apache.commons.io.FilenameUtils;
import org.apache.jena.fuseki.embedded.FusekiServer;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static be.ugent.rml.MySQLTestCore.deleteTempMappingFile;

@RunWith(Parameterized.class)
public class Mapper_SPARQL_Test extends TestCore {

    private static int PORTNUMBER_SPARQL;
    private FusekiServer.Builder builder;
    private FusekiServer server;

    @Parameterized.Parameter(0)
    public String testCaseName;

    @Parameterized.Parameter(1)
    public Class<? extends Exception> expectedException;

    @Parameterized.Parameters(name = "{index}: SPARQL_{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                // scenarios:
                {"RMLTC0000", null},
                {"RMLTC0001a", null},
                {"RMLTC0001b", null},
                {"RMLTC0002a", null},
//                {"RMLTC0002b", null}, // TODO: check if needs to be added
//                {"RMLTC0002c", Error.class}, // TODO: check if needs to be added
//                {"RMLTC0002d", null}, // TODO: check if needs to be added
//                {"RMLTC0002e", Error.class}, // TODO: check if needs to be added
//                {"RMLTC0002f", null}, // TODO: check if needs to be added
                {"RMLTC0002g", Error.class},
//                {"RMLTC0002h", Error.class}, // TODO: fails
//                {"RMLTC0002i", Error.class},
//                {"RMLTC0002j", null}, // TODO: check if needs to be added
//                {"RMLTC0003a", Error.class}, // TODO: check if needs to be added
//                {"RMLTC0003b", null}, // TODO: check if needs to be added
                {"RMLTC0003c", null},
                {"RMLTC0004a", null},
                {"RMLTC0004b", Error.class},
//                {"RMLTC0005a", null}, // TODO: check if needs to be added
//                {"RMLTC0005b", null}, // TODO: check if needs to be added
                {"RMLTC0006a", null},
                {"RMLTC0007a", null},
                {"RMLTC0007b", null},
                {"RMLTC0007c", null},
                {"RMLTC0007d", null},
                {"RMLTC0007e", null},
                {"RMLTC0007f", null},
                {"RMLTC0007g", null},
                {"RMLTC0007h", Error.class},
                {"RMLTC0008a", null},
                {"RMLTC0008b", null},
                {"RMLTC0008c", null},
//                {"RMLTC0009a", null}, // TODO: fails
//                {"RMLTC0009b", null}, // TODO: fails
//                {"RMLTC0009c", null}, // TODO: check if needs to be added
//                {"RMLTC0009d", null}, // TODO: check if needs to be added
//                {"RMLTC0010a", null}, // TODO: check if needs to be added
//                {"RMLTC0010b", null}, // TODO: check if needs to be added
//                {"RMLTC0010c", null}, // TODO: check if needs to be added
//                {"RMLTC0011a", null}, // TODO: check if needs to be added
//                {"RMLTC0011b", null}, // TODO: check if needs to be added
                {"RMLTC0012a", null},
//                {"RMLTC0012b", null}, // TODO: fails
//                {"RMLTC0012c", Error.class}, // TODO: check if needs to be added
//                {"RMLTC0012d", Error.class}, // TODO: check if needs to be added
//                {"RMLTC0012e", null}, // TODO: check if needs to be added
//                {"RMLTC0013a", null}, // TODO: check if needs to be added
//                {"RMLTC0014d", null}, // TODO: check if needs to be added
//                {"RMLTC0015a", null}, // TODO: check if needs to be added
//                {"RMLTC0015b", Error.class}, // TODO: check if needs to be added
//                {"RMLTC0016a", null}, // TODO: check if needs to be added
//                {"RMLTC0016b", null}, // TODO: check if needs to be added
//                {"RMLTC0016c", null}, // TODO: check if needs to be added
//                {"RMLTC0016d", null}, // TODO: check if needs to be added
//                {"RMLTC0016e", null}, // TODO: check if needs to be added
//                {"RMLTC0018a", null}, // TODO: check if needs to be added
//                {"RMLTC0019a", null}, // TODO: check if needs to be added
//                {"RMLTC0019b", null}, // TODO: check if needs to be added
//                {"RMLTC0020a", null}, // TODO: check if needs to be added
//                {"RMLTC0020b", null}, // TODO: check if needs to be added
        });
    }

    @Before
    public void intialize() {
        builder = FusekiServer.create();
        builder.setPort(PORTNUMBER_SPARQL);
    }

    @BeforeClass
    public static void startServer() throws Exception {
        try {
            PORTNUMBER_SPARQL = Utils.getFreePortNumber();
        } catch (Exception ex) {
            throw new Exception("Could not find a free port number for SPARQL testing.");
        }
    }

    @Test
    public void doMapping() throws Exception {
        mappingTest(testCaseName, expectedException);
    }

    private void mappingTest(String testCaseName, Class expectedException) throws Exception {
        String resourcePath = "test-cases/" + testCaseName + "-SPARQL/resource.ttl";
        String mappingPath = "./test-cases/" + testCaseName + "-SPARQL/mapping.ttl";
        String outputPath = "test-cases/" + testCaseName + "-SPARQL/output.nq";
        String tempMappingPath = replacePortInMappingFile(mappingPath, "" + PORTNUMBER_SPARQL);

        builder.add("/ds"+(1), RDFDataMgr.loadDataset(resourcePath), true);
        this.server = builder.build();
        this.server.start();

        // mapping
        if (expectedException == null) {
            doMapping(tempMappingPath, outputPath);
        } else {
            doMappingExpectError(tempMappingPath);
        }

        deleteTempMappingFile(tempMappingPath);
    }

    @After
    public void stopServer() {
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
    private String replacePortInMappingFile(String path, String port) {
        try {
            // Read mapping file
            String mapping = new String(Files.readAllBytes(Paths.get(Utils.getFile(path).getAbsolutePath())), StandardCharsets.UTF_8);

            // Replace "PORT" in mapping file by new port
            mapping = mapping.replace("PORT", port);

            // Write to temp mapping file
            String fileName = port + ".ttl";
            Path file = Paths.get(fileName);
            Files.write(file, Arrays.asList(mapping.split("\n")));

            String absolutePath = Paths.get(Utils.getFile(fileName, null).getAbsolutePath()).toString();

            return absolutePath;
        } catch (IOException ex) {
            throw new Error(ex.getMessage());
        }
    }
}

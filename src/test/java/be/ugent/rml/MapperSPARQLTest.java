package be.ugent.rml;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

import static be.ugent.rml.DBTestCore.deleteTempMappingFile;

public class MapperSPARQLTest extends TestCore {

    private static int PORTNUMBER_SPARQL;
    private FusekiServer.Builder builder;
    private FusekiServer server;

    public static Stream<Arguments> data() {
        return Stream.of(
                // scenarios:
                Arguments.of("RMLTC0000", false),
                Arguments.of("RMLTC0001a", false),
                Arguments.of("RMLTC0001b", false),
                Arguments.of("RMLTC0002a", false),
//                Arguments.of("RMLTC0002b", false), // TODO: check if needs to be added
//                Arguments.of("RMLTC0002c", true), // TODO: check if needs to be added
//                Arguments.of("RMLTC0002d", false), // TODO: check if needs to be added
//                Arguments.of("RMLTC0002e", true), // TODO: check if needs to be added
//                Arguments.of("RMLTC0002f", false), // TODO: check if needs to be added
                Arguments.of("RMLTC0002g", true),
//                Arguments.of("RMLTC0002h", true), // TODO: fails
//                Arguments.of("RMLTC0002i", true),
//                Arguments.of("RMLTC0002j", false), // TODO: check if needs to be added
//                Arguments.of("RMLTC0003a", true), // TODO: check if needs to be added
//                Arguments.of("RMLTC0003b", false), // TODO: check if needs to be added
                Arguments.of("RMLTC0003c", false),
                Arguments.of("RMLTC0004a", false),
                Arguments.of("RMLTC0004b", true),
//                Arguments.of("RMLTC0005a", false), // TODO: check if needs to be added
//                Arguments.of("RMLTC0005b", false), // TODO: check if needs to be added
                Arguments.of("RMLTC0006a", false),
                Arguments.of("RMLTC0007a", false),
                Arguments.of("RMLTC0007b", false),
                Arguments.of("RMLTC0007c", false),
                Arguments.of("RMLTC0007d", false),
                Arguments.of("RMLTC0007e", false),
                Arguments.of("RMLTC0007f", false),
                Arguments.of("RMLTC0007g", false),
                Arguments.of("RMLTC0007h", true),
                Arguments.of("RMLTC0008a", false),
                Arguments.of("RMLTC0008b", false),
                Arguments.of("RMLTC0008c", false),
//                Arguments.of("RMLTC0009a", false), // TODO: fails
//                Arguments.of("RMLTC0009b", false), // TODO: fails
//                Arguments.of("RMLTC0009c", false), // TODO: check if needs to be added
//                Arguments.of("RMLTC0009d", false), // TODO: check if needs to be added
//                Arguments.of("RMLTC0010a", false), // TODO: check if needs to be added
//                Arguments.of("RMLTC0010b", false), // TODO: check if needs to be added
//                Arguments.of("RMLTC0010c", false), // TODO: check if needs to be added
//                Arguments.of("RMLTC0011a", false), // TODO: check if needs to be added
//                Arguments.of("RMLTC0011b", false), // TODO: check if needs to be added
                Arguments.of("RMLTC0012a", false),
//                Arguments.of("RMLTC0012b", false), // TODO: fails
//                Arguments.of("RMLTC0012c", true), // TODO: check if needs to be added
//                Arguments.of("RMLTC0012d", true), // TODO: check if needs to be added
//                Arguments.of("RMLTC0012e", false), // TODO: check if needs to be added
//                Arguments.of("RMLTC0013a", false), // TODO: check if needs to be added
//                Arguments.of("RMLTC0014d", false), // TODO: check if needs to be added
//                Arguments.of("RMLTC0015a", false), // TODO: check if needs to be added
//                Arguments.of("RMLTC0015b", true), // TODO: check if needs to be added
//                Arguments.of("RMLTC0016a", false), // TODO: check if needs to be added
//                Arguments.of("RMLTC0016b", false), // TODO: check if needs to be added
//                Arguments.of("RMLTC0016c", false), // TODO: check if needs to be added
//                Arguments.of("RMLTC0016d", false), // TODO: check if needs to be added
//                Arguments.of("RMLTC0016e", false), // TODO: check if needs to be added
//                Arguments.of("RMLTC0018a", false), // TODO: check if needs to be added
//                Arguments.of("RMLTC0019a", false), // TODO: check if needs to be added
//                Arguments.of("RMLTC0019b", false), // TODO: check if needs to be added
//                Arguments.of("RMLTC0020a", false), // TODO: check if needs to be added
//                Arguments.of("RMLTC0020b", false), // TODO: check if needs to be added
                Arguments.of("RMLTC1029a", false)
        );
    }

    @BeforeEach
    public void intialize() {
        builder = FusekiServer.create();
        builder.port(PORTNUMBER_SPARQL);
    }

    @BeforeAll
    public static void startServer() throws Exception {
        try {
            PORTNUMBER_SPARQL = Utils.getFreePortNumber();
        } catch (Exception ex) {
            throw new Exception("Could not find a free port number for SPARQL testing.");
        }
    }

    @ParameterizedTest
    @MethodSource("data")
    public void mappingTest(String testCaseName, boolean expectedException) {
        String resourcePath = "test-cases/" + testCaseName + "-SPARQL/resource.ttl";
        String mappingPath = "./test-cases/" + testCaseName + "-SPARQL/mapping.ttl";
        String outputPath = "test-cases/" + testCaseName + "-SPARQL/output.nq";
        String tempMappingPath = replacePortInMappingFile(mappingPath, "" + PORTNUMBER_SPARQL);

        builder.add("/ds"+(1), RDFDataMgr.loadDataset(resourcePath));
        this.server = builder.build();
        this.server.start();

        // mapping
        if (!expectedException) {
            doMapping(tempMappingPath, outputPath);
        } else {
            doMappingExpectError(tempMappingPath);
        }

        deleteTempMappingFile(tempMappingPath);
    }

    @AfterEach
    public void stopServer() {
        if (server != null) {
            server.stop();
        }

        System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.StdErrLog");
        System.setProperty("org.eclipse.jetty.LEVEL", "OFF");
    }

    /*
        Replaces the "PORT" in the given mapping file to an available port and saves this in a new temp file
        Returns the absolute path to the temp mapping file
     */
    private String replacePortInMappingFile(String path, String port) {
        try {
            // Read mapping file
            String mapping = Files.readString(Paths.get(Utils.getFile(path).getAbsolutePath()), StandardCharsets.UTF_8);

            // Replace "PORT" in mapping file by new port
            mapping = mapping.replace("PORT", port);

            // Write to temp mapping file
            File tempFile = File.createTempFile("MapperSPARQLTest" + port, ".ttl");
            tempFile.deleteOnExit();
            Files.writeString(tempFile.toPath(), mapping, StandardCharsets.UTF_8);
            return tempFile.getCanonicalPath();
        } catch (IOException ex) {
            throw new Error(ex.getMessage());
        }
    }
}

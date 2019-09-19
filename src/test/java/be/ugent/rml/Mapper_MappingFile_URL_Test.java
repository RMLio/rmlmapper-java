package be.ugent.rml;

import be.ugent.rml.cli.Main;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class Mapper_MappingFile_URL_Test extends TestCore {
    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    private void mappingHandlerTest(String extension, HttpHandler mappingHandler) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/mappingFile", mappingHandler);
        server.createContext("/inputFile", new ValidInputFileHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        Main.main(String.format("-m http://localhost:8080/mappingFile.%s -o ./generated_output.nq", extension).split(" "));
        compareFiles(
                "./generated_output.nq",
                "MAPPINGFILE_URL_TEST_valid/target_output.nq",
                false
        );

        server.stop(0);

        File outputFile = Utils.getFile("./generated_output.nq");
        assertTrue(outputFile.delete());
    }

    @Test
    public void testValidTurtle() throws Exception {
        mappingHandlerTest("ttl", new TurtleFileHandler());
    }

    @Test
    public void testValidJSON() throws Exception {
        mappingHandlerTest("json", new JSONLDFileHandler());
    }

    @Test
    public void testValidN3() throws Exception {
        mappingHandlerTest("n3", new N3FileHandler());
    }

    @Test
    public void testValidNT() throws Exception {
        mappingHandlerTest("nt", new NTFileHandler());
    }

    @Test
    public void testValidXML() throws Exception {
        mappingHandlerTest("xml", new XMLFileHandler());
    }

    @Test(expected = FileNotFoundException.class)
    public void testInvalidMappingURL() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
        server.createContext("/mappingFile", new InvalidMappingFileHandler());
        server.createContext("/inputFile", new ValidInputFileHandler());
        server.setExecutor(null); // creates a default executor
        server.start();

        exit.expectSystemExitWithStatus(1); // Handle System.exit(1)
        Main.main("-m http://localhost:8081/mappingFile -o MAPPINGFILE_URL_TEST_valid/generated_output_invalid.nq".split(" "));
        server.stop(0);
        Utils.getFile("MAPPINGFILE_URL_TEST_valid/generated_output_invalid.nq");
    }

    static abstract class ValidMappingFileHandler implements HttpHandler {

        String extension;
        String contentType;

        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "couldn't load mapping file";
            try {
                response = Utils.fileToString(Utils.getFile(String.format("MAPPINGFILE_URL_TEST_valid/mapping.%s", extension)));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            List<String> contentType = new ArrayList<>();
            contentType.add(this.contentType);
            t.getResponseHeaders().put("Content-Type", contentType);
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class TurtleFileHandler extends ValidMappingFileHandler {
        TurtleFileHandler() {
            extension = "ttl";
            contentType = "text/turtle";
        }
    }

    static class JSONLDFileHandler extends ValidMappingFileHandler {
        JSONLDFileHandler() {
            extension = "json";
            contentType = "application/ld+json";
        }
    }

    static class N3FileHandler extends ValidMappingFileHandler {
        N3FileHandler() {
            extension = "n3";
            contentType = "text/n3;charset=utf-8";
        }
    }

    static class NTFileHandler extends ValidMappingFileHandler {
        NTFileHandler() {
            extension = "nt";
            contentType = "application/n-triples";
        }
    }

    static class XMLFileHandler extends ValidMappingFileHandler {
        XMLFileHandler() {
            extension = "xml";
            contentType = "application/rdf+xml";
        }
    }

    static class ValidInputFileHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "couldn't load input file";
            try {
                response = Utils.fileToString(Utils.getFile("MAPPINGFILE_URL_TEST_valid/student.json"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            List<String> contentType = new ArrayList<>();
            contentType.add("application/json");
            t.getResponseHeaders().put("Content-Type", contentType);
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class InvalidMappingFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "kqdsfmqsdfklnmqdsfnklmfqdsnklmqdsfnkmlqefnkmq";
            List<String> contentType = new ArrayList<>();
            contentType.add("text/turtle");
            t.getResponseHeaders().put("Content-Type", contentType);
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}

package be.ugent.rml;

import be.ugent.rml.cli.Main;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MapperMappingFileURLTest extends TestCore {

    private void mappingHandlerTest(RDFFormat format) throws Exception {
        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
            ValidMappingFileHandler mappingHandler = new ValidMappingFileHandler();
            mappingHandler.format = format;
            server.createContext("/mappingFile", mappingHandler);
            server.createContext("/inputFile", new ValidInputFileHandler());
            server.setExecutor(null); // creates a default executor
            server.start();
            Main.run(String.format("-m http://localhost:8080/mappingFile.%s -o ./generated_output.nq", format.getDefaultFileExtension()).split(" "));
            compareFiles(
                    "./generated_output.nq",
                    "MAPPINGFILE_URL_TEST_valid/target_output.nq",
                    false
            );



            File outputFile = Utils.getFile("./generated_output.nq");
            assertTrue(outputFile.delete());
        } finally {
            if (server != null) {
                server.stop(0);
            }
        }
    }

    @Test
    public void testValidTurtle() throws Exception {
        mappingHandlerTest(RDFFormat.TURTLE);
    }

    @Test
    public void testValidJSON() throws Exception {
        mappingHandlerTest(RDFFormat.JSONLD);
    }

    @Test
    public void testValidN3() throws Exception {
        mappingHandlerTest(RDFFormat.N3);
    }

    @Test
    public void testValidNT() throws Exception {
        mappingHandlerTest(RDFFormat.NTRIPLES);
    }

    @Test
    public void testValidXML() throws Exception {
        mappingHandlerTest(RDFFormat.RDFXML);
    }

    //@Test(expected = FileNotFoundException.class)
    @Test
    public void testInvalidMappingURL() /*throws Exception*/ {
        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(8081), 0);
            server.createContext("/mappingFile", new InvalidMappingFileHandler());
            server.createContext("/inputFile", new ValidInputFileHandler());
            server.setExecutor(null); // creates a default executor
            server.start();
            assertThrows(IllegalArgumentException.class, () -> Main.run("-m http://localhost:8081/mappingFile -o MAPPINGFILE_URL_TEST_valid/generated_output_invalid.nq".split(" ")));
            //Utils.getFile("MAPPINGFILE_URL_TEST_valid/generated_output_invalid.nq");
        } catch (Throwable e) {
            logger.debug("Test throwed an exception.", e);
        } finally {
            if (server != null) {
                server.stop(0);
            }
        }
    }

    static class ValidMappingFileHandler implements HttpHandler {

        RDFFormat format;

        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "couldn't load mapping file";
            try {
                response = Utils.fileToString(Utils.getFile(String.format("MAPPINGFILE_URL_TEST_valid/mapping.%s", format.getDefaultFileExtension())));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            List<String> contentType = new ArrayList<>();
            contentType.add(format.getDefaultMIMEType());
            t.getResponseHeaders().put("Content-Type", contentType);
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
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

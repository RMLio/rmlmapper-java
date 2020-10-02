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

    @Test
    public void testValid() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/mappingFile", new ValidMappingFileHandler());
        server.createContext("/inputFile", new ValidInputFileHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        
        Main.main("-m http://localhost:8080/mappingFile -o ./generated_output.nq".split(" "));
        compareFiles(
                "./generated_output.nq",
                "MAPPINGFILE_URL_TEST_valid/target_output.nq",
                false
        );

        server.stop(0);

        File outputFile = Utils.getFile("./generated_output.nq");
        assertTrue(outputFile.delete());
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

    static class ValidMappingFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "couldn't load mapping file";
            try {
                response = Utils.fileToString(Utils.getFile("MAPPINGFILE_URL_TEST_valid/mapping.ttl"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            List<String> contentType = new ArrayList<>();
            contentType.add("text/turtle");
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

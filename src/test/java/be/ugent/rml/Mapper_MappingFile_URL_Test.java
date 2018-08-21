package be.ugent.rml;

import be.ugent.rml.cli.Main;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class Mapper_MappingFile_URL_Test extends TestCore {

    @Test
    public void testValid() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/mappingFile", new ValidMappingFileHandler());
        server.createContext("/inputFile", new ValidInputFileHandler());
        server.setExecutor(null); // creates a default executor
        server.start();

        Main.main("-m http://localhost:8000/mappingFile -o src/test/resources/test-cases/MAPPINGFILE_URL_TEST_valid/generated_output.nq".split(" "));
        compareFiles("test-cases/MAPPINGFILE_URL_TEST_valid/generated_output.nq", "test-cases/MAPPINGFILE_URL_TEST_valid/target_output.nq");

        server.stop(0);
    }

    @Test(expected = Error.class)
    public void testInvalidMappingURL() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/mappingFile", new InvalidMappingFileHandler());
        server.createContext("/inputFile", new ValidInputFileHandler());
        server.setExecutor(null); // creates a default executor
        server.start();

        Main.main("-m http://localhost:8000/mappingFile -o src/test/resources/test-cases/MAPPINGFILE_URL_TEST_valid/generated_output.nq".split(" "));
        compareFiles("test-cases/MAPPINGFILE_URL_TEST_valid/generated_output.nq", "test-cases/MAPPINGFILE_URL_TEST_valid/target_output.nq");

        server.stop(0);
    }

    static class ValidMappingFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "couldn't load mapping file";
            try {
                ClassLoader classLoader = getClass().getClassLoader();
                response = Utils.fileToString(new File(classLoader.getResource("test-cases/MAPPINGFILE_URL_TEST_valid/mapping.ttl").getFile()));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            List<String> contentType = new ArrayList<>();
            contentType.add( "application/rdf+xml");
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
                response = Utils.fileToString(new File("/Users/driesmarzougui/Documents/work/IDLab/RMLProcessor/rmlmapper-java/src/test/resources/test-cases/MAPPINGFILE_URL_TEST_valid/student.json"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            List<String> contentType = new ArrayList<>();
            contentType.add( "application/json");
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
            contentType.add( "application/rdf+xml");
            t.getResponseHeaders().put("Content-Type", contentType);
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}

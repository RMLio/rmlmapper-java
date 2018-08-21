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

public class Mapper_MappingFile_URL_Test {

    @Test
    public void test() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/mappingFile", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();

        Main.main("-m http://localhost:8000/mappingFile -o ./test-cases/example_mappingFile_url/generated_output.nq".split(" "));


        server.stop(0);
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "couldn't load mapping file";
            try {
                response = Utils.fileToString(new File("/Users/driesmarzougui/Documents/work/IDLab/RMLProcessor/rmlmapper-java/src/test/resources/test-cases/example_mappingFile_url/mapping.ttl"));
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
}

package be.ugent.rml;

import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Mapper_WoT_Test extends TestCore {
    @Test
    public void evaluate_essence_wot_support() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/trashcans", new Mapper_WoT_Test.TrashCansFileHandler());
        server.setExecutor(null); // creates a default executor
        server.start();

        HashMap<Term, String> outPaths = new HashMap<Term, String>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump"), "./web-of-things/essence/out-local-file.nt");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./web-of-things/essence/out-default.nq");
        doMapping("./web-of-things/essence/mapping.ttl", outPaths, "./web-of-things/essence/private-security-data.ttl");

        server.stop(0);
    }

    static class TrashCansFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "couldn't load trashcan JSON file";
            try {
                response = Utils.fileToString(Utils.getFile("./web-of-things/essence/iot-sensors.json"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            List<String> contentType = new ArrayList<>();
            contentType.add("application/json");
            List<String> key = t.getRequestHeaders().get("apikey");

            // Check API key
            try {
                if (key.get(0).equals("123456789")) {
                    t.getResponseHeaders().put("Content-Type", contentType);
                    t.sendResponseHeaders(200, response.length());
                    OutputStream os = t.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
                // Wrong API key
                else {
                    t.sendResponseHeaders(401, response.length());
                }
            }
            // No API key provided
            catch (IndexOutOfBoundsException e) {
                t.sendResponseHeaders(401, response.length());
            }

        }
    }
}
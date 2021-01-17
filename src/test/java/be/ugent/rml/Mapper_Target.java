package be.ugent.rml;

import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import com.sun.net.httpserver.HttpServer;
import org.apache.jena.fuseki.embedded.FusekiServer;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.junit.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;

public class Mapper_Target extends TestCore {
    private static int PORTNUMBER_SPARQL;
    private FusekiServer.Builder builder;
    private FusekiServer server;

    @Test
    public void evaluate_sparql_target() throws Exception {
        // Create Web API
        HttpServer webApi = HttpServer.create(new InetSocketAddress(8000), 0);
        webApi.createContext("/trashcans", new Mapper_WoT_Test.TrashCansFileHandler());
        webApi.setExecutor(null); // creates a default executor
        webApi.start();

        // Replace PORT number in mapping file
        String tempMappingPath = replacePortInMappingFile("./web-of-things/logical-target/sparql/mapping.ttl", "" + PORTNUMBER_SPARQL);
        HashMap<Term, String> outPaths = new HashMap<Term, String>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetSPARQL"), "./web-of-things/logical-target/sparql/out-sparql.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./web-of-things/logical-target/sparql/out-default.nq");
        doMapping(tempMappingPath, outPaths, "./web-of-things/logical-target/private-security-data.ttl");

        webApi.stop(0);
    }

    @Test
    public void evaluate_local_file_target() throws Exception {
        // Create Web API
        HttpServer webApi = HttpServer.create(new InetSocketAddress(8000), 0);
        webApi.createContext("/trashcans", new Mapper_WoT_Test.TrashCansFileHandler());
        webApi.setExecutor(null); // creates a default executor
        webApi.start();

        HashMap<Term, String> outPaths = new HashMap<Term, String>();
        outPaths.put(new NamedNode("http://example.com/rules/#TargetDump"), "./web-of-things/logical-target/local-file/out-local-file.nq");
        outPaths.put(new NamedNode("rmlmapper://default.store"), "./web-of-things/logical-target/local-file/out-default.nq");
        doMapping("./web-of-things/logical-target/local-file/mapping.ttl", outPaths, "./web-of-things/logical-target/private-security-data.ttl");

        webApi.stop(0);
    }

    @Before
    public void intialize() throws IOException {
        // Create Fuseki SPARQL endpoint /ds1
        builder = FusekiServer.create();
        builder.setPort(PORTNUMBER_SPARQL);
        builder.add("/ds1", DatasetGraphFactory.createTxnMem(), true);
        this.server = builder.build();
        this.server.start();
    }

    @BeforeClass
    public static void startServer() throws Exception {
        try {
            PORTNUMBER_SPARQL = Utils.getFreePortNumber();
        } catch (Exception ex) {
            throw new Exception("Could not find a free port number for SPARQL testing.");
        }
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

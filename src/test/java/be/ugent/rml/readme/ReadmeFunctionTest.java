package be.ugent.rml.readme;

import be.ugent.idlab.knows.functions.agent.Agent;
import be.ugent.idlab.knows.functions.agent.AgentFactory;
import be.ugent.rml.Executor;
import be.ugent.rml.Utils;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.QuadStoreFactory;
import be.ugent.rml.store.RDF4JStore;
import be.ugent.rml.term.NamedNode;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.fail;


public class ReadmeFunctionTest {

    @Test
    public void function() {
        try {
            String mapPath = Utils.getFile("argument/mapping.ttl").getAbsolutePath(); //path to the mapping file that needs to be executed
            File mappingFile = new File(mapPath);

            // Use custom functions.ttl file
            String functionPath = "rml-fno-test-cases/functions_test.ttl";

            // Get the mapping string stream
            InputStream mappingStream = new FileInputStream(mappingFile);

            // Load the mapping in a QuadStore
            QuadStore rmlStore = QuadStoreFactory.read(mappingStream);

            // Set up the basepath for the records factory, i.e., the basepath for the (local file) data sources
            RecordsFactory factory = new RecordsFactory(mappingFile.getParent(), mappingFile.getParent());

            // Set up the functions used during the mapping
            Agent functionAgent = AgentFactory.createFromFnO(functionPath);

            // Set up the outputstore (needed when you want to output something else than nquads
            QuadStore outputStore = new RDF4JStore();

            // Create the Executor
            Executor executor = new Executor(rmlStore, factory, outputStore, Utils.getBaseDirectiveTurtleOrDefault(mappingStream, "http://example.com"), functionAgent);

            // Execute the mapping
            QuadStore result = executor.execute(null).get(new NamedNode("rmlmapper://default.store"));

            // Output the result
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out));
            result.write(out, "turtle");
        } catch (Exception e) {
            fail("No exception was expected.");
        }
    }
}

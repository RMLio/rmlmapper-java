package be.ugent.rml;

import be.ugent.idlab.knows.functions.agent.Agent;
import be.ugent.idlab.knows.functions.agent.AgentFactory;
import be.ugent.knows.idlabFunctions.IDLabFunctions;
import be.ugent.rml.store.Quad;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.QuadStoreFactory;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.NoSuchFileException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapperLDESTest extends TestCore {
    private static Agent functionAgent;

    @AfterEach
    public void cleanUp() throws IOException {
        IDLabFunctions.resetState();
        FileUtils.deleteDirectory(new File("/tmp/ldes-test"));
        try {
            FileUtils.delete(new File("/tmp/bluebike_create_state"));
        } catch (NoSuchFileException e) {}

        try {
            FileUtils.delete(new File("/tmp/bluebike_update_state"));
        } catch (NoSuchFileException e) {}

        try {
            FileUtils.delete(new File("/tmp/bluebike_delete_state"));
        } catch (NoSuchFileException e) {}
    }

    @BeforeAll
    public static void setups() throws Exception {
        functionAgent = AgentFactory.createFromFnO("fno/functions_idlab.ttl", "fno/functions_idlab_test_classes_java_mapping.ttl");
    }

    @Test
    public void evaluate_unique_LDES () throws Exception {
        Executor executor = this.createExecutor("./web-of-things/ldes/generation/basic/mapping.ttl", functionAgent);
        doMapping(executor, "./web-of-things/ldes/generation/basic/output.nq");
    }

    @Test
    public void evaluate_repeat_LDES() throws Exception {
        Executor executor = this.createExecutor("./web-of-things/ldes/generation/repeat/mapping.ttl", functionAgent);
        executor.execute(null).get(new NamedNode("rmlmapper://default.store"));
        IDLabFunctions.saveState();
        executor = this.createExecutor("./web-of-things/ldes/generation/repeat/mapping.ttl", functionAgent);
        doMapping(executor, "./web-of-things/ldes/generation/repeat/output.nq");
    }

    @Test
    public void evaluate_partial_repeat_LDES() throws Exception {
        Executor executor = this.createExecutor("./web-of-things/ldes/generation/partial/mapping.ttl", functionAgent);
        QuadStore result = executor.execute(null).get(new NamedNode("rmlmapper://default.store"));
        IDLabFunctions.saveState();
        executor = this.createExecutor("./web-of-things/ldes/generation/partial/mapping2.ttl", functionAgent);
        QuadStore result_second = executor.execute(null).get(new NamedNode("rmlmapper://default.store"));
        assertEquals(3, result.size());
        assertEquals(1, result_second.size());

    }

    @Test
    public void bluebikeAdditionUpdateDelete() throws Exception {
        Executor executor = this.createExecutor("./rml-ldes/bluebike/bluebike-change-base.rml.ttl", functionAgent);
        executor.execute(null);
        IDLabFunctions.saveState();
        executor = this.createExecutor("./rml-ldes/bluebike/bluebike-change-change.rml.ttl", functionAgent);
        QuadStore target = executor.execute(null).get(new NamedNode("http://example.com/rules/#LDESLogicalTarget"));
        String expectedString = target.toSortedString();
        File resultFile = Utils.getFile("./rml-ldes/bluebike/out-bluebike-change-stations-change.nq");
        String resultString = QuadStoreFactory.read(resultFile, RDFFormat.NQUADS).toSortedString();

        assertEquals(expectedString, resultString);
    }
}

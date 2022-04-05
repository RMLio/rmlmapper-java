package be.ugent.rml;

import be.ugent.idlab.knows.functions.agent.Agent;
import be.ugent.idlab.knows.functions.agent.AgentFactory;
import be.ugent.rml.functions.lib.IDLabFunctions;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.RDF4JStore;
import be.ugent.rml.term.NamedNode;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class Mapper_LDES_Test extends TestCore {
    private static Agent functionAgent;

    @After
    public void cleanUp() throws IOException {
        IDLabFunctions.resetState();
        FileUtils.deleteDirectory(new File("/tmp/ldes-test"));
    }

    @BeforeClass
    public static void setups() throws Exception {
        QuadStore functionDescriptionTriples = new RDF4JStore();
        functionAgent = AgentFactory.createFromFnO("functions_idlab.ttl", "functions_idlab_classes_java_mapping_tests.ttl");
    }

    @Test
    public void evaluate_unique_LDES () throws Exception {
        Executor executor = this.createExecutor("./web-of-things/ldes/generation/basic/mapping.ttl", functionAgent);
        doMapping(executor, "./web-of-things/ldes/generation/basic/output.nq");
    }

    @Test
    public void evaluate_repeat_LDES() throws Exception {
        Executor executor = this.createExecutor("./web-of-things/ldes/generation/repeat/mapping.ttl", functionAgent);
        executor.executeV5(null).get(new NamedNode("rmlmapper://default.store"));
        IDLabFunctions.saveState();
        executor = this.createExecutor("./web-of-things/ldes/generation/repeat/mapping.ttl", functionAgent);
        doMapping(executor, "./web-of-things/ldes/generation/repeat/output.nq");
    }

    @Test
    public void evaluate_partial_repeat_LDES() throws Exception {
        Executor executor = this.createExecutor("./web-of-things/ldes/generation/partial/mapping.ttl", functionAgent);
        QuadStore result = executor.executeV5(null).get(new NamedNode("rmlmapper://default.store"));
        IDLabFunctions.saveState();
        executor = this.createExecutor("./web-of-things/ldes/generation/partial/mapping2.ttl", functionAgent);
        QuadStore result_second = executor.executeV5(null).get(new NamedNode("rmlmapper://default.store"));
        assertEquals(3, result.size());
        assertEquals(1, result_second.size());

    }

    
}

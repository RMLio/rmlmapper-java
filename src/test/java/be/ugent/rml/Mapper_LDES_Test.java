package be.ugent.rml;

import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.functions.lib.IDLabFunctions;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.RDF4JStore;
import be.ugent.rml.term.NamedNode;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Mapper_LDES_Test extends TestCore {

    private static FunctionLoader LOADER;
    @After
    public void cleanUp() throws IOException {
        IDLabFunctions.resetState();
        FileUtils.deleteDirectory(new File("/tmp/ldes-test"));
    }

    @BeforeClass
    public static void setups() throws Exception {
        QuadStore functionDescriptionTriples = new RDF4JStore();
        functionDescriptionTriples.read(Utils.getInputStreamFromFile(new File("./src/main/resources/functions_idlab.ttl")), null, RDFFormat.TURTLE);
        LOADER = new FunctionLoader(functionDescriptionTriples);

    }

    @Test
    public void evaluate_unique_LDES () throws Exception {
        Executor executor = this.createExecutor("./web-of-things/ldes/generation/basic/mapping.ttl", LOADER);
        doMapping(executor, "./web-of-things/ldes/generation/basic/output.nq");
    }

    @Test
    public void evaluate_repeat_LDES() throws Exception {
        Executor executor = this.createExecutor("./web-of-things/ldes/generation/repeat/mapping.ttl", LOADER);
        executor.executeV5(null).get(new NamedNode("rmlmapper://default.store"));
        IDLabFunctions.saveState();
        executor = this.createExecutor("./web-of-things/ldes/generation/repeat/mapping.ttl", LOADER);
        doMapping(executor, "./web-of-things/ldes/generation/repeat/output.nq");
    }

    @Test
    public void evaluate_partial_repeat_LDES() throws Exception {
        Executor executor = this.createExecutor("./web-of-things/ldes/generation/partial/mapping.ttl", LOADER);
        QuadStore result = executor.executeV5(null).get(new NamedNode("rmlmapper://default.store"));
        IDLabFunctions.saveState();
        executor = this.createExecutor("./web-of-things/ldes/generation/partial/mapping2.ttl", LOADER);
        QuadStore result_second = executor.executeV5(null).get(new NamedNode("rmlmapper://default.store"));
        assertEquals(3, result.size());
        assertEquals(1, result_second.size());

    }

    
}

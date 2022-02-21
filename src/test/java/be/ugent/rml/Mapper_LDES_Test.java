package be.ugent.rml;

import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.RDF4JStore;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

public class Mapper_LDES_Test extends TestCore {

    private static FunctionLoader LOADER;

    @BeforeClass
    public static void setups() throws Exception {
        QuadStore functionDescriptionTriples = new RDF4JStore();
        functionDescriptionTriples.read(Utils.getInputStreamFromFile(new File("./src/main/resources/functions_idlab.ttl")), null, RDFFormat.TURTLE);
        LOADER = new FunctionLoader(functionDescriptionTriples);

    }

    @Test
    public void evaluate_basic_LDES () throws Exception {
        Executor executor = this.createExecutor("./web-of-things/ldes/generation/mapping.ttl", LOADER);
        doMapping(executor, "./web-of-things/ldes/generation/output.nq");
    }
}

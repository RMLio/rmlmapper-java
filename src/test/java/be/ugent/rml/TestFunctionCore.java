package be.ugent.rml;

import be.ugent.idlab.knows.functions.agent.Agent;
import be.ugent.idlab.knows.functions.agent.AgentFactory;
import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.functions.lib.IDLabTestFunctions;
import be.ugent.rml.store.RDF4JStore;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.util.HashMap;
import java.util.Map;

abstract class TestFunctionCore extends TestCore {

    Executor doPreloadMapping(String mapPath, String outPath) throws Exception {
        Map<String, Class> libraryMap = new HashMap<>();
        libraryMap.put("IDLabFunctions", IDLabTestFunctions.class);
        libraryMap.put("io.fno.grel.ArrayFunctions", io.fno.grel.ArrayFunctions.class);
        libraryMap.put("io.fno.grel.BooleanFunctions", io.fno.grel.BooleanFunctions.class);
        libraryMap.put("io.fno.grel.ControlsFunctions", io.fno.grel.ControlsFunctions.class);
        libraryMap.put("io.fno.grel.StringFunctions", io.fno.grel.StringFunctions.class);

        // Read function description files.
        RDF4JStore functionDescriptionTriples = new RDF4JStore();
        functionDescriptionTriples.read(Utils.getInputStreamFromFile(Utils.getFile("functions_idlab.ttl")), null, RDFFormat.TURTLE);
        functionDescriptionTriples.read(Utils.getInputStreamFromFile(Utils.getFile("rml-fno-test-cases/functions_test.ttl")), null, RDFFormat.TURTLE);
        functionDescriptionTriples.read(Utils.getInputStreamFromFile(Utils.getFile("functions_grel.ttl")), null, RDFFormat.TURTLE);
        functionDescriptionTriples.read(Utils.getInputStreamFromFile(Utils.getFile("grel_java_mapping.ttl")), null, RDFFormat.TURTLE);
//            File myFile = Utils.getFile("rml-fno-test-cases/functions_test.ttl");
        FunctionLoader functionLoader = new FunctionLoader(functionDescriptionTriples, libraryMap);

        Agent functionAgent = AgentFactory.createFromFnO(
                "functions_idlab.ttl",
                "rml-fno-test-cases/functions_test.ttl",
                "grel_java_mapping.ttl",
                "https://users.ugent.be/~bjdmeest/function/grel.ttl"
                );

        Executor executor = this.createExecutor(mapPath, functionLoader, functionAgent);
        doMapping(executor, outPath);
        return executor;
    }
}

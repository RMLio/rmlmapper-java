package be.ugent.rml;

import be.ugent.idlab.knows.functions.agent.Agent;
import be.ugent.idlab.knows.functions.agent.AgentFactory;

abstract class TestFunctionCore extends TestCore {

    Executor doPreloadMapping(String mapPath, String outPath) throws Exception {
        Agent functionAgent = AgentFactory.createFromFnO(
                "functions_idlab.ttl",
                "functions_idlab_classes_java_mapping_tests.ttl",
                "rml-fno-test-cases/functions_test.ttl",
                "grel_java_mapping.ttl",
                "https://users.ugent.be/~bjdmeest/function/grel.ttl"
                );

        Executor executor = this.createExecutor(mapPath, functionAgent);
        doMapping(executor, outPath);
        return executor;
    }
}

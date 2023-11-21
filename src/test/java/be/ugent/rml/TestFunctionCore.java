package be.ugent.rml;

import be.ugent.idlab.knows.functions.agent.Agent;
import be.ugent.idlab.knows.functions.agent.AgentFactory;

abstract class TestFunctionCore extends TestCore {

    Executor doPreloadMapping(String mapPath, String outPath) throws Exception {
        Agent functionAgent = AgentFactory.createFromFnO(
                "fno/functions_idlab.ttl",
                "fno/functions_idlab_test_classes_java_mapping.ttl",
                "fno_idlab_old/functions_idlab.ttl", "fno_idlab_old/functions_idlab_classes_java_mapping.ttl",
                "rml-fno-test-cases/functions_test.ttl",
                "grel_java_mapping.ttl",
                "functions_grel.ttl"
                );

        Executor executor = this.createExecutor(mapPath, functionAgent);
        doMapping(executor, outPath);
        return executor;
    }
}

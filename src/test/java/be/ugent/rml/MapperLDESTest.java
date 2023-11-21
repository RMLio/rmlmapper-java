package be.ugent.rml;

import be.ugent.idlab.knows.functions.agent.Agent;
import be.ugent.idlab.knows.functions.agent.AgentFactory;
import be.ugent.knows.idlabFunctions.IDLabFunctions;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.QuadStoreFactory;
import be.ugent.rml.target.Target;
import be.ugent.rml.target.TargetFactory;
import be.ugent.rml.term.NamedNode;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapperLDESTest extends TestCore {
    private static Agent functionAgent;
    private static NamedNode LDES_LOGICAL_TARGET_IRI = new NamedNode("http://example.com/rules/#LDESLogicalTarget");
    private static TargetFactory targetFactory = new TargetFactory(System.getProperty("user.dir"));

    @AfterEach
    public void cleanUp() throws IOException {
        IDLabFunctions.resetState();
        FileUtils.deleteDirectory(new File("/tmp/ldes-test"));
        try {
            FileUtils.delete(new File("/tmp/create_state"));
        } catch (NoSuchFileException e) {}

        try {
            FileUtils.delete(new File("/tmp/update_state"));
        } catch (NoSuchFileException e) {}

        try {
            FileUtils.delete(new File("/tmp/delete_state"));
        } catch (NoSuchFileException e) {}
    }

    /**
     * Execute a change-based LDES mapping and asserts if the output is as expected.
     *
     * @param baseMappingPath
     * @param baseOutputPath
     * @param changeMappingPath
     * @param changeOutputPath
     * @param LDESLogicalTarget
     * @throws Exception
     */
    private void executeLDESMapping(String baseMappingPath, String baseOutputPath, String changeMappingPath,
                                    String changeOutputPath, NamedNode LDESLogicalTarget) throws Exception {
        QuadStore outputStore;
        String expectedString;
        String resultString;
        File expectedFile;
        Executor executor;
        Target target;

        /* Base */
        executor = this.createExecutor(baseMappingPath, functionAgent);
        outputStore = executor.execute(null).get(LDESLogicalTarget);
        System.out.println(outputStore.size());
        target = targetFactory.getTarget(LDESLogicalTarget, executor.getRMLStore(), outputStore);
        outputStore.addQuads(target.getMetadata());
        resultString = outputStore.toSortedString();
        expectedFile = Utils.getFile(baseOutputPath);
        expectedString = QuadStoreFactory.read(expectedFile, RDFFormat.NQUADS).toSortedString();
        assertEquals(expectedString, resultString);
        IDLabFunctions.saveState();

        /* Change */
        executor = this.createExecutor(changeMappingPath, functionAgent);
        outputStore = executor.execute(null).get(LDESLogicalTarget);
        target = targetFactory.getTarget(LDESLogicalTarget, executor.getRMLStore(), outputStore);
        outputStore.addQuads(target.getMetadata());
        resultString = outputStore.toSortedString();
        expectedFile = Utils.getFile(changeOutputPath);
        expectedString = QuadStoreFactory.read(expectedFile, RDFFormat.NQUADS).toSortedString();
        assertEquals(expectedString, resultString);
    }

    @BeforeAll
    public static void setups() throws Exception {
        functionAgent = AgentFactory.createFromFnO(
                "fno/functions_idlab.ttl", "fno/functions_idlab_test_classes_java_mapping.ttl",
                "fno_idlab_old/functions_idlab.ttl", "fno_idlab_old/functions_idlab_classes_java_mapping.ttl");
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
        this.executeLDESMapping("./rml-ldes/bluebike/base.rml.ttl",
                "./rml-ldes/bluebike/output-base.nq",
                "./rml-ldes/bluebike/change.rml.ttl",
                "./rml-ldes/bluebike/output-change.nq",
                LDES_LOGICAL_TARGET_IRI);
    }

    @Test
    public void RMLLDES0001a() throws Exception {
        this.executeLDESMapping("./rml-ldes/RMLLDES0001a/base.rml.ttl",
                "./rml-ldes/RMLLDES0001a/output-base.nq",
                "./rml-ldes/RMLLDES0001a/change.rml.ttl",
                "./rml-ldes/RMLLDES0001a/output-change.nq",
                LDES_LOGICAL_TARGET_IRI);
    }

    @Test
    public void RMLLDES0001b() throws Exception {
        this.executeLDESMapping("./rml-ldes/RMLLDES0001b/base.rml.ttl",
                "./rml-ldes/RMLLDES0001b/output-base.nq",
                "./rml-ldes/RMLLDES0001b/change.rml.ttl",
                "./rml-ldes/RMLLDES0001b/output-change.nq",
                LDES_LOGICAL_TARGET_IRI);
    }

    @Test
    public void RMLLDES0001c() throws Exception {
        this.executeLDESMapping("./rml-ldes/RMLLDES0001c/base.rml.ttl",
                "./rml-ldes/RMLLDES0001c/output-base.nq",
                "./rml-ldes/RMLLDES0001c/change.rml.ttl",
                "./rml-ldes/RMLLDES0001c/output-change.nq",
                LDES_LOGICAL_TARGET_IRI);
    }

    @Test
    public void RMLLDES0001d() throws Exception {
        this.executeLDESMapping("./rml-ldes/RMLLDES0001d/base.rml.ttl",
                "./rml-ldes/RMLLDES0001d/output-base.nq",
                "./rml-ldes/RMLLDES0001d/change.rml.ttl",
                "./rml-ldes/RMLLDES0001d/output-change.nq",
                LDES_LOGICAL_TARGET_IRI);
    }

    @Test
    public void RMLLDES0001e() throws Exception {
        this.executeLDESMapping("./rml-ldes/RMLLDES0001e/base.rml.ttl",
                "./rml-ldes/RMLLDES0001e/output-base.nq",
                "./rml-ldes/RMLLDES0001e/change.rml.ttl",
                "./rml-ldes/RMLLDES0001e/output-change.nq",
                LDES_LOGICAL_TARGET_IRI);
    }

    @Test
    public void RMLLDES0001f() throws Exception {
        this.executeLDESMapping("./rml-ldes/RMLLDES0001f/base.rml.ttl",
                "./rml-ldes/RMLLDES0001f/output-base.nq",
                "./rml-ldes/RMLLDES0001f/change.rml.ttl",
                "./rml-ldes/RMLLDES0001f/output-change.nq",
                LDES_LOGICAL_TARGET_IRI);
    }

    @Test
    public void RMLLDES0001g() throws Exception {
        this.executeLDESMapping("./rml-ldes/RMLLDES0001g/base.rml.ttl",
                "./rml-ldes/RMLLDES0001g/output-base.nq",
                "./rml-ldes/RMLLDES0001g/change.rml.ttl",
                "./rml-ldes/RMLLDES0001g/output-change.nq",
                LDES_LOGICAL_TARGET_IRI);
    }

    @Test
    public void RMLLDES0001h() throws Exception {
        this.executeLDESMapping("./rml-ldes/RMLLDES0001h/base.rml.ttl",
                "./rml-ldes/RMLLDES0001h/output-base.nq",
                "./rml-ldes/RMLLDES0001h/change.rml.ttl",
                "./rml-ldes/RMLLDES0001h/output-change.nq",
                LDES_LOGICAL_TARGET_IRI);
    }

    @Test
    public void RMLLDES0002a() throws Exception {
        this.executeLDESMapping("./rml-ldes/RMLLDES0002a/base.rml.ttl",
                "./rml-ldes/RMLLDES0002a/output-base.nq",
                "./rml-ldes/RMLLDES0002a/change.rml.ttl",
                "./rml-ldes/RMLLDES0002a/output-change.nq",
                LDES_LOGICAL_TARGET_IRI);
    }

    @Test
    public void RMLLDES0002b() throws Exception {
        this.executeLDESMapping("./rml-ldes/RMLLDES0002b/base.rml.ttl",
                "./rml-ldes/RMLLDES0002b/output-base.nq",
                "./rml-ldes/RMLLDES0002b/change.rml.ttl",
                "./rml-ldes/RMLLDES0002b/output-change.nq",
                LDES_LOGICAL_TARGET_IRI);
    }

    @Test
    public void RMLLDES0002c() throws Exception {
        this.executeLDESMapping("./rml-ldes/RMLLDES0002c/base.rml.ttl",
                "./rml-ldes/RMLLDES0002c/output-base.nq",
                "./rml-ldes/RMLLDES0002c/change.rml.ttl",
                "./rml-ldes/RMLLDES0002c/output-change.nq",
                LDES_LOGICAL_TARGET_IRI);
    }

    @Test
    public void RMLLDES0002d() throws Exception {
        this.executeLDESMapping("./rml-ldes/RMLLDES0002d/base.rml.ttl",
                "./rml-ldes/RMLLDES0002d/output-base.nq",
                "./rml-ldes/RMLLDES0002d/change.rml.ttl",
                "./rml-ldes/RMLLDES0002d/output-change.nq",
                LDES_LOGICAL_TARGET_IRI);
    }

    public void RMLLDES0002e() throws Exception {
        this.executeLDESMapping("./rml-ldes/RMLLDES0002e/base.rml.ttl",
                "./rml-ldes/RMLLDES0002e/output-base.nq",
                "./rml-ldes/RMLLDES0002e/change.rml.ttl",
                "./rml-ldes/RMLLDES0002e/output-change.nq",
                LDES_LOGICAL_TARGET_IRI);
    }

    @Test
    public void RMLLDES0002f() throws Exception {
        this.executeLDESMapping("./rml-ldes/RMLLDES0002f/base.rml.ttl",
                "./rml-ldes/RMLLDES0002f/output-base.nq",
                "./rml-ldes/RMLLDES0002f/change.rml.ttl",
                "./rml-ldes/RMLLDES0002f/output-change.nq",
                LDES_LOGICAL_TARGET_IRI);
    }

    @Test
    public void RMLLDES0002g() throws Exception {
        this.executeLDESMapping("./rml-ldes/RMLLDES0002g/base.rml.ttl",
                "./rml-ldes/RMLLDES0002g/output-base.nq",
                "./rml-ldes/RMLLDES0002g/change.rml.ttl",
                "./rml-ldes/RMLLDES0002g/output-change.nq",
                LDES_LOGICAL_TARGET_IRI);
    }

    @Test
    public void RMLLDES0003a() throws Exception {
        this.executeLDESMapping("./rml-ldes/RMLLDES0003a/base.rml.ttl",
                "./rml-ldes/RMLLDES0003a/output-base.nq",
                "./rml-ldes/RMLLDES0003a/change.rml.ttl",
                "./rml-ldes/RMLLDES0003a/output-change.nq",
                LDES_LOGICAL_TARGET_IRI);
    }

    @Test
    public void RMLLDES0003b() throws Exception {
        this.executeLDESMapping("./rml-ldes/RMLLDES0003b/base.rml.ttl",
                "./rml-ldes/RMLLDES0003b/output-base.nq",
                "./rml-ldes/RMLLDES0003b/change.rml.ttl",
                "./rml-ldes/RMLLDES0003b/output-change.nq",
                LDES_LOGICAL_TARGET_IRI);
    }

    @Test
    public void RMLLDES0003c() throws Exception {
        this.executeLDESMapping("./rml-ldes/RMLLDES0003c/base.rml.ttl",
                "./rml-ldes/RMLLDES0003c/output-base.nq",
                "./rml-ldes/RMLLDES0003c/change.rml.ttl",
                "./rml-ldes/RMLLDES0003c/output-change.nq",
                LDES_LOGICAL_TARGET_IRI);
    }

    @Test
    public void RMLLDES0003d() throws Exception {
        this.executeLDESMapping("./rml-ldes/RMLLDES0003d/base.rml.ttl",
                "./rml-ldes/RMLLDES0003d/output-base.nq",
                "./rml-ldes/RMLLDES0003d/change.rml.ttl",
                "./rml-ldes/RMLLDES0003d/output-change.nq",
                LDES_LOGICAL_TARGET_IRI);
    }

    public void RMLLDES0003e() throws Exception {
        this.executeLDESMapping("./rml-ldes/RMLLDES0003e/base.rml.ttl",
                "./rml-ldes/RMLLDES0003e/output-base.nq",
                "./rml-ldes/RMLLDES0003e/change.rml.ttl",
                "./rml-ldes/RMLLDES0003e/output-change.nq",
                LDES_LOGICAL_TARGET_IRI);
    }

    @Test
    public void RMLLDES0003f() throws Exception {
        this.executeLDESMapping("./rml-ldes/RMLLDES0003f/base.rml.ttl",
                "./rml-ldes/RMLLDES0003f/output-base.nq",
                "./rml-ldes/RMLLDES0003f/change.rml.ttl",
                "./rml-ldes/RMLLDES0003f/output-change.nq",
                LDES_LOGICAL_TARGET_IRI);
    }

    @Test
    public void RMLLDES0003g() throws Exception {
        this.executeLDESMapping("./rml-ldes/RMLLDES0003g/base.rml.ttl",
                "./rml-ldes/RMLLDES0003g/output-base.nq",
                "./rml-ldes/RMLLDES0003g/change.rml.ttl",
                "./rml-ldes/RMLLDES0003g/output-change.nq",
                LDES_LOGICAL_TARGET_IRI);
    }
}

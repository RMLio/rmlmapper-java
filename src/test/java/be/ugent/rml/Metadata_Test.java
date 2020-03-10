package be.ugent.rml;

import be.ugent.rml.cli.Main;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * Metadata files are compared as strings, without loading them in a QuadStore first.
 * So when making tests, make sure to use the exact same format as the mapper output.
 */
public class Metadata_Test extends TestCore {

    @Test
    public void datasetLevelTest() throws Exception {
        Main.main("-c ./metadata-test-cases/metadata-dataset-level/config_example.properties".split(" "));
        compareFiles(
                "metadata-test-cases/metadata-dataset-level/target_metadata.nq",
                "./generated_metadata.nq",
                true
        );

        cleanup();
    }

    @Test
    public void tripleLevelTest() throws Exception {
        Main.main("-c ./metadata-test-cases/metadata-triple-level/config_example.properties".split(" "));
        compareFiles(
                "metadata-test-cases/metadata-triple-level/target_metadata.nq",
                "./generated_metadata.nq",
                true
        );

        cleanup();
    }

    @Test
    public void termLevelTest() throws Exception {
        Main.main("-c ./src/test/resources/metadata-test-cases/metadata-term-level/config_example.properties".split(" "));
        compareFiles(
                "metadata-test-cases/metadata-term-level/target_metadata.nq",
                "./generated_metadata.nq",
                true
        );

        cleanup();
    }

    private void cleanup() {
        try {
            File outputFile = Utils.getFile("./generated_output.nq");
            assertTrue(outputFile.delete());
            outputFile = Utils.getFile("./generated_metadata.nq");
            assertTrue(outputFile.delete());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

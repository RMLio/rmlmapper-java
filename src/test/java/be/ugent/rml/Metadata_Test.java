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
    public void datasetLevelTest() {
        Main.main("-c ./metadata/METADATA_TEST_dataset_level/config_example.properties".split(" "));
        compareFiles(
                "./metadata/METADATA_TEST_dataset_level/target_metadata.nq",
                "./generated_metadata.nq",
                true
        );

        cleanup();
    }

    @Test
    public void tripleLevelTest() {
        Main.main("-c ./metadata/METADATA_TEST_triple_level/config_example.properties".split(" "));
        compareFiles(
                "./metadata/METADATA_TEST_triple_level/target_metadata.nq",
                "./generated_metadata.nq",
                true
        );

        cleanup();
    }

    @Test
    public void termLevelTest() {
        Main.main("-c ./src/test/resources/metadata/METADATA_TEST_term_level/config_example.properties".split(" "));
        compareFiles(
                "./metadata/METADATA_TEST_term_level/target_metadata.nq",
                "./generated_metadata.nq",
                true
        );

        cleanup();
    }

    void cleanup() {
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

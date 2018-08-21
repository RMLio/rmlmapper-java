package be.ugent.rml;

import be.ugent.rml.cli.Main;
import org.junit.Test;

/**
 * Metadata files are compared as strings, without loading them in a QuadStore first.
 * So when making tests, make sure to use the exact same format as the mapper output.
 */
public class Metadata_Test extends TestCore {

    @Test
    public void datasetLevelTest() {
        Main.main("-c ./test-cases/METADATA_TEST_dataset_level/config_example.properties".split(" "));
        compareFiles(
                "test-cases/METADATA_TEST_dataset_level/target_metadata.nq",
                "test-cases/METADATA_TEST_dataset_level/generated_metadata.nq",
                true
        );
    }

    @Test
    public void tripleLevelTest() {
        Main.main("-c ./test-cases/METADATA_TEST_triple_level/config_example.properties".split(" "));
        compareFiles(
                "test-cases/METADATA_TEST_triple_level/target_metadata.nq",
                "test-cases/METADATA_TEST_triple_level/generated_metadata.nq",
                true
        );
    }

    @Test
    public void termLevelTest() {
        Main.main("-c ./test-cases/METADATA_TEST_term_level/config_example.properties".split(" "));
        compareFiles(
                "test-cases/METADATA_TEST_term_level/target_metadata.nq",
                "test-cases/METADATA_TEST_term_level/generated_metadata.nq",
                true
        );
    }
}

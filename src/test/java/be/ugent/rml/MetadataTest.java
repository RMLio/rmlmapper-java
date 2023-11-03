package be.ugent.rml;

import be.ugent.rml.cli.Main;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Metadata files are compared as strings, without loading them in a QuadStore first.
 * So when making tests, make sure to use the exact same format as the mapper output.
 */
public class MetadataTest extends TestCore {
    private final static Logger logger = LoggerFactory.getLogger(MetadataTest.class);

    @Test
    public void datasetLevelTest() throws Exception {
        Main.run("-c metadata-test-cases/metadata-dataset-level/nquads/config_example.properties".split(" "));
        compareFiles(
                "metadata-test-cases/metadata-dataset-level/nquads/target_metadata.nq",
                "./generated_metadata.nq",
                true
        );

        cleanup();
    }

    @Test
    public void datasetLevelTestTurtle() throws Exception {
        Main.run("-c metadata-test-cases/metadata-dataset-level/turtle/config_example.properties".split(" "));
        compareFiles(
                "metadata-test-cases/metadata-dataset-level/turtle/target_metadata.ttl",
                "./generated_metadata.ttl",
                true
        );

        cleanup("turtle");
    }

    @Test
    public void tripleLevelTest() throws Exception {
        Main.run("-c metadata-test-cases/metadata-triple-level/config_example.properties".split(" "));
        compareFiles(
                "metadata-test-cases/metadata-triple-level/target_metadata.nq",
                "./generated_metadata.nq",
                true
        );

        cleanup();
    }

    @Test
    public void termLevelTest() throws Exception {
        Main.run("-c metadata-test-cases/metadata-term-level/config_example.properties".split(" "));
        compareFiles(
                "metadata-test-cases/metadata-term-level/target_metadata.nq",
                "./generated_metadata.nq",
                true
        );

        cleanup();
    }

    private void cleanup() {
        cleanup("nquads");
    }

    private void cleanup(String format) {
        try {
            File outputFile;
            if (format.equals("nquads")) {
                outputFile = Utils.getFile("./generated_output.nq");
                assertTrue(outputFile.delete());
                outputFile = Utils.getFile("./generated_metadata.nq");
            } else {
                outputFile = Utils.getFile("./generated_output.ttl");
                assertTrue(outputFile.delete());
                outputFile = Utils.getFile("./generated_metadata.ttl");
            }
            assertTrue(outputFile.delete());
        } catch (Exception e) {
            logger.warn("Could not clean up temporary files", e);
        }
    }
}

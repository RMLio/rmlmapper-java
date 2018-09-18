package be.ugent.rml;

import be.ugent.rml.cli.Main;
import org.junit.Test;

import java.io.File;
import static org.junit.Assert.assertTrue;

public class Arguments_Test extends TestCore {

    @Test
    public void withConfigFile() {
        Main.main("-c ./ARGUMENT_TEST_config_file/config_example.properties".split(" "));
        compareFiles(
                "./ARGUMENT_TEST_config_file/target_output.nq",
                "./generated_output.nq",
                false
        );

        File outputFile = null;
        try {
            outputFile = Utils.getFile("./generated_output.nq");
            assertTrue(outputFile.delete());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void withoutConfigFile() {
        Main.main("-m ./ARGUMENT_TEST_config_file/mapping.ttl -o ./generated_output.nq".split(" "));
        compareFiles(
                "./ARGUMENT_TEST_config_file/target_output.nq",
                "./generated_output.nq",
                false
        );

        File outputFile = null;
        try {
            outputFile = Utils.getFile("./generated_output.nq");
            assertTrue(outputFile.delete());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

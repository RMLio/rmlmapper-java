package be.ugent.rml;

import be.ugent.rml.cli.Main;
import org.junit.Test;

public class Arguments_Test extends TestCore {

    @Test
    public void withConfigFile() {
        Main.main("-c ./ARGUMENT_TEST_config_file/config_example.properties".split(" "));
        compareFiles(
                "ARGUMENT_TEST_config_file/target_output.nq",
                "ARGUMENT_TEST_config_file/generated_output.nq",
                false
        );
    }

    @Test
    public void withoutConfigFile() {
        Main.main("-m ./ARGUMENT_TEST_config_file/mapping.ttl -o src/test/resources/ARGUMENT_TEST_config_file/generated_output.nq".split(" "));
        compareFiles(
                "ARGUMENT_TEST_config_file/target_output.nq",
                "ARGUMENT_TEST_config_file/generated_output.nq",
                false
        );
    }
}

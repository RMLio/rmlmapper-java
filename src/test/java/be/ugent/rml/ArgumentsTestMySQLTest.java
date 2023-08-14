package be.ugent.rml;

import be.ugent.rml.cli.Main;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ArgumentsTestMySQLTest extends MySQLTestCore {

    @BeforeAll
    public static void beforeClass() {
        logger = LoggerFactory.getLogger(ArgumentsTestMySQLTest.class);
    }

    @Test
    public void executeR2RML() throws Exception {
        String cwd = (new File("./src/test/resources/argument/r2rml")).getAbsolutePath();
        String mappingFilePath = (new File(cwd, "mapping.r2rml.ttl")).getAbsolutePath();
        String actualPath = (new File("./generated_output.nq")).getAbsolutePath();
        String expectedPath = (new File(cwd, "output.nq")).getAbsolutePath();
        String resourcePath = "src/test/resources/argument/r2rml/resource.sql";

        prepareDatabase(resourcePath, "root", "");

        Main.run(new String[]{"-m" , mappingFilePath , "-o" , actualPath , "--r2rml-jdbcDSN" , getDbURL() , "--r2rml-username", "root","-v"}, cwd);
        compareFiles(
                expectedPath,
                actualPath,
                false
        );

        try {
            File outputFile = Utils.getFile(actualPath);
            assertTrue(outputFile.delete());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package be.ugent.rml;

import be.ugent.rml.cli.Main;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class Arguments_Test_MySQL extends MySQLTestCore {

    @BeforeClass
    public static void beforeClass() {
        logger = LoggerFactory.getLogger(Arguments_Test_MySQL.class);
    }

    @Test
    public void executeR2RML() throws Exception {
        String cwd = (new File("./src/test/resources/argument/r2rml")).getAbsolutePath();
        String mappingFilePath = (new File(cwd, "mapping.r2rml.ttl")).getAbsolutePath();
        String actualPath = (new File("./generated_output.nq")).getAbsolutePath();
        String expectedPath = (new File(cwd, "output.nq")).getAbsolutePath();
        String resourcePath = "src/test/resources/argument/r2rml/resource.sql";

        prepareDatabase(resourcePath, "root", "");

        Main.main(("-m " + mappingFilePath + " -o " + actualPath + " --r2rml-jdbcDSN " + dbURL + " --r2rml-username root -v").split(" "), cwd);
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

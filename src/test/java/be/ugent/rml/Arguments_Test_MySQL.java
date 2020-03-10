package be.ugent.rml;

import be.ugent.rml.cli.Main;
import ch.vorburger.exec.ManagedProcessException;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class Arguments_Test_MySQL extends MySQLTestCore {

    @Test
    public void executeR2RML() throws Exception {
        String cwd = (new File( "./src/test/resources/argument/r2rml")).getAbsolutePath();
        String mappingFilePath = (new File(cwd, "mapping.r2rml.ttl")).getAbsolutePath();
        String actualPath = (new File("./generated_output.nq")).getAbsolutePath();
        String expectedPath = (new File( cwd, "output.nq")).getAbsolutePath();
        String resourcePath = "argument/r2rml/resource.sql";

        // Get SQL resource
        try {
            mysqlDB.source(resourcePath);
        } catch (ManagedProcessException e) {
            e.printStackTrace();
            fail();
        }

        Main.main(("-m " + mappingFilePath + " -o " + actualPath + " --r2rml-jdbcDSN " + CONNECTIONSTRING + " --r2rml-username root").split(" "), cwd);
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

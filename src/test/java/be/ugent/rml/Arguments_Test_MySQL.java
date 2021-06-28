package be.ugent.rml;

import be.ugent.rml.cli.Main;
import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class Arguments_Test_MySQL extends MySQLTestCore {

    private static String CONNECTIONSTRING;

    @BeforeClass
    public static void before() throws Exception {
        int portNumber = Utils.getFreePortNumber();
        CONNECTIONSTRING = getConnectionString(portNumber);
        mysqlDB = setUpMySQLDBInstance(portNumber);
    }

    @AfterClass
    public static void after() throws ManagedProcessException {
        stopDBs();
    }

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

        Main.main(("-m " + mappingFilePath + " -o " + actualPath + " --r2rml-jdbcDSN " + CONNECTIONSTRING + " --r2rml-username root -v").split(" "), cwd);
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

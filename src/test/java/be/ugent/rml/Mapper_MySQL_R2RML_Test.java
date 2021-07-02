package be.ugent.rml;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.HashMap;

import static be.ugent.rml.MyFileUtils.getParentPath;

// Adapted from Mapper_MySQL_Test to include connection options for R2RML mapping files.
@RunWith(Parameterized.class)
public class Mapper_MySQL_R2RML_Test extends MySQLTestCore{

    private static String CONNECTIONSTRING;

    @BeforeClass
    public static void before() throws Exception {
        int portNumber = Utils.getFreePortNumber();
        CONNECTIONSTRING = getConnectionString(portNumber);
        mysqlDB = setUpMySQLDBInstance(portNumber);

        // add RDB connection options
        mappingOptions = new HashMap<>();
        mappingOptions.put("jdbcDSN", CONNECTIONSTRING);
        mappingOptions.put("username", "root");
        mappingOptions.put("password", "");
    }

    @AfterClass
    public static void after() throws ManagedProcessException {
        stopDBs();
    }

    @Parameterized.Parameter(0)
    public String testCaseName;

    @Parameterized.Parameter(1)
    public Class<? extends Exception> expectedException;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Parameterized.Parameters(name = "{index}: mySQL_{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"R2RMLTC0000", null},
                {"R2RMLTC0001a", null},
                {"R2RMLTC0001b", null},
                {"R2RMLTC0002a", null},
                {"R2RMLTC0002b", null},
                {"R2RMLTC0002c", Error.class},
                {"R2RMLTC0002d", null},
                {"R2RMLTC0002e", Error.class},
//                {"R2RMLTC0002f", Error.class}, Issue #189
                {"R2RMLTC0002g", Error.class},
//                {"R2RMLTC0002h", Error.class}, Issue #189
                {"R2RMLTC0002i", null},
                {"R2RMLTC0002j", null},
                {"R2RMLTC0003b", null},
                {"R2RMLTC0003c", null},
                {"R2RMLTC0004a", null},
//                {"R2RMLTC0004b", Error.class}, Issue #189
                {"R2RMLTC0005a", null},
                {"R2RMLTC0005b", null},
                {"R2RMLTC0006a", null},
                {"R2RMLTC0007a", null},
                {"R2RMLTC0007b", null},
                {"R2RMLTC0007c", null},
                {"R2RMLTC0007d", null},
                {"R2RMLTC0007e", null},
                {"R2RMLTC0007f", null},
                {"R2RMLTC0007g", null},
                {"R2RMLTC0007h", Error.class},
                {"R2RMLTC0008a", null},
                {"R2RMLTC0008b", null},
                {"R2RMLTC0008c", null},
                {"R2RMLTC0009a", null},
                {"R2RMLTC0009b", null},
                {"R2RMLTC0009c", null},
                {"R2RMLTC0009d", null},
                {"R2RMLTC0010a", null},
                {"R2RMLTC0010b", null},
                {"R2RMLTC0010c", null},
                {"R2RMLTC0011a", null},
                {"R2RMLTC0011b", null},
                {"R2RMLTC0012a", null}, // Issue #203, resolved
                {"R2RMLTC0012b", null},
                {"R2RMLTC0012c", Error.class},
                {"R2RMLTC0012d", Error.class},
                {"R2RMLTC0012e", null}, // Issue #203, resolved
                {"R2RMLTC0013a", null},
                {"R2RMLTC0014a", null},
                {"R2RMLTC0014b", null},
                {"R2RMLTC0014c", null},
                {"R2RMLTC0014d", null},
                {"R2RMLTC0015a", null},
                {"R2RMLTC0015b", Error.class},
                {"R2RMLTC0016a", null},
                {"R2RMLTC0016b", null},
                {"R2RMLTC0016c", null},
                {"R2RMLTC0016d", null},
//                {"R2RMLTC0016e", null}, Issues #184, #200
//                {"R2RMLTC0018a", null}, Issue #202
                {"R2RMLTC0019a", null},
//                {"R2RMLTC0019b", Error.class}, Issue #19O
                {"R2RMLTC0020a", null},
//                {"R2RMLTC0020b", Error.class}, Issue #190
        });
    }

    @Test
    public void doMapping() throws Exception {
        mappingTest(testCaseName, expectedException);
    }

    private void mappingTest(String testCaseName, Class expectedException) throws Exception {
        String resourcePath = "test-cases-R2RML/" + testCaseName + "-MySQL/resource.sql";
        String mappingPath = "./test-cases-R2RML/" + testCaseName + "-MySQL/mapping.ttl";
        String outputPath = "test-cases-R2RML/" + testCaseName + "-MySQL/output.nq";

        // Create a temporary copy of the mapping file
        String tempMappingPath = createTempMappingFile(mappingPath);

        // Get SQL resource
        mysqlDB.source(resourcePath, "test");

        // mapping
        String parentPath = getParentPath(getClass(), outputPath);

        if (expectedException == null) {
            doMapping(tempMappingPath, outputPath, parentPath);
        } else {
            doMappingExpectError(tempMappingPath);
        }

        deleteTempMappingFile(tempMappingPath);
    }
}

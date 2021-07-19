package be.ugent.rml;

import ch.vorburger.exec.ManagedProcessException;
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
import static be.ugent.rml.TestStrictMode.*;

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

    @Parameterized.Parameter(2)
    public TestStrictMode testStrictMode;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Parameterized.Parameters(name = "{index}: mySQL_{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"R2RMLTC0000", null, BOTH},
                {"R2RMLTC0001a", null, BOTH},
                {"R2RMLTC0001b", null, BOTH},
                {"R2RMLTC0002a", null, BOTH},
                {"R2RMLTC0002b", null, BOTH},
                {"R2RMLTC0002c", Error.class, BOTH},
                {"R2RMLTC0002d", null, BOTH},
                {"R2RMLTC0002e", Error.class, BOTH},
//                {"R2RMLTC0002f", Error.class, BOTH}, Issue #189
                {"R2RMLTC0002g", Error.class, BOTH},
//                {"R2RMLTC0002h", Error.class, BOTH}, Issue #189
                {"R2RMLTC0002i", null, BOTH},
                {"R2RMLTC0002j", null, BOTH},
                {"R2RMLTC0003b", null, BOTH},
                {"R2RMLTC0003c", null, BOTH},
                {"R2RMLTC0004a", null, BOTH},
                {"R2RMLTC0004b", Error.class, BOTH}, // Issue #189
                {"R2RMLTC0005a", null, BOTH},
                {"R2RMLTC0005b", null, BOTH},
                {"R2RMLTC0006a", null, BOTH},
                {"R2RMLTC0007a", null, BOTH},
                {"R2RMLTC0007b", null, BOTH},
                {"R2RMLTC0007c", null, BOTH},
                {"R2RMLTC0007d", null, BOTH},
                {"R2RMLTC0007e", null, BOTH},
                {"R2RMLTC0007f", null, BOTH},
                {"R2RMLTC0007g", null, BOTH},
                {"R2RMLTC0007h", Error.class, BOTH},
                {"R2RMLTC0008a", null, BOTH},
                {"R2RMLTC0008b", null, BOTH},
                {"R2RMLTC0008c", null, BOTH},
                {"R2RMLTC0009a", null, BOTH},
                {"R2RMLTC0009b", null, BOTH},
                {"R2RMLTC0009c", null, BOTH},
                {"R2RMLTC0009d", null, BOTH},
                {"R2RMLTC0010a", null, BOTH},
                {"R2RMLTC0010b", null, BOTH},
                {"R2RMLTC0010c", null, BOTH},
                {"R2RMLTC0011a", null, BOTH},
                {"R2RMLTC0011b", null, BOTH},
                {"R2RMLTC0012a", null, BOTH}, // Issue #203, resolved
                {"R2RMLTC0012b", null, BOTH},
                {"R2RMLTC0012c", Error.class, BOTH},
                {"R2RMLTC0012d", Error.class, BOTH},
                {"R2RMLTC0012e", null, BOTH}, // Issue #203, resolved
                {"R2RMLTC0013a", null, BOTH},
                {"R2RMLTC0014a", null, BOTH},
                {"R2RMLTC0014b", null, BOTH},
                {"R2RMLTC0014c", null, BOTH},
                {"R2RMLTC0014d", null, BOTH},
                {"R2RMLTC0015a", null, BOTH},
                {"R2RMLTC0015b", Error.class, BOTH},
                {"R2RMLTC0016a", null, BOTH},
                {"R2RMLTC0016b", null, BOTH},
                {"R2RMLTC0016c", null, BOTH},
                {"R2RMLTC0016d", null, BOTH},
                {"R2RMLTC0016e", null, BOTH},// Issues #184, #200, resolved
//                {"R2RMLTC0018a", null, BOTH}, Issue #202
                {"R2RMLTC0019a", null, BOTH},
                /*
                   0019b should only be tested in strict-mode, because it deals
                   with invalid IRIs. In best effort mode, the mapper will try
                   to skip these rows instead of failing.
                */
                {"R2RMLTC0019b", Error.class, STRICT_ONLY}, // Issue #19O, resolved
                {"R2RMLTC0020a", null, BOTH},
                /*
                   002Ob should only be tested in strict-mode, because it deals
                   with invalid IRIs. In best effort mode, the mapper will try
                   to skip these rows instead of failing.
                */
                {"R2RMLTC0020b", Error.class, STRICT_ONLY}, // Issue #190, resolved
        });
    }

    @Test
    public void doMapping() throws Exception {
        if (testStrictMode.equals(BOTH) || testStrictMode.equals(BEST_EFFORT_ONLY)) {
            // test the best-effort mode of the mapper
            mappingTest(testCaseName, expectedException, StrictMode.BEST_EFFORT);
        }
        if (testStrictMode.equals(BOTH) || testStrictMode.equals(STRICT_ONLY)) {
            // test the mapper in strict mode
            mappingTest(testCaseName, expectedException, StrictMode.STRICT);
        }
    }

    private void mappingTest(String testCaseName, Class expectedException, StrictMode strictMode) throws Exception {
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
            doMapping(tempMappingPath, outputPath, parentPath, strictMode);
        } else {
            doMappingExpectError(tempMappingPath, strictMode);
        }

        deleteTempMappingFile(tempMappingPath);
    }
}

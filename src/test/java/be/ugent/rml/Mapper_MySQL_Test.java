package be.ugent.rml;

import org.junit.BeforeClass;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static be.ugent.rml.MyFileUtils.getParentPath;
import static be.ugent.rml.TestStrictMode.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Mapper_MySQL_Test extends MySQLTestCore {
    @BeforeClass
    public static void beforeClass() {
        logger = LoggerFactory.getLogger(Mapper_MySQL_Test.class);
    }

    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                // scenarios:
                {"RMLTC0000", null, BOTH},
                {"RMLTC0001a", null, BOTH},
                {"RMLTC0001b", null, BOTH},
                {"RMLTC0002a", null, BOTH},
                {"RMLTC0002b", null, BOTH},
                {"RMLTC0002c", Error.class, BOTH},
                {"RMLTC0002d", null, BOTH},
                {"RMLTC0002e", Error.class, BOTH},
//                {"RMLTC0002f", null, TestMode.BOTH},
                {"RMLTC0002g", Error.class, BOTH},
                {"RMLTC0002h", Error.class, BOTH},
                {"RMLTC0002i", null, BOTH},
                {"RMLTC0002j", null, BOTH},
                {"RMLTC0002k", null, BOTH},
                {"RMLTC0003a", null, BOTH},
                {"RMLTC0003b", null, BOTH},
                {"RMLTC0003c", null, BOTH},
                {"RMLTC0004a", null, BOTH},
                {"RMLTC0004b", Error.class, BOTH},
                {"RMLTC0005a", null, BOTH},
                {"RMLTC0005b", null, BOTH},
                {"RMLTC0006a", null, BOTH},
                {"RMLTC0007a", null, BOTH},
                {"RMLTC0007b", null, BOTH},
                {"RMLTC0007c", null, BOTH},
                {"RMLTC0007d", null, BOTH},
                {"RMLTC0007e", null, BOTH},
                {"RMLTC0007f", null, BOTH},
                {"RMLTC0007g", null, BOTH},
                {"RMLTC0007h", Error.class, BOTH},
                {"RMLTC0008a", null, BOTH},
                {"RMLTC0008b", null, BOTH},
                {"RMLTC0008c", null, BOTH},
                {"RMLTC0009a", null, BOTH},
                {"RMLTC0009b", null, BOTH},
                {"RMLTC0009c", null, BOTH},
                {"RMLTC0009d", null, BOTH},
                {"RMLTC0010a", null, BOTH},
                {"RMLTC0010b", null, BOTH},
                {"RMLTC0010c", null, BOTH},
//                {"RMLTC0011a", null, BOTH},
                {"RMLTC0011b", null, BOTH},
                {"RMLTC0012a", null, BOTH},
                {"RMLTC0012b", null, BOTH},
                {"RMLTC0012c", Error.class, BOTH},
                {"RMLTC0012d", Error.class, BOTH},
                {"RMLTC0012e", null, BOTH},
                {"RMLTC0013a", null, BOTH},
                {"RMLTC0014d", null, BOTH},
//                {"RMLTC0015a", null, BOTH},
                {"RMLTC0015b", Error.class, BOTH},
                {"RMLTC0016a", null, BOTH},
                {"RMLTC0016b", null, BOTH},
                {"RMLTC0016c", null, BOTH},
                {"RMLTC0016d", null, BOTH},
                {"RMLTC0016e", null, BOTH}, // Issue 184, resolved
//                {"RMLTC0018a", null, BOTH},
                {"RMLTC0019a", null, BOTH},
                /*
                    Expected output for RMLTC0019b is written for best-effort operation.
                    The case will fail in strict mode.
                 */
                {"RMLTC0019b", null, BEST_EFFORT_ONLY},
                {"RMLTC0020a", null, BOTH},
                /*
                    Expected output for RMLTC0020b is written for best-effort operation.
                    The case will fail in strict mode.
                 */
                {"RMLTC0020b", null, BEST_EFFORT_ONLY},
                {"RMLTC1019", null, BOTH},
                {"RMLTC1020", null, BOTH},
                {"RMLTC1022", null, BOTH},
                {"RMLTC1027", null, BOTH}
        });
    }

    @ParameterizedTest(name = "{index}: mySQL_{0}")
    @MethodSource("data")
    public void doMapping(String testCaseName, Class expectedException, TestStrictMode testStrictMode) throws Exception {
        prepareDatabase(String.format("src/test/resources/test-cases/%s-MySQL/resource.sql", testCaseName), USERNAME, PASSWORD);
        if (testStrictMode.equals(BOTH) || testStrictMode.equals(BEST_EFFORT_ONLY)) {
            // test the best-effort mode of the mapper
            mappingTest(testCaseName, expectedException, StrictMode.BEST_EFFORT);
        }
        if (testStrictMode.equals(BOTH) || testStrictMode.equals(STRICT_ONLY)) {
            // test the mapper in strict mode
            mappingTest(testCaseName, expectedException, StrictMode.STRICT);
        }
    }

    private void mappingTest(String testCaseName, Class expectedException, StrictMode strictMode) {
        String mappingPath = "./test-cases/" + testCaseName + "-MySQL/mapping.ttl";
        String outputPath = "test-cases/" + testCaseName + "-MySQL/output.nq";

        // Create a temporary copy of the mapping file and replace source details
        String tempMappingPath = CreateTempMappingFileAndReplaceDSN(mappingPath, dbURL);

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

package be.ugent.rml;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

import static be.ugent.rml.MyFileUtils.getParentPath;
import static be.ugent.rml.TestStrictMode.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MapperMySQLTest extends MySQLTestCore {
    @BeforeAll
    public static void beforeClass() {
        logger = LoggerFactory.getLogger(MapperMySQLTest.class);
    }

    static Stream<Arguments> data() {
        return Stream.of(
                // scenarios:
                Arguments.of("RMLTC0000", false, BOTH),
                Arguments.of("RMLTC0001a", false, BOTH),
                Arguments.of("RMLTC0001b", false, BOTH),
                Arguments.of("RMLTC0002a", false, BOTH),
                Arguments.of("RMLTC0002b", false, BOTH),
                Arguments.of("RMLTC0002c", true, BOTH),
                Arguments.of("RMLTC0002d", false, BOTH),
                Arguments.of("RMLTC0002e", true, BOTH),
//Arguments.of("RMLTC0002f", false, TestMode.BOTH),
                Arguments.of("RMLTC0002g", true, BOTH),
                Arguments.of("RMLTC0002h", true, BOTH),
                Arguments.of("RMLTC0002i", false, BOTH),
                Arguments.of("RMLTC0002j", false, BOTH),
                Arguments.of("RMLTC0002k", false, BOTH),
                Arguments.of("RMLTC0003a", false, BOTH),
                Arguments.of("RMLTC0003b", false, BOTH),
                Arguments.of("RMLTC0003c", false, BOTH),
                Arguments.of("RMLTC0004a", false, BOTH),
                Arguments.of("RMLTC0004b", true, BOTH),
                Arguments.of("RMLTC0005a", false, BOTH),
                Arguments.of("RMLTC0005b", false, BOTH),
                Arguments.of("RMLTC0006a", false, BOTH),
                Arguments.of("RMLTC0007a", false, BOTH),
                Arguments.of("RMLTC0007b", false, BOTH),
                Arguments.of("RMLTC0007c", false, BOTH),
                Arguments.of("RMLTC0007d", false, BOTH),
                Arguments.of("RMLTC0007e", false, BOTH),
                Arguments.of("RMLTC0007f", false, BOTH),
                Arguments.of("RMLTC0007g", false, BOTH),
                Arguments.of("RMLTC0007h", true, BOTH),
                Arguments.of("RMLTC0008a", false, BOTH),
                Arguments.of("RMLTC0008b", false, BOTH),
                Arguments.of("RMLTC0008c", false, BOTH),
                Arguments.of("RMLTC0009a", false, BOTH),
                Arguments.of("RMLTC0009b", false, BOTH),
                Arguments.of("RMLTC0009c", false, BOTH),
                Arguments.of("RMLTC0009d", false, BOTH),
                Arguments.of("RMLTC0010a", false, BOTH),
                Arguments.of("RMLTC0010b", false, BOTH),
                Arguments.of("RMLTC0010c", false, BOTH),
//Arguments.of("RMLTC0011a", false, BOTH),
                Arguments.of("RMLTC0011b", false, BOTH),
                Arguments.of("RMLTC0012a", false, BOTH),
                Arguments.of("RMLTC0012b", false, BOTH),
                Arguments.of("RMLTC0012c", true, BOTH),
                Arguments.of("RMLTC0012d", true, BOTH),
                Arguments.of("RMLTC0012e", false, BOTH),
                Arguments.of("RMLTC0013a", false, BOTH),
                Arguments.of("RMLTC0014d", false, BOTH),
//Arguments.of("RMLTC0015a", false, BOTH),
                Arguments.of("RMLTC0015b", true, BOTH),
                Arguments.of("RMLTC0016a", false, BOTH),
                Arguments.of("RMLTC0016b", false, BOTH),
                Arguments.of("RMLTC0016c", false, BOTH),
                Arguments.of("RMLTC0016d", false, BOTH),
                Arguments.of("RMLTC0016e", false, BOTH), // Issue 184, resolved
//Arguments.of("RMLTC0018a", false, BOTH),
                Arguments.of("RMLTC0019a", false, BOTH),
                /*
                    Expected output for RMLTC0019b is written for best-effort operation.
                    The case will fail in strict mode.
                 */
                Arguments.of("RMLTC0019b", false, BEST_EFFORT_ONLY),
                Arguments.of("RMLTC0020a", false, BOTH),
                /*
                    Expected output for RMLTC0020b is written for best-effort operation.
                    The case will fail in strict mode.
                 */
                Arguments.of("RMLTC0020b", false, BEST_EFFORT_ONLY),
                Arguments.of("RMLTC1019", false, BOTH),
                Arguments.of("RMLTC1020", false, BOTH),
                Arguments.of("RMLTC1022", false, BOTH),
                Arguments.of("RMLTC1027", false, BOTH)
        );
    }

    @ParameterizedTest(name = "{index}: mySQL_{0}")
    @MethodSource("data")
    public void doMapping(String testCaseName, boolean expectedException, TestStrictMode testStrictMode) throws Exception {
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

    private void mappingTest(String testCaseName, boolean expectedException, StrictMode strictMode) {
        String mappingPath = "./test-cases/" + testCaseName + "-MySQL/mapping.ttl";
        String outputPath = "test-cases/" + testCaseName + "-MySQL/output.nq";

        // Create a temporary copy of the mapping file and replace source details
        String tempMappingPath = CreateTempMappingFileAndReplaceDSN(mappingPath);

        // mapping
        String parentPath = getParentPath(getClass(), outputPath);

        if (!expectedException) {
            doMapping(tempMappingPath, outputPath, parentPath, strictMode);
        } else {
            doMappingExpectError(tempMappingPath, strictMode);
        }

        deleteTempMappingFile(tempMappingPath);
    }
}

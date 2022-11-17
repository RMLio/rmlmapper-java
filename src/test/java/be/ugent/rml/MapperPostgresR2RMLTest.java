package be.ugent.rml;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.stream.Stream;

import static be.ugent.rml.MyFileUtils.getParentPath;
import static be.ugent.rml.TestStrictMode.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MapperPostgresR2RMLTest extends PostgresTestCore {
    public static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of("R2RMLTC0000", false, BOTH),
                Arguments.of("R2RMLTC0001a", false, BOTH),
                Arguments.of("R2RMLTC0001b", false, BOTH),
                Arguments.of("R2RMLTC0002a", false, BOTH),
                Arguments.of("R2RMLTC0002b", false, BOTH),
                Arguments.of("R2RMLTC0002c", true, BOTH),
                Arguments.of("R2RMLTC0002d", false, BOTH),
                Arguments.of("R2RMLTC0002e", true, BOTH),
//                Arguments.of("R2RMLTC0002f", true, BOTH), Issue #189
                Arguments.of("R2RMLTC0002g", true, BOTH),
                Arguments.of("R2RMLTC0002h", true, BOTH),
                Arguments.of("R2RMLTC0002i", false, BOTH),
                Arguments.of("R2RMLTC0002j", false, BOTH),
                Arguments.of("R2RMLTC0003b", false, BOTH),
                Arguments.of("R2RMLTC0003c", false, BOTH),
                Arguments.of("R2RMLTC0004a", false, BOTH),
                Arguments.of("R2RMLTC0004b", true, BOTH), // Issue #189
                Arguments.of("R2RMLTC0005a", false, BOTH),
                Arguments.of("R2RMLTC0005b", false, BOTH),
                Arguments.of("R2RMLTC0006a", false, BOTH),
                Arguments.of("R2RMLTC0007a", false, BOTH),
                Arguments.of("R2RMLTC0007b", false, BOTH),
                Arguments.of("R2RMLTC0007c", false, BOTH),
                Arguments.of("R2RMLTC0007d", false, BOTH),
                Arguments.of("R2RMLTC0007e", false, BOTH),
                Arguments.of("R2RMLTC0007f", false, BOTH),
                Arguments.of("R2RMLTC0007g", false, BOTH),
                Arguments.of("R2RMLTC0007h", true, BOTH),
                Arguments.of("R2RMLTC0008a", false, BOTH),
                Arguments.of("R2RMLTC0008b", false, BOTH),
                Arguments.of("R2RMLTC0008c", false, BOTH),
                Arguments.of("R2RMLTC0009a", false, BOTH),
                Arguments.of("R2RMLTC0009b", false, BOTH),
                Arguments.of("R2RMLTC0009c", false, BOTH),
                Arguments.of("R2RMLTC0009d", false, BOTH),
                Arguments.of("R2RMLTC0010a", false, BOTH),
                Arguments.of("R2RMLTC0010b", false, BOTH),
                Arguments.of("R2RMLTC0010c", false, BOTH),
                Arguments.of("R2RMLTC0011a", false, BOTH),
                Arguments.of("R2RMLTC0011b", false, BOTH),
                Arguments.of("R2RMLTC0012a", false, BOTH),
                Arguments.of("R2RMLTC0012b", false, BOTH),
                Arguments.of("R2RMLTC0012c", true, BOTH),
                Arguments.of("R2RMLTC0012d", true, BOTH),
                Arguments.of("R2RMLTC0012e", false, BOTH),
                Arguments.of("R2RMLTC0013a", false, BOTH),
                Arguments.of("R2RMLTC0014a", false, BOTH),
                Arguments.of("R2RMLTC0014b", false, BOTH),
                Arguments.of("R2RMLTC0014c", false, BOTH),
                Arguments.of("R2RMLTC0014d", false, BOTH),
                Arguments.of("R2RMLTC0015a", false, BOTH),
                Arguments.of("R2RMLTC0015b", true, BOTH),
                Arguments.of("R2RMLTC0016a", false, BOTH),
//                Arguments.of("R2RMLTC0016b", false, BOTH), Issue #201
                Arguments.of("R2RMLTC0016c", false, BOTH),
                Arguments.of("R2RMLTC0016d", false, BOTH),
                Arguments.of("R2RMLTC0016e", false, BOTH), // Issues #184, #200, resolved
                Arguments.of("R2RMLTC0018a", false, BOTH),
                Arguments.of("R2RMLTC0019a", false, BOTH),
                /* 0019b should only be tested in strict-mode, because it deals
                   with invalid IRIs. In best effort mode, the mapper will try
                   to skip these rows instead of failing. */
                Arguments.of("R2RMLTC0019b", true, STRICT_ONLY), // Issue #19O, resolved
                Arguments.of("R2RMLTC0020a", false, BOTH),
                /* 002Ob should only be tested in strict-mode, because it deals
                   with invalid IRIs. In best effort mode, the mapper will try
                   to skip these rows instead of failing. */
                Arguments.of("R2RMLTC0020b", true, STRICT_ONLY) // Issue #190, resolved
        );
    }

    @BeforeAll
    public static void beforeClass() {
        logger = LoggerFactory.getLogger(MapperPostgresR2RMLTest.class);
    }

    @BeforeEach
    public void before() {
        // add RDB connection options
        mappingOptions = new HashMap<>();
        mappingOptions.put("jdbcDriver", "org.postgresql.Driver");
        mappingOptions.put("username", "postgres");
        mappingOptions.put("password", "");
        mappingOptions.put("jdbcDSN", getDbURL());
    }

    @ParameterizedTest(name = "{index}: PostgreSQL_{0}")
    @MethodSource("data")
    public void doMapping(String testCaseName, boolean expectedException, TestStrictMode testStrictMode) throws Exception {
        prepareDatabase(String.format("src/test/resources/test-cases-R2RML/%s-PostgreSQL/resource.sql", testCaseName), USERNAME, PASSWORD);

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
        String mappingPath = String.format("./test-cases-R2RML/%s-PostgreSQL/mapping.ttl", testCaseName);
        String outputPath = String.format("test-cases-R2RML/%s-PostgreSQL/output.nq", testCaseName);

        // Create a temporary copy of the mapping file
        String tempMappingPath = createTempMappingFile(mappingPath);

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

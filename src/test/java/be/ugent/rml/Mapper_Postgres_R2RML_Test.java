package be.ugent.rml;

import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;

import static be.ugent.rml.MyFileUtils.getParentPath;
import static be.ugent.rml.TestStrictMode.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Mapper_Postgres_R2RML_Test extends PostgresTestCore {
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
                {"R2RMLTC0002h", Error.class, BOTH},
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
                {"R2RMLTC0012a", null, BOTH},
                {"R2RMLTC0012b", null, BOTH},
                {"R2RMLTC0012c", Error.class, BOTH},
                {"R2RMLTC0012d", Error.class, BOTH},
                {"R2RMLTC0012e", null, BOTH},
                {"R2RMLTC0013a", null, BOTH},
                {"R2RMLTC0014a", null, BOTH},
                {"R2RMLTC0014b", null, BOTH},
                {"R2RMLTC0014c", null, BOTH},
                {"R2RMLTC0014d", null, BOTH},
                {"R2RMLTC0015a", null, BOTH},
                {"R2RMLTC0015b", Error.class, BOTH},
                {"R2RMLTC0016a", null, BOTH},
//                {"R2RMLTC0016b", null, BOTH}, Issue #201
                {"R2RMLTC0016c", null, BOTH},
                {"R2RMLTC0016d", null, BOTH},
                {"R2RMLTC0016e", null, BOTH}, // Issues #184, #200, resolved
                {"R2RMLTC0018a", null, BOTH},
                {"R2RMLTC0019a", null, BOTH},
                /* 0019b should only be tested in strict-mode, because it deals
                   with invalid IRIs. In best effort mode, the mapper will try
                   to skip these rows instead of failing. */
                {"R2RMLTC0019b", Error.class, STRICT_ONLY}, // Issue #19O, resolved
                {"R2RMLTC0020a", null, BOTH},
                /* 002Ob should only be tested in strict-mode, because it deals
                   with invalid IRIs. In best effort mode, the mapper will try
                   to skip these rows instead of failing. */
                {"R2RMLTC0020b", Error.class, STRICT_ONLY}, // Issue #190, resolved
        });
    }

    @BeforeClass
    public static void beforeClass() {
        logger = LoggerFactory.getLogger(Mapper_Postgres_R2RML_Test.class);
    }

    @BeforeAll
    public void before() {
        // add RDB connection options
        mappingOptions = new HashMap<>();
        mappingOptions.put("jdbcDriver", "org.postgresql.Driver");
        mappingOptions.put("username", "postgres");
        mappingOptions.put("password", "");
        mappingOptions.put("jdbcDSN", dbURL);
    }

    @ParameterizedTest(name = "{index}: PostgreSQL_{0}")
    @MethodSource("data")
    public void doMapping(String testCaseName, Class expectedException, TestStrictMode testStrictMode) throws Exception {
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

    private void mappingTest(String testCaseName, Class expectedException, StrictMode strictMode) {
        String mappingPath = String.format("./test-cases-R2RML/%s-PostgreSQL/mapping.ttl", testCaseName);
        String outputPath = String.format("test-cases-R2RML/%s-PostgreSQL/output.nq", testCaseName);

        // Create a temporary copy of the mapping file
        String tempMappingPath = createTempMappingFile(mappingPath);

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

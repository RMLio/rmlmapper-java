package be.ugent.rml;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static be.ugent.rml.MyFileUtils.getParentPath;


@RunWith(Parameterized.class)
public class Mapper_Postgres_Test extends PostgresTestCore {

    @BeforeClass
    public static void before() {
        logger = LoggerFactory.getLogger(Mapper_Postgres_Test.class);
        startDBs();
    }

    @AfterClass
    public static void after() {
        stopDBs();
    }

    @Parameterized.Parameter(0)
    public String testCaseName;

    @Parameterized.Parameter(1)
    public Class<? extends Exception> expectedException;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Parameterized.Parameters(name = "{index}: Postgres_{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                // scenarios:
                {"RMLTC0000", null},
                {"RMLTC0001a", null},
                {"RMLTC0001b", null},
                {"RMLTC0002a", null},
                {"RMLTC0002b", null},
                {"RMLTC0002c", Error.class},
                {"RMLTC0002d", null},
                {"RMLTC0002e", Error.class},
//                {"RMLTC0002f", null},
                {"RMLTC0002g", Error.class},
                {"RMLTC0002h", Error.class},
                {"RMLTC0002i", Error.class},
                {"RMLTC0002j", null},
                {"RMLTC0003a", Error.class},
                {"RMLTC0003b", null},
                {"RMLTC0003c", null},
                {"RMLTC0004a", null},
                {"RMLTC0004b", null},
                {"RMLTC0005a", null},
                {"RMLTC0005b", null},
                {"RMLTC0006a", null},
                {"RMLTC0007a", null},
                {"RMLTC0007b", null},
                {"RMLTC0007c", null},
                {"RMLTC0007d", null},
                {"RMLTC0007e", null},
                {"RMLTC0007f", null},
                {"RMLTC0007g", null},
                {"RMLTC0007h", Error.class},
                {"RMLTC0008a", null},
                {"RMLTC0008b", null},
                {"RMLTC0008c", null},
                {"RMLTC0009a", null},
                {"RMLTC0009b", null},
                {"RMLTC0009c", null},
                {"RMLTC0009d", null},
                {"RMLTC0010a", null},
                {"RMLTC0010b", null},
                {"RMLTC0010c", null},
//                {"RMLTC0011a", null},
                {"RMLTC0011b", null},
                {"RMLTC0012a", null},
                {"RMLTC0012b", null},
                {"RMLTC0012c", Error.class},
                {"RMLTC0012d", Error.class},
                {"RMLTC0012e", null},
//                {"RMLTC0013a", null},
                {"RMLTC0014d", null},
//                {"RMLTC0015a", null},
                {"RMLTC0015b", Error.class},
                {"RMLTC0016a", null},
//                {"RMLTC0016b", null},
                {"RMLTC0016c", null},
                {"RMLTC0016d", null},
//                {"RMLTC0016e", null},
                {"RMLTC0018a", null},
                {"RMLTC0019a", null},
                {"RMLTC0019b", null},
                {"RMLTC0020a", null},
                {"RMLTC0020b", null},
        });

    }

    @Test
    public void doMapping() throws Exception {
        mappingTest(testCaseName);
    }

    private void mappingTest(String testCaseName) throws Exception {

        String resourcePath = "test-cases/" + testCaseName + "-PostgreSQL/resource.sql";
        String mappingPath = "./test-cases/" + testCaseName + "-PostgreSQL/mapping.ttl";
        String outputPath = "test-cases/" + testCaseName + "-PostgreSQL/output.nq";

        String tempMappingPath = replaceDSNInMappingFile(mappingPath, CONNECTIONSTRING);

        // Execute SQL
        executeSQL(remoteDB.connectionString, resourcePath);

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

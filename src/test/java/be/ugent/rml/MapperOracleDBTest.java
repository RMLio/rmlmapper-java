package be.ugent.rml;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.util.stream.Stream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MapperOracleDBTest extends DBTestCore {

    // will be shared between test methods, i.e., one instance
    @Container
    private static OracleContainer container;
    public MapperOracleDBTest() {

        super("rmlmapper_test",  "test", "gvenzl/oracle-xe:latest");
        container = new OracleContainer(DockerImageName.parse(DOCKER_TAG))
                .withUsername(USERNAME)
                .withPassword(PASSWORD)
                .withEnv("runID", Integer.toString(this.hashCode()))
                .withEnv("NLS_LANG", "American_America.WE8ISO8859P1");
    }

    public static Stream<Arguments> data() {
        return Stream.of(
                // scenarios:
//                {"RMLTC0000", false},
                Arguments.of("RMLTC0001a", false)
//                {"RMLTC0001b", false},
//                {"RMLTC0002a", false},
//                {"RMLTC0002b", false},
//                {"RMLTC0002c", true},
//                {"RMLTC0002d", false},
//                {"RMLTC0002e", true},
//                {"RMLTC0002f", false},
//                {"RMLTC0002g", true},
//                {"RMLTC0002h", true},
                // TODO see issue #130
//                {"RMLTC0002i", true},
//                {"RMLTC0002j", false},
                // TODO see issue #130
//                {"RMLTC0003a", true},
//                {"RMLTC0003b", false},
//                {"RMLTC0003c", false},
//                {"RMLTC0004a", false},
//                {"RMLTC0004b", true},
//                {"RMLTC0005a", false},
//                {"RMLTC0005b", false},
//                {"RMLTC0006a", false},
//                {"RMLTC0007a", false},
//                {"RMLTC0007b", false},
//                {"RMLTC0007c", false},
//                {"RMLTC0007d", false},
//                {"RMLTC0007e", false},
//                {"RMLTC0007f", false},
//                {"RMLTC0007g", false},
//                {"RMLTC0007h", true},
//                {"RMLTC0008a", false},
//                {"RMLTC0008b", false},
//                {"RMLTC0008c", false},
//                {"RMLTC0009a", false},
//                {"RMLTC0009b", false},
//                {"RMLTC0009c", false},
//                {"RMLTC0009d", false},
//                {"RMLTC0010a", false},
//                {"RMLTC0010b", false},
//                {"RMLTC0010c", false},
//                {"RMLTC0011a", false},
//                {"RMLTC0011b", false},
//                {"RMLTC0012a", false},
//                {"RMLTC0012b", false},
//                {"RMLTC0012c", true},
//                {"RMLTC0012d", true},
//                {"RMLTC0012e", false},
//                {"RMLTC0013a", false},
//                {"RMLTC0014d", false},
//                {"RMLTC0015a", false},
//                {"RMLTC0015b", true},
//                {"RMLTC0016a", false},
//                {"RMLTC0016b", false},
//                {"RMLTC0016c", false},
//                {"RMLTC0016d", false},
//                {"RMLTC0016e", false},
//                {"RMLTC0018a", false},
//                {"RMLTC0019a", false},
//                {"RMLTC0019b", false},
//                {"RMLTC0020a", false},
//                {"RMLTC0020b", false},
        );
    }

    @BeforeAll
    public static void beforeClass() {
        logger = LoggerFactory.getLogger(MapperOracleDBTest.class);
    }

    @AfterEach
    public void afterTest() {
        container.stop();  // for oracle, a fresh container is the easiest way of purging any previous data
        container.start();
    }

    @AfterAll
    public void afterAll() {
        container.stop();
    }

    @ParameterizedTest(name = "{index}: OracleDB_{0}")
    @MethodSource("data")
    public void doMapping(String testCaseName, boolean expectedException) throws Exception {
        prepareDatabase(String.format("src/test/resources/test-cases/%s-OracleDB/resource.sql", testCaseName), USERNAME, PASSWORD);
        mappingTest(testCaseName, expectedException);
    }

    private void mappingTest(String testCaseName, boolean expectedException) {
        String mappingPath = "./test-cases/" + testCaseName + "-OracleDB/mapping.ttl";
        String outputPath = "test-cases/" + testCaseName + "-OracleDB/output.nq";

        // Create a temporary copy of the mapping file and replace source details
        String tempMappingPath = CreateTempMappingFileAndReplaceDSN(mappingPath);

        // mapping
        if (!expectedException) {
            doMapping(tempMappingPath, outputPath);
        } else {
            doMappingExpectError(tempMappingPath);
        }

        deleteTempMappingFile(tempMappingPath);
    }

    @Override
    protected String getDbURL() {
        return container.getJdbcUrl();
    }
}

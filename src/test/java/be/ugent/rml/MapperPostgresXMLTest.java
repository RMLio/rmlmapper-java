package be.ugent.rml;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MapperPostgresXMLTest extends PostgresTestCore {
    @BeforeAll
    public static void beforeClass() {
        logger = LoggerFactory.getLogger(MapperPostgresXMLTest.class);
    }

    static Stream<Arguments> data() {
        return Stream.of(
                // scenarios:
//                Arguments.of("RMLTC0000", false),
//                Arguments.of("RMLTC0001a", false),
                Arguments.of("RMLTC0001b", false),
//                Arguments.of("RMLTC0002a", false),
//                Arguments.of("RMLTC0002b", false),
//                Arguments.of("RMLTC0002c", Error.class),
                Arguments.of("RMLTC0002d", false),
//                Arguments.of("RMLTC0002e", Error.class),
////                Arguments.of("RMLTC0002f", false),
                Arguments.of("RMLTC0002g", true),
//                Arguments.of("RMLTC0002h", false),
//                Arguments.of("RMLTC0002i", Error.class),
//                Arguments.of("RMLTC0002j", false),
//                Arguments.of("RMLTC0003a", Error.class),
//                Arguments.of("RMLTC0003b", false),
//                Arguments.of("RMLTC0003c", false),
//                Arguments.of("RMLTC0004a", false),
//                Arguments.of("RMLTC0004b", false),
//                Arguments.of("RMLTC0005a", false),
//                Arguments.of("RMLTC0005b", false),
//                Arguments.of("RMLTC0006a", false),
//                Arguments.of("RMLTC0007a", false),
//                Arguments.of("RMLTC0007b", false),
//                Arguments.of("RMLTC0007c", false),
//                Arguments.of("RMLTC0007d", false),
//                Arguments.of("RMLTC0007e", false),
//                Arguments.of("RMLTC0007f", false),
//                Arguments.of("RMLTC0007g", false),
//                Arguments.of("RMLTC0007h", false),
//                Arguments.of("RMLTC0008a", false),
//                Arguments.of("RMLTC0008b", false),
//                Arguments.of("RMLTC0008c", false),
//                Arguments.of("RMLTC0009a", false),
//                Arguments.of("RMLTC0009b", false),
                Arguments.of("RMLTC0009c", false),
//                See issue 102
//                Arguments.of("RMLTC0009d", false),
//                Arguments.of("RMLTC0010a", false),
//                Arguments.of("RMLTC0010b", false),
//                Arguments.of("RMLTC0010c", false),
////                Arguments.of("RMLTC0011a", false),
//                Arguments.of("RMLTC0011b", false),
//                Arguments.of("RMLTC0012a", false),
//                Arguments.of("RMLTC0012b", false),
//                Arguments.of("RMLTC0012c", Error.class),
//                Arguments.of("RMLTC0012d", Error.class),
//                Arguments.of("RMLTC0012e", false),
////                Arguments.of("RMLTC0013a", false),
                Arguments.of("RMLTC0014d", false),
////                Arguments.of("RMLTC0015a", false),
//                Arguments.of("RMLTC0015b", Error.class),
//                Arguments.of("RMLTC0016a", false),
////                Arguments.of("RMLTC0016b", false),
//                Arguments.of("RMLTC0016c", false),
//                Arguments.of("RMLTC0016d", false),
////                Arguments.of("RMLTC0016e", false),
//                Arguments.of("RMLTC0018a", false),
                Arguments.of("RMLTC0019a", false)
//                Arguments.of("RMLTC0019b", false),
//                Arguments.of("RMLTC0020a", false),
//                Arguments.of("RMLTC0020b", false),
        );
    }

    @ParameterizedTest(name = "{index}: Postgres_XML_{0}")
    @MethodSource("data")
    public void doMapping(String testCaseName, boolean expectedException) throws Exception {
        prepareDatabase(String.format("src/test/resources/test-cases/%s-PostgreSQL-XML/resource.sql", testCaseName), USERNAME, PASSWORD);

        String mappingPath = "./test-cases/" + testCaseName + "-PostgreSQL-XML/mapping.ttl";
        String outputPath = "test-cases/" + testCaseName + "-PostgreSQL-XML/output.nq";

        String tempMappingPath = CreateTempMappingFileAndReplaceDSN(mappingPath);

        if (!expectedException) {
            doMapping(tempMappingPath, outputPath);
        } else {
            doMappingExpectError(tempMappingPath);
        }

        deleteTempMappingFile(tempMappingPath);
    }
}

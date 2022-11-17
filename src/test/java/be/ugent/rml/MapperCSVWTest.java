package be.ugent.rml;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class MapperCSVWTest extends TestCore {

    public static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of("RMLTC0002a_comment_prefix"),
                Arguments.of("RMLTC0002a_delimiter"),
                Arguments.of("RMLTC0002a_encoding"),
                Arguments.of("RMLTC0002a_tabs"),
                Arguments.of("RMLTC0002a_tabs_unicode"),
                Arguments.of("RMLTC0002a_trim"),
                Arguments.of("RMLTC1002a_null"),
                Arguments.of("RMLTC1002a_null_ignore"),
                Arguments.of("RMLTC1002a_nulls"),
                Arguments.of("RMLTC1025_missing_column_names"),
                Arguments.of("RMLTC1035_bom")
        );

    }

    @ParameterizedTest
    @MethodSource("data")
    public void doCSVWTest(String name) {
        String base = "./test-cases-CSVW/" + name + "-CSVW/";
        doMapping(base + "mapping.ttl", base + "output.nq");
    }
}

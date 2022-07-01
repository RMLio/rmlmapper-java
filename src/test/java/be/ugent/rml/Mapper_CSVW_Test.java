package be.ugent.rml;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

@RunWith(Parameterized.class)
public class Mapper_CSVW_Test extends TestCore {
    @Parameterized.Parameter(0)
    public String testCaseName;

    @Parameterized.Parameters(name = "{index}: CSVW_{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"RMLTC0002a_comment_prefix"},
                {"RMLTC0002a_delimiter"},
                {"RMLTC0002a_encoding"},
                {"RMLTC0002a_tabs"},
                {"RMLTC0002a_tabs_unicode"},
                {"RMLTC0002a_trim"},
                {"RMLTC1002a_null"},
                {"RMLTC1002a_null_ignore"},
                {"RMLTC1002a_nulls"},
                {"RMLTC1025_missing_column_names"},
                {"RMLTC1035_bom"}
        });

    }

    @Test
    public void doMapping() throws Exception {

//        //setup expected exception
//        if (expectedException != null) {
//            thrown.expect(expectedException);
//        }
        doCSVWTest(testCaseName);
    }

    private void doCSVWTest(String name) {
        String base = "./test-cases-CSVW/" + name + "-CSVW/";
        doMapping(base + "mapping.ttl", base + "output.nq");
    }
}

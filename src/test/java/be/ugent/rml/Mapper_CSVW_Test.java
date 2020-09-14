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
                {"delimiter"},
                {"encoding"},
                {"tabs"},
                {"tabs_unicode"},
                {"trim"}
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
        String base = "./test-cases-CSVW/RMLTC0002a_" + name + "-CSVW/";
        doMapping(base + "mapping.ttl", base + "output.nq");
    }
}

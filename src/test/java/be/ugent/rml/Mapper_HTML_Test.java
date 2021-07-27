package be.ugent.rml;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

@RunWith(Parameterized.class)
public class Mapper_HTML_Test extends TestCore {
    @Parameterized.Parameter(0)
    public String testCaseName;

    @Parameterized.Parameters(name = "{index}: CSVW_{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"RMLTC0001a"},
                {"RMLTC0015a"}
        });

    }

    @Test
    public void doMapping() throws Exception {
        doCSVWTest(testCaseName);
    }

    private void doCSVWTest(String name) {
        String base = "./test-cases-HTML/" + name + "-HTML/";
        doMapping(base + "mapping.ttl", base + "output.nq");
    }
}

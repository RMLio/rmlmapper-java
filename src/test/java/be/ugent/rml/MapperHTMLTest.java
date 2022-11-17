package be.ugent.rml;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


public class MapperHTMLTest extends TestCore {

    @ParameterizedTest
    @ValueSource(strings = {"RMLTC0001a", "RMLTC0015a"})
    public void doCSVWTest(String name) {
        String base = "./test-cases-HTML/" + name + "-HTML/";
        doMapping(base + "mapping.ttl", base + "output.nq");
    }
}

package be.ugent.rml;

import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class MapperJoinAndTargetsBugTest extends TestCore {
    @Test
    public void evaluate_joins_and_targets() {
        Map<Term, String> outPaths = new HashMap<>();
        outPaths.put(new NamedNode("http://example.com/base/StudentTarget"), "./joins-and-targets/students.ttl");
        outPaths.put(new NamedNode("http://example.com/base/SportTarget"), "./joins-and-targets/sports.ttl");
        //outPaths.put(new NamedNode("rmlmapper://default.store"), "./new-test-cases/io/RMLTTC0002f/default.nq");
        doMapping("./joins-and-targets/mapping.rml.ttl", outPaths);
    }
}

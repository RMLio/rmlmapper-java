package be.ugent.rml;

import org.junit.Ignore;
import org.junit.Test;

public class Mapper_SPARQL_Test extends TestCore {
    @Ignore
    public void evaluate_example() {
        doMapping("./test-cases/example-SPARQL/mapping.ttl", "./test-cases/example-SPARQL/output.ttl");
    }

}

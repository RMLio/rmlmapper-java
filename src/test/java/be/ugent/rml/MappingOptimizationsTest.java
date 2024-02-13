package be.ugent.rml;

import org.junit.jupiter.api.Test;

public class MappingOptimizationsTest extends TestCore {

    @Test
    public void selfJoinWithoutCondition() {
        doMapping("./mapping-optimizations/self-join-without-condition/mapping.ttl", "./mapping-optimizations/self-join-without-condition/output.nt");
    }

    @Test
    public void sameLogicalSource() {
        doMapping("./mapping-optimizations/same-logical-source/mapping.ttl", "./mapping-optimizations/same-logical-source/output.nt");
    }

    @Test
    public void differentLogicalSources() {
        doMapping("./mapping-optimizations/different-logical-sources/mapping.ttl", "./mapping-optimizations/different-logical-sources/output.nt");
    }

    @Test
    public void sameLogicalSourceWithJoinCondition() {
        doMapping("./mapping-optimizations/same-logical-source-with-join-condition/mapping.ttl", "./mapping-optimizations/same-logical-source-with-join-condition/output.nt");
    }

    @Test
    public void sameLogicalSourceWithJoinCondition2() {
        doMapping("./mapping-optimizations/same-logical-source-with-join-condition2/mapping.ttl", "./mapping-optimizations/same-logical-source-with-join-condition2/output.nt");
    }

    @Test
    public void sameLogicalSourceWithJoinCondition3() {
        doMapping("./mapping-optimizations/same-logical-source-with-join-condition3/mapping.ttl", "./mapping-optimizations/same-logical-source-with-join-condition3/output.nt");
    }
}

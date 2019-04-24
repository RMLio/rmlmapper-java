package be.ugent.rml;

import org.junit.Test;

public class MappingFactoryTest extends TestCore {

    @Test
    public void invalidLanguageTags() {
        doMappingExpectError("utils/invalidLanguageTagsMapping.ttl");
    }
}
package be.ugent.rml;

import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.Quad;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.QuadStoreFactory;
import be.ugent.rml.term.Literal;
import be.ugent.rml.term.NamedNode;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static be.ugent.rml.StrictMode.BEST_EFFORT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The natural datatype of a JSON number (RML-Core), in a mapping written in the
 * vocabulary of the W3C KG-Construct CG (RMLTC0002a-JSON of kg-construct/rml-core);
 * a simple literal in a mapping of the legacy vocabulary (RMLTC0002a-JSON of
 * RMLio/rml-test-cases). The mapping is given to the Executor as it is read, as the
 * command line does.
 */
public class NaturalDatatypeTest {

    private Literal id(String mapPath) throws Exception {
        File mappingFile = Utils.getFile(mapPath);
        QuadStore rmlStore = QuadStoreFactory.read(mappingFile);
        String parentPath = mappingFile.getParent();
        Executor executor = new Executor(rmlStore, new RecordsFactory(parentPath, parentPath), null, "http://example.com/base/", BEST_EFFORT, null);
        QuadStore result = executor.execute(null).get(new NamedNode("rmlmapper://default.store"));
        List<Quad> ids = result.getQuads(null, new NamedNode("http://example.com/id"), null);
        assertEquals(1, ids.size());
        return (Literal) ids.get(0).getObject();
    }

    @Test
    public void jsonNumberInRmlCore() throws Exception {
        Literal id = id("./new-test-cases/core/RMLTC0002a-JSON/mapping.ttl");
        assertEquals("10", id.getValue());
        assertEquals(NAMESPACES.XSD + "integer", id.getDatatype().toString());
    }

    @Test
    public void jsonNumberInLegacyRml() throws Exception {
        Literal id = id("./test-cases/RMLTC0002a-JSON/mapping.ttl");
        assertEquals("10", id.getValue());
        assertEquals(NAMESPACES.XSD + "string", id.getDatatype().toString());
    }
}

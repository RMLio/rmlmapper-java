package be.ugent.rml.conformer;

import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.RDF4JStore;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Simple test cases for r2rml to rml conversion
 */
public class R2RMLConverterTest {

    /**
     * Convert r2rml mapping file and compare output rml mapping file with given
     * rml mapping file and mapping options. Show difference if needed.
     * @param input Location r2rml mapping file
     * @param output Location expected rml mapping file
     */
    private void doOptionsTest(String input, Map<String, String> options, String output, String baseIRI) throws Exception {
        ClassLoader classLoader = R2RMLConverterTest.class.getClassLoader();

        RDF4JStore actualStore = new RDF4JStore();
        actualStore.read(classLoader.getResourceAsStream(input), baseIRI, RDFFormat.TURTLE);

        MappingConformer conformer = new MappingConformer(actualStore, options);

        try {
            conformer.conform();
        } catch (Exception e) {
            e.printStackTrace();
        }

        QuadStore expectedStore = new RDF4JStore();
        expectedStore.read(classLoader.getResourceAsStream(output), baseIRI, RDFFormat.TURTLE);

        boolean isEqual = actualStore.isIsomorphic(expectedStore);
        assertTrue(isEqual);
    }

    /**
     * add logical source from given options :Database
     * @throws Exception
     */
    @Test
    public void basic() throws Exception {
        String folder = "conformer/r2rml/basic/";
        String input = folder + "mapping.r2rml.ttl";
        String output = folder + "mapping.rml.ttl";
        Map<String, String> options = new HashMap<>();
        options.put("jdbcDSN", "jdbc:mysql://localhost:1234/test");
        options.put("password", "YourSTRONG!Passw0rd;");
        options.put("username", "sa");

        doOptionsTest(input, options, output, "");
    }

    /**
     * Test if triple quoted sql query don't cause parsing issues
     * @throws Exception
     */
    @Test
    public void sqlQuery() throws Exception {
        String folder = "conformer/r2rml/sqlQuery/";
        String input = folder + "mapping.r2rml.ttl";
        String output = folder + "mapping.rml.ttl";

        Map<String, String> options = new HashMap<>();
        options.put("jdbcDSN", "jdbc:mysql://localhost:1234/test");
        options.put("password", "YourSTRONG!Passw0rd;");
        options.put("username", "sa");

        doOptionsTest(input, options, output, "http://example.com/base/");
    }
}
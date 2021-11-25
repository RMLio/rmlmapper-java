package be.ugent.rml;

import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.functions.FunctionUtils;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.RDF4JStore;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Custom_RML_FnO_Mapper_Test extends TestFunctionCore {

    @Test
    public void evaluate_A001() throws Exception {
        QuadStore functionDescriptionTriples = new RDF4JStore();
        functionDescriptionTriples.read(Utils.getInputStreamFromFile(new File("./src/test/resources/rml-fno-test-cases/functions_dynamic.ttl")), null, RDFFormat.TURTLE);
        FunctionLoader functionLoader = new FunctionLoader(functionDescriptionTriples);
        Executor executor = this.createExecutor("./rml-fno-test-cases/RMLFNOTC0001-CSV/mapping.ttl", functionLoader);
        doMapping(executor, "./rml-fno-test-cases/RMLFNOTC0001-CSV/output.ttl");
        assertTrue(functionLoader.getLibraryPath("GrelFunctions").endsWith("GrelFunctions_dynamic.jar"));
    }

    /**
     * Check if error is thrown if parameters are missing
     * @throws Exception
     */
    @Test(expected = Exception.class)
    public void evaluate_A001_missing_params() throws Exception {
        QuadStore functionDescriptionTriples = new RDF4JStore();
        functionDescriptionTriples.read(Utils.getInputStreamFromFile(new File("./src/test/resources/rml-fno-test-cases/functions_dynamic_missing_params.ttl")), null, RDFFormat.TURTLE);
        FunctionLoader functionLoader = new FunctionLoader(functionDescriptionTriples);
        Executor executor = this.createExecutor("./rml-fno-test-cases/RMLFNOTC0001-CSV/mapping.ttl", functionLoader);
        doMapping(executor, "./rml-fno-test-cases/RMLFNOTC0001-CSV/output.ttl");
        assertTrue(functionLoader.getLibraryPath("GrelFunctions").endsWith("GrelFunctions_dynamic.jar"));
    }

    @Test
    public void evaluate_A002() throws Exception {
        Executor executor = doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0001-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0001-CSV/output.ttl");
        assertEquals("__local", executor.getFunctionLoader().getLibraryPath("io.fno.grel.ArrayFunctions"));
    }

    @Test
    public void evaluate_A003() throws Exception {
        QuadStore functionDescriptionTriples = new RDF4JStore();
        functionDescriptionTriples.read(Utils.getInputStreamFromFile(new File("./src/test/resources/rml-fno-test-cases/functions_dynamic.ttl")), null, RDFFormat.TURTLE);
        FunctionLoader functionLoader = new FunctionLoader(functionDescriptionTriples);
        // You first need to execute the mapping, bc the libraryMap of loaded Jars is dynamically built
        Executor executor = this.createExecutor("./rml-fno-test-cases/RMLFNOTC0001-CSV/mapping.ttl", functionLoader);
        doMapping(executor, "./rml-fno-test-cases/RMLFNOTC0001-CSV/output.ttl");
        String libPath = functionLoader.getLibraryPath("GrelFunctions");
        Class cls = FunctionUtils.functionRequire(new File(libPath), "GrelFunctions");
        assertEquals("GrelFunctions", cls.getName());
    }

    /**
     * Tests whether the function `idlab-fn:trueCondition` is supported correctly by the mapper
     */
    @Test
    public void evaluate_A004() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTCA004/mapping.ttl", "./rml-fno-test-cases/RMLFNOTCA004/output.ttl");
    }

    @Test
    public void evaluate_A004b() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTCA004b/mapping.ttl", "./rml-fno-test-cases/RMLFNOTCA004b/output.ttl");
    }

    /**
     * Tests whether the function idlab-fn:readFile is supported correctly by the mapper
     */
    @Test
    public void evaluate_A005() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTCA005/mapping.ttl", "./rml-fno-test-cases/RMLFNOTCA005/output.ttl");
    }

    /**
     * Tests whether the function idlab-fn:inRange is supported correctly by the mapper
     */
    @Test
    public void evaluate_A006() {
        doMapping("./rml-fno-test-cases/RMLFNOTCA006/mapping.ttl", "./rml-fno-test-cases/RMLFNOTCA006/output.ttl");
    }

    /**
     * Tests whether the function idlab-fn:stringContainsOtherString is supported correctly by the mapper
     */
    @Test
    public void Evaluate_idlab_F001() {
        doMapping("./rml-fno-test-cases/RMLFNOTCF001/mapping.ttl", "./rml-fno-test-cases/RMLFNOTCF001/output.ttl");
    }

    /**
     * Tests whether the function idlab-fn:listContainsElement is supported correctly by the mapper
     */
    @Test
    public void Evaluate_idlab_F002() {
        doMapping("./rml-fno-test-cases/RMLFNOTCF002/mapping.ttl", "./rml-fno-test-cases/RMLFNOTCF002/output.ttl");
    }

    /**
     * Tests whether the function idlab-fn:dbpediaSpotlight is supported correctly by the mapper
     */
    @Test
    public void Evaluate_idlab_F003() {
        doMapping("./rml-fno-test-cases/RMLFNOTCF003/mapping.ttl", "./rml-fno-test-cases/RMLFNOTCF003/output.ttl");
    }

    /**
     * Tests whether the function idlab-fn:decide is supported correctly by the mapper
     */
    @Test
    public void Evaluate_idlab_f004() {
        doMapping("./rml-fno-test-cases/RMLFNOTCF004/mapping.ttl", "./rml-fno-test-cases/RMLFNOTCF004/output.ttl");
    }

    /**
     * Tests whether the function idlab-fn:getMIMEType is supported correctly by the mapper
     */
    @Test
    public void Evaluate_idlab_F005() {
        doMapping("./rml-fno-test-cases/RMLFNOTCF005/mapping.ttl", "./rml-fno-test-cases/RMLFNOTCF005/output.ttl");
    }

    /**
     * Tests whether the function idlab:toUpperCaseURL is supported correctly by the mapper
     */
    @Test
    public void Evaluate_idlab_F006() {
        doMapping("./rml-fno-test-cases/RMLFNOTCF006/mapping.ttl", "./rml-fno-test-cases/RMLFNOTCF006/output.ttl");

    }

    /**
     * Tests whether the function idlab-fn:slugify is supported correctly by the mapper
     */
    @Test
    public void evaluate_idlab_F007() {
        doMapping("./rml-fno-test-cases/RMLFNOTCF007/mapping.ttl", "./rml-fno-test-cases/RMLFNOTCF007/output.ttl");
    }

    /**
     * Tests whether the function idlab-fn:isNull is supported correctly by the mapper
     */
    @Test
    public void evaluate_idlab_F008() {
        doMapping("./rml-fno-test-cases/RMLFNOTCF008/mapping.ttl", "./rml-fno-test-cases/RMLFNOTCF008/output.nq");
    }

    /**
     * Tests whether the function millisecondsToInstant can be loaded and is supported correctly by the mapper
     */
    @Test
    public void evaluate_AB0001() throws Exception {
        // load the functions from a test resource jar & description file
        QuadStore functionDescriptionTriples = new RDF4JStore();
        functionDescriptionTriples.read(Utils.getInputStreamFromFile(new File("./src/test/resources/aaabimfunctions/aaabim_java_mapping.ttl")), null, RDFFormat.TURTLE);
        FunctionLoader functionLoader = new FunctionLoader(functionDescriptionTriples);

        // You first need to execute the mapping, bc the libraryMap of loaded Jars is dynamically built
        Executor executor = this.createExecutor("rml-fno-test-cases/RMLFNOTCAB0001-JSON/mapping.ttl", functionLoader);

        // execute mapping
        doMapping(executor, "rml-fno-test-cases/RMLFNOTCAB0001-JSON/output.ttl");
    }

    /**
     * Tests whether the function geoHashToLatitude can be loaded and is supported correctly by the mapper
     */
    @Test
    public void evaluate_AB0002() throws Exception {
        // load the functions from a test resource jar & description file
        QuadStore functionDescriptionTriples = new RDF4JStore();
        functionDescriptionTriples.read(Utils.getInputStreamFromFile(new File("./src/test/resources/aaabimfunctions/aaabim_java_mapping.ttl")), null, RDFFormat.TURTLE);
        FunctionLoader functionLoader = new FunctionLoader(functionDescriptionTriples);

        // You first need to execute the mapping, bc the libraryMap of loaded Jars is dynamically built
        Executor executor = this.createExecutor("rml-fno-test-cases/RMLFNOTCAB0002-JSON/mapping.ttl", functionLoader);

        // execute mapping
        doMapping(executor, "rml-fno-test-cases/RMLFNOTCAB0002-JSON/output.ttl");
    }

    /**
     * Tests whether grel:controls_if works for when the condition is true
     * @throws Exception
     */
    @Test
    public void evaluate_B0003() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTCAB0003-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTCAB0003-CSV/output.ttl");
    }

    /**
     * Tests whether grel:controls_if works for when the condition is false
     * @throws Exception
     */@Test
    public void evaluate_B0004() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTCAB0004-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTCAB0004-CSV/output.ttl");
    }

    /**
     * Tests whether grel:controls_if works for when the condition is true and no value for the "else case" is given
     * @throws Exception
     */
    @Test
    public void evaluate_B0005() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTCAB0005-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTCAB0005-CSV/output.ttl");
    }

    /**
     * Tests whether grel:controls_if works for when the condition is false and no value for the "else case" is given
     * @throws Exception
     */
    @Test
    public void evaluate_B0006() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTCAB0006-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTCAB0006-CSV/output.ttl");
    }
}

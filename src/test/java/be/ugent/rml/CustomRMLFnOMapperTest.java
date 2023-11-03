package be.ugent.rml;

import be.ugent.idlab.knows.functions.agent.Agent;
import be.ugent.idlab.knows.functions.agent.AgentFactory;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class CustomRMLFnOMapperTest extends TestFunctionCore {

    @Test
    public void evaluate_A001() throws Exception {
        Agent functionAgent = AgentFactory.createFromFnO(
                "rml-fno-test-cases/functions_dynamic.ttl",
                "functions_grel.ttl",
                "grel_java_mapping.ttl");
        Executor executor = this.createExecutor("./rml-fno-test-cases/RMLFNOTC0001-CSV/mapping.ttl", functionAgent);
        doMapping(executor, "./rml-fno-test-cases/RMLFNOTC0001-CSV/output.ttl");
    }

    /**
     * Check if error is thrown if parameters are missing
     */
    @Test
    public void evaluate_A001_missing_params() throws Exception {

        Agent functionAgent = AgentFactory.createFromFnO(
                "rml-fno-test-cases/functions_dynamic_missing_params.ttl");
        Executor executor = this.createExecutor("./rml-fno-test-cases/RMLFNOTC0001-CSV/mapping.ttl", functionAgent);
        assertThrows(Exception.class, () -> doMapping(executor, "./rml-fno-test-cases/RMLFNOTC0001-CSV/output.ttl"));
    }

    @Test
    public void evaluate_A002() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTC0001-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTC0001-CSV/output.ttl");
    }

    @Test
    public void evaluate_A003() throws Exception {
        Agent functionAgent = AgentFactory.createFromFnO(
                "rml-fno-test-cases/functions_dynamic.ttl",
                "functions_grel.ttl",
                "grel_java_mapping.ttl");
        // You first need to execute the mapping, bc the libraryMap of loaded Jars is dynamically built
        // TODO: what kind of test is this?
        Executor executor = this.createExecutor("./rml-fno-test-cases/RMLFNOTC0001-CSV/mapping.ttl", functionAgent);
        doMapping(executor, "./rml-fno-test-cases/RMLFNOTC0001-CSV/output.ttl");
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
    public void evaluate_A006() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTCA006/mapping.ttl", "./rml-fno-test-cases/RMLFNOTCA006/output.ttl");
    }

    /**
     * Tests whether the function idlab-fn:stringContainsOtherString is supported correctly by the mapper
     */
    @Test
    public void Evaluate_idlab_F001() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTCF001/mapping.ttl", "./rml-fno-test-cases/RMLFNOTCF001/output.ttl");
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
    public void Evaluate_idlab_F003() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/annotate", exchange -> {
            // response generated using
            // curl -X GET "https://api.dbpedia-spotlight.org/en/annotate?text=Barrack%20Obama" -H "accept: application/json"
            String response = "{\"@text\":\"Barrack Obama\",\"@confidence\":\"0.5\",\"@support\":\"0\",\"@types\":\"\",\"@sparql\":\"\",\"@policy\":\"whitelist\",\"Resources\":[{\"@URI\":\"http://dbpedia.org/resource/Barack_Obama\",\"@support\":\"27440\",\"@types\":\"Http://xmlns.com/foaf/0.1/Person,Wikidata:Q82955,Wikidata:Q5,Wikidata:Q24229398,Wikidata:Q215627,DUL:NaturalPerson,DUL:Agent,Schema:Person,DBpedia:Person,DBpedia:Agent,DBpedia:Politician\",\"@surfaceForm\":\"Barrack Obama\",\"@offset\":\"0\",\"@similarityScore\":\"0.9999999999951719\",\"@percentageOfSecondRank\":\"0.0\"}]}";
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });

        server.start();

        doMapping("./rml-fno-test-cases/RMLFNOTCF003/mapping.ttl", "./rml-fno-test-cases/RMLFNOTCF003/output.ttl");

        server.stop(0);
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
        doMapping("./rml-fno-test-cases/RMLFNOTCF008/mapping.rml.ttl", "./rml-fno-test-cases/RMLFNOTCF008/output.nq");
    }

    /**
     * Tests whether the function idlab-fn:normalizeDate is supported correctly by the mapper
     */
    @Test
    public void evaluate_idlab_F009() {
        doMapping("./rml-fno-test-cases/RMLFNOTCF009/mapping.ttl", "./rml-fno-test-cases/RMLFNOTCF009/output.ttl");
    }

    /**
     * Tests whether the function idlab-fn:normalizeDateWithLang is supported correctly by the mapper
     */
    @Test
    public void evaluate_idlab_F010() {
        doMapping("./rml-fno-test-cases/RMLFNOTCF010/mapping.ttl", "./rml-fno-test-cases/RMLFNOTCF010/output.ttl");
    }

    /**
     * Tests whether the function idlab-fn:normalizeDateTime is supported correctly by the mapper
     */
    @Test
    public void evaluate_idlab_F011() {
        doMapping("./rml-fno-test-cases/RMLFNOTCF011/mapping.ttl", "./rml-fno-test-cases/RMLFNOTCF011/output.ttl");
    }

    /**
     * Tests whether the function idlab-fn:normalizeDateTimeWithLang is supported correctly by the mapper
     */
    @Test
    public void evaluate_idlab_F012() {
        doMapping("./rml-fno-test-cases/RMLFNOTCF012/mapping.ttl", "./rml-fno-test-cases/RMLFNOTCF012/output.ttl");
    }

    /**
     * Tests whether the function millisecondsToInstant can be loaded and is supported correctly by the mapper
     */
    @Test
    public void evaluate_AB0001() throws Exception {
        // load the functions from a test resource jar & description file
        Agent functionAgent = AgentFactory.createFromFnO("aaabimfunctions/aaabim_java_mapping.ttl");

        // You first need to execute the mapping, bc the libraryMap of loaded Jars is dynamically built
        Executor executor = this.createExecutor("rml-fno-test-cases/RMLFNOTCAB0001-JSON/mapping.ttl", functionAgent);

        // execute mapping
        doMapping(executor, "rml-fno-test-cases/RMLFNOTCAB0001-JSON/output.ttl");
    }

    /**
     * Tests whether the function geoHashToLatitude can be loaded and is supported correctly by the mapper
     */
    @Test
    public void evaluate_AB0002() throws Exception {
        // load the functions from a test resource jar & description file
        Agent functionAgent = AgentFactory.createFromFnO("aaabimfunctions/aaabim_java_mapping.ttl");

        // You first need to execute the mapping, bc the libraryMap of loaded Jars is dynamically built
        Executor executor = this.createExecutor("rml-fno-test-cases/RMLFNOTCAB0002-JSON/mapping.ttl", functionAgent);

        // execute mapping
        doMapping(executor, "rml-fno-test-cases/RMLFNOTCAB0002-JSON/output.ttl");
    }

    /**
     * Tests whether grel:controls_if works for when the condition is true
     *
     * @throws Exception
     */
    @Test
    public void evaluate_B0003() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTCAB0003-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTCAB0003-CSV/output.ttl");
    }

    /**
     * Tests whether grel:controls_if works for when the condition is false
     *
     * @throws Exception
     */
    @Test
    public void evaluate_B0004() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTCAB0004-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTCAB0004-CSV/output.ttl");
    }

    /**
     * Tests whether grel:controls_if works for when the condition is true and no value for the "else case" is given
     *
     * @throws Exception
     */
    @Test
    public void evaluate_B0005() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTCAB0005-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTCAB0005-CSV/output.ttl");
    }

    /**
     * Tests whether grel:controls_if works for when the condition is false and no value for the "else case" is given
     *
     * @throws Exception
     */
    @Test
    public void evaluate_B0006() throws Exception {
        doPreloadMapping("./rml-fno-test-cases/RMLFNOTCAB0006-CSV/mapping.ttl", "./rml-fno-test-cases/RMLFNOTCAB0006-CSV/output.ttl");
    }
}

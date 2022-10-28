package be.ugent.rml;

import be.ugent.idlab.knows.functions.agent.Agent;
import be.ugent.idlab.knows.functions.agent.AgentFactory;
import be.ugent.rml.cli.Main;
import be.ugent.rml.conformer.MappingConformer;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.Quad;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.QuadStoreFactory;
import be.ugent.rml.target.Target;
import be.ugent.rml.target.TargetFactory;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import ch.qos.logback.classic.Level;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static be.ugent.rml.StrictMode.BEST_EFFORT;
import static org.junit.Assert.*;

@net.jcip.annotations.NotThreadSafe
public abstract class TestCore {

    final String DEFAULT_BASE_IRI = "http://example.com/base/";
    final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    // Mapping options to be applied by the MappingConformer
    protected static Map<String, String> mappingOptions = new HashMap<>();

    TestCore(){
        if(System.getenv("VERBOSE") != null){
            logger.setLevel(Level.DEBUG);
        };
    }


    /**
     *  Note: the created Executor will run in best effort mode
     */

    Executor createExecutor(String mapPath) throws Exception {
        return createExecutor(mapPath, new ArrayList<>(), null, BEST_EFFORT);
    }

    /**
     *  Note: the created Executor will run in best effort mode
     */
    Executor createExecutor(String mapPath, List<Quad> extraQuads) throws Exception {
        return createExecutor(mapPath, extraQuads, null, BEST_EFFORT);
    }

    /**
     * Create an executor and add extra quads to the mapping file.
     *
     * @param mapPath    The path to the mapping file.
     * @param extraQuads A list of extra quads that need to be added to the mapping file.
     * @param strictMode Flag to indicate whether the Executor should operate in strict mode.
     * @return An executor.
     * @throws Exception
     */
    Executor createExecutor(String mapPath, List<Quad> extraQuads, String parentPath, StrictMode strictMode) throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        // execute mapping file
        URL url = classLoader.getResource(mapPath);

        if (url != null) {
            mapPath = url.getFile();
        }

        File mappingFile = new File(mapPath);
        QuadStore rmlStore = QuadStoreFactory.read(mappingFile);

        if (parentPath == null) {
            parentPath = mappingFile.getParent();
        }

        rmlStore.addQuads(extraQuads);

        /* NOTE: this is an important step in the Main method to enable R2RML mapping.
           Ideally, this should code should be shared between the tests and the Main
           method to avoid different behavior between test code and the CLI interface! */
        convertToRml(rmlStore);

        return createExecutorWithIDLabFunctions(rmlStore, new RecordsFactory(parentPath),
                DEFAULT_BASE_IRI, strictMode);
    }

    /**
     *  Note: the created Executor will run in best effort mode
     */
    Executor createExecutorPrivateSecurityData(String mapPath, String privateSecurityDataPath) throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        // execute mapping file
        URL url1 = classLoader.getResource(mapPath);
        URL url2 = classLoader.getResource(privateSecurityDataPath);

        if (url1 != null) {
            mapPath = url1.getFile();
        }

        if (url2 != null) {
            privateSecurityDataPath = url2.getFile();
        }

        File mappingFile = new File(mapPath);
        File privateSecurityDataFile = new File(privateSecurityDataPath);
        QuadStore rmlStore = QuadStoreFactory.read(mappingFile);
        rmlStore.read(new FileInputStream(privateSecurityDataFile), null, RDFFormat.TURTLE);
        String parentPath = mappingFile.getParent();

        return createExecutorWithIDLabFunctions(rmlStore,
                new RecordsFactory(parentPath), DEFAULT_BASE_IRI, BEST_EFFORT);
    }

    Executor createExecutor(String mapPath, String parentPath, StrictMode strictMode) throws Exception {
        return createExecutor(mapPath, new ArrayList<>(), parentPath, strictMode);
    }

    /**
     *  Note: the created Executor will run in best effort mode
     */
    Executor createExecutor(String mapPath, final Agent functionAgent) throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        // execute mapping file
        File mappingFile = new File(classLoader.getResource(mapPath).getFile());
        QuadStore rmlStore = QuadStoreFactory.read(mappingFile);

        return new Executor(rmlStore, new RecordsFactory(mappingFile.getParent()), DEFAULT_BASE_IRI, BEST_EFFORT, functionAgent);
    }

    /**
     * Test function to compare output with expected files, using CLI interface
     */
    public void doMapping(Path cwd, String mappingFiles, String output, String expected) {
        Main.main(("-m " + mappingFiles + " -o " + output).split(" "), cwd.toString());
        try {
            try {
                compareFiles(
                    expected,
                    output,
                    false
                );
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }
        } finally {
            // remove output file
            try {
                File outputFile = Utils.getFile(output);
                assertTrue(outputFile.delete());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method executes a mapping, compares it to the expected out, and returns the used Executor.
     * @param mapPath The path of the mapping file.
     * @param outPath The path of the file with the expected output.
     * @return The Executor used to execute the mapping.
     */
    public Executor doMapping(String mapPath, String outPath) {
        try {
            Executor executor = this.createExecutor(mapPath);
            doMapping(executor, outPath);
            return executor;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail();
        }

        return null;
    }

    /**
     * This method executes a mapping with Targets, compares it to the expected out, and returns the used Executor.
     * @param mapPath The path of the mapping file.
     * @param outPaths The paths of the files with the expected output.
     * @param privateSecurityDataPath The path of the private security data file.
     * @return The Executor used to execute the mapping.
     */
    public Executor doMapping(String mapPath, HashMap<Term, String> outPaths, String privateSecurityDataPath) {
        try {
            Executor executor = this.createExecutorPrivateSecurityData(mapPath, privateSecurityDataPath);
            doMapping(executor, outPaths);
            return executor;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail();
        }
        return null;
    }

    /**
     * This method executes a mapping with Targets, compares it to the expected out, and returns the used Executor.
     * @param mapPath The path of the mapping file.
     * @param outPaths The paths of the files with the expected output.
     * @return The Executor used to execute the mapping.
     */
    public Executor doMapping(String mapPath, HashMap<Term, String> outPaths) {
        try {
            Executor executor = this.createExecutor(mapPath);
            doMapping(executor, outPaths);
            return executor;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail();
        }
        return null;
    }

    /**
     * This method executes a mapping, compares it to the expected out, and returns the used Executor.
     * @param mapPath The path of the mapping file.
     * @param outPath The path of the file with the expected output.
     * @param parentPath The path of the folder where the Executor looks for files, such as CSV files.
     * @param strictMode Whether the used Executor should operate in strict mode.
     * @return The Executor used to execute the mapping.
     */
    public Executor doMapping(String mapPath, String outPath, String parentPath, StrictMode strictMode) {
        try {
            Executor executor = this.createExecutor(mapPath, parentPath, strictMode);
            doMapping(executor, outPath);
            return executor;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail();
        }

        return null;
    }

    /**
     * This method executes a mapping and compares with the expected output.
     * @param executor The Executor that is used to execute the mapping.
     * @param outPath The path of the file with the expected output.
     */
    void doMapping(Executor executor, String outPath) throws Exception {
        QuadStore result = executor.execute(null).get(new NamedNode("rmlmapper://default.store"));
        result.removeDuplicates();
        compareStores(filePathToStore(outPath), result);
    }

    /**
     * This method executes a mapping and compares with the expected output of Targets.
     * @param executor The Executor that is used to execute the mapping.
     * @param outPaths The paths of the files with the expected output.
     */
    void doMapping(Executor executor, HashMap<Term, String> outPaths) throws Exception {
        logger.debug("Comparing target outputs");
        TargetFactory targetFactory = new TargetFactory("http://example.org/rules/");
        HashMap<Term, QuadStore> results = executor.execute(null);

        for (Map.Entry<Term, String> entry: outPaths.entrySet()) {
            Term target = entry.getKey();
            String outPath = entry.getValue();
            logger.debug("Target: {}", target.getValue());
            logger.debug("\tOutput path: {}", outPath);
            logger.debug("\tSize: {}", results.get(target).size());
            results.get(target).removeDuplicates();

            // Targets may have additional metadata that needs to be included such as LDES encapsulation
            if (!target.getValue().equals("rmlmapper://default.store")) {
                Target t = targetFactory.getTarget(target, executor.getRMLStore(), results.get(target));
                results.get(target).addQuads(t.getMetadata());
            }

            compareStores(filePathToStore(outPath), results.get(target));
        }
    }


    /**
     *  Note: the created Executor will run in best effort mode
     */
    void doMappingExpectError(String mapPath) {
        doMappingExpectError(mapPath, BEST_EFFORT);
    }

    /**
     * Run a test where an error in the Executor should occur.
     * @param mapPath path to the mapping file for the test
     * @param strictMode should the used Executor operate in strict mode
     */
    void doMappingExpectError(String mapPath, StrictMode strictMode) {
        ClassLoader classLoader = getClass().getClassLoader();

        File mappingFile = new File(mapPath);

        if (!mappingFile.isAbsolute()) {
            mappingFile = new File(classLoader.getResource(mapPath).getFile());
        }

        QuadStore rmlStore = null;

        // Fail the test if there is an error reading the mappingFile or converting R2RML to RML.
        try {
            /* NOTE: this is an important step in the Main method to enable R2RML mapping.
               Ideally, this should code should be shared between the tests and the Main
               method to avoid different behavior between test code and the CLI interface! */
            rmlStore = QuadStoreFactory.read(mappingFile);
            convertToRml(rmlStore);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        // Pass the test if an error occurs during mapping execution.
        try {
            Executor executor = createExecutorWithIDLabFunctions(rmlStore, new RecordsFactory(mappingFile.getParent()), DEFAULT_BASE_IRI, strictMode);
            executor.execute(null).get(new NamedNode("rmlmapper://default.store"));
        } catch (Exception e) {
            // I expected you!
            logger.warn(e.getMessage(), e);
            return;
        }

        // Fail the test if no error occurred during the execution stage.
        fail("Expecting error not found (strict mode: " + strictMode.toString() + ")");
    }

    /**
     * Converts the input to RML, if required.
     * @param store The QuadStore containing the mapping rules.
     * @throws Exception if conversion failed
     */
    private void convertToRml(QuadStore store) throws Exception {
        MappingConformer conformer = new MappingConformer(store, mappingOptions);
        try {
            boolean conversionNeeded = conformer.conform();
            if (conversionNeeded) {
                logger.info("Conversion to RML was needed.");
            }
        } catch (Exception e) {
            logger.error("Failed to make mapping file conformant to RML spec.", e);
        }
    }

    private void compareStores(QuadStore expectedStory, QuadStore resultStore) {
        String expectedString = expectedStory.toSortedString();
        String resultString = resultStore.toSortedString();
        // First arg is expected, second is actual
        assertEquals(expectedString, resultString);
    }

    void compareFiles(String expectedPath, String resultPath, boolean removeTimestamps) throws Exception {
        QuadStore expectedStore = null;
        QuadStore resultStore = null;

        expectedStore = filePathToStore(expectedPath);
        resultStore = filePathToStore(resultPath);

        if (removeTimestamps) {
            expectedStore.removeQuads(null, new NamedNode("http://www.w3.org/ns/prov#generatedAtTime"), null);
            resultStore.removeQuads(null, new NamedNode("http://www.w3.org/ns/prov#generatedAtTime"), null);
            expectedStore.removeQuads(null, new NamedNode("http://www.w3.org/ns/prov#endedAtTime"), null);
            resultStore.removeQuads(null, new NamedNode("http://www.w3.org/ns/prov#endedAtTime"), null);
            expectedStore.removeQuads(null, new NamedNode("http://www.w3.org/ns/prov#startedAtTime"), null);
            resultStore.removeQuads(null, new NamedNode("http://www.w3.org/ns/prov#startedAtTime"), null);
        }

        try {
            assertEquals(expectedStore, resultStore);
        } catch (AssertionError e) {
            compareStores(expectedStore, resultStore);
        }
    }

    private QuadStore filePathToStore(String path) throws Exception {
        // load output-turtle file
        File outputFile = null;

        try {
            outputFile = Utils.getFile(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        QuadStore store;

        if (path.endsWith(".nq")) {
            store = QuadStoreFactory.read(outputFile, RDFFormat.NQUADS);
        } else if (path.endsWith(".json") || path.endsWith(".jsonld")) {
            store = QuadStoreFactory.read(outputFile, RDFFormat.JSONLD);
        } else if (path.endsWith(".trig")) {
            store = QuadStoreFactory.read(outputFile, RDFFormat.TRIG);
        } else {
            store = QuadStoreFactory.read(outputFile);
        }

        return store;
    }

    private Executor createExecutorWithIDLabFunctions(QuadStore rmlStore, RecordsFactory recordsFactory, String baseIRI, StrictMode strictMode) throws Exception {
        Agent functionAgent = AgentFactory.createFromFnO(
                "fno/functions_idlab.ttl",
                "fno/functions_idlab_classes_java_mapping.ttl",
                "grel_java_mapping.ttl",
                "functions_grel.ttl");
        return new Executor(rmlStore, recordsFactory, null, baseIRI, strictMode, functionAgent);
    }
}

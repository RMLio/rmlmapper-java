package be.ugent.rml;

import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.RDF4JStore;
import be.ugent.rml.term.NamedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.*;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

abstract class TestCore {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    Executor createExecutor(String mapPath) throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        // execute mapping file
        URL url = classLoader.getResource(mapPath);
        if (url != null) {
            mapPath = url.getFile();
        }
        File mappingFile = new File(mapPath);
        QuadStore rmlStore = Utils.readTurtle(mappingFile);

        return new Executor(rmlStore,
                new RecordsFactory(new DataFetcher(mappingFile.getParent(), rmlStore)), Utils.getBaseDirectiveTurtle(mappingFile));
    }

    Executor createExecutor(String mapPath, FunctionLoader functionLoader) throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        // execute mapping file
        File mappingFile = new File(classLoader.getResource(mapPath).getFile());
        QuadStore rmlStore = Utils.readTurtle(mappingFile);

        return new Executor(rmlStore, new RecordsFactory(new DataFetcher(mappingFile.getParent(), rmlStore)),
                functionLoader, Utils.getBaseDirectiveTurtle(mappingFile));
    }

    Executor doMapping(String mapPath, String outPath) {
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

    void doMapping(Executor executor, String outPath) {
        try {
            QuadStore result = executor.execute(null);
            result.removeDuplicates();
            compareStores(result, filePathToStore(outPath));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            fail();
        }
    }

    void doMappingExpectError(String mapPath) {
        ClassLoader classLoader = getClass().getClassLoader();

        // execute mapping file
        File mappingFile = new File(classLoader.getResource(mapPath).getFile());
        QuadStore rmlStore = Utils.readTurtle(mappingFile);

        try {
            Executor executor = new Executor(rmlStore, new RecordsFactory(new DataFetcher(mappingFile.getParent(), rmlStore)), Utils.getBaseDirectiveTurtle(mappingFile));
            QuadStore result = executor.execute(null);
        } catch (Exception e) {
            // I expected you!
        }
    }

    private void compareStores(QuadStore store1, QuadStore store2) {
        String string1 = store1.toSortedString();
        String string2 = store2.toSortedString();

        assertEquals(string2, string1);
    }

    void compareFiles(String path1, String path2, boolean removeTimestamps) {
        RDF4JStore store1 = filePathToStore(path1);
        RDF4JStore store2 = filePathToStore(path2);

        if (removeTimestamps) {
            store1.removeQuads(null, new NamedNode("http://www.w3.org/ns/prov#generatedAtTime"), null);
            store2.removeQuads(null, new NamedNode("http://www.w3.org/ns/prov#generatedAtTime"), null);
            store1.removeQuads(null, new NamedNode("http://www.w3.org/ns/prov#endedAtTime"), null);
            store2.removeQuads(null, new NamedNode("http://www.w3.org/ns/prov#endedAtTime"), null);
            store1.removeQuads(null, new NamedNode("http://www.w3.org/ns/prov#startedAtTime"), null);
            store2.removeQuads(null, new NamedNode("http://www.w3.org/ns/prov#startedAtTime"), null);
        }

        try {
            assertEquals(store1, store2);
        } catch (AssertionError e) {
            compareStores(store1, store2);
        }
    }

    private RDF4JStore filePathToStore(String path) {
        // load output-turtle file
        File outputFile = null;
        try {
            outputFile = Utils.getFile(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        RDF4JStore store;

        if (path.endsWith(".nq")) {
            store = Utils.readTurtle(outputFile, RDFFormat.NQUADS);
        } else if (path.endsWith(".json")) {
            store = Utils.readTurtle(outputFile, RDFFormat.JSONLD);
        } else if (path.endsWith(".trig")) {
            store = Utils.readTurtle(outputFile, RDFFormat.TRIG);
        } else {
            store = Utils.readTurtle(outputFile);
        }

        return store;
    }
}

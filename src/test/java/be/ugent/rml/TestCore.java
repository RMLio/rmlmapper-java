package be.ugent.rml;

import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.QuadStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.*;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

abstract class TestCore {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    Executor createExecutor(String mapPath) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        // execute mapping file
        URL url = classLoader.getResource(mapPath);
        if (url != null) {
            mapPath = url.getFile();
        }
        File mappingFile = new File(mapPath);
        QuadStore rmlStore = Utils.readTurtle(mappingFile);

        return new Executor(rmlStore,
                new RecordsFactory(new DataFetcher(mappingFile.getParent(), rmlStore)));
    }

    Executor createExecutor(String mapPath, FunctionLoader functionLoader) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        // execute mapping file
        File mappingFile = new File(classLoader.getResource(mapPath).getFile());
        QuadStore rmlStore = Utils.readTurtle(mappingFile);

        return new Executor(rmlStore, new RecordsFactory(new DataFetcher(mappingFile.getParent(), rmlStore)),
                functionLoader);
    }

    Executor doMapping(String mapPath, String outPath) {
        try {
            Executor executor = this.createExecutor(mapPath);
            doMapping(executor, outPath);
            return executor;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            fail();
        }

        return null;
    }

    void doMapping(Executor executor, String outPath) {
        ClassLoader classLoader = getClass().getClassLoader();

        try {
            QuadStore result = executor.execute(null);
            result.removeDuplicates();

            // load output file
            File outputFile = new File(classLoader.getResource(outPath).getFile());
            QuadStore outputStore;

            if (outPath.endsWith(".nq")) {
                outputStore = Utils.readTurtle(outputFile, RDFFormat.NQUADS);
            } else {
                outputStore = Utils.readTurtle(outputFile);
            }

            assertEquals(outputStore.toSortedString(), result.toSortedString());
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
            Executor executor = new Executor(rmlStore, new RecordsFactory(new DataFetcher(mappingFile.getParent(), rmlStore)));
            QuadStore result = executor.execute(null);
        } catch (IOException e) {

        }
    }

    void compareFiles(String path1, String path2, boolean removeTimestamps) {
        ClassLoader classLoader = getClass().getClassLoader();

        // load output file 1
        File outputFile1 = new File(classLoader.getResource(path1).getFile());
        QuadStore outputStore1;

        if (path1.endsWith(".nq")) {
            outputStore1 = Utils.readTurtle(outputFile1, RDFFormat.NQUADS);
        } else {
            outputStore1 = Utils.readTurtle(outputFile1);
        }

        // load output file 2
        File outputFile2 = new File(classLoader.getResource(path2).getFile());
        QuadStore outputStore2;

        if (path1.endsWith(".nq")) {
            outputStore2 = Utils.readTurtle(outputFile2, RDFFormat.NQUADS);
        } else {
            outputStore2 = Utils.readTurtle(outputFile2);
        }

        String string1 = outputStore1.toSortedString();
        String string2 = outputStore2.toSortedString();

        if (removeTimestamps) {
            string1 = string1.replaceAll("\"[^\"]*\"\\^\\^<http://www.w3\\.org/2001/XMLSchema#dateTime>", "");
            string2 = string2.replaceAll("\"[^\"]*\"\\^\\^<http://www.w3\\.org/2001/XMLSchema#dateTime>", "");
        }

        assertEquals(string1, string2);
    }
}

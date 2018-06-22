package be.ugent.rml;

import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.QuadStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

abstract class TestCore {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    Executor createExecutor(String mapPath) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        // execute mapping file
        File mappingFile = new File(classLoader.getResource(mapPath).getFile());
        QuadStore rmlStore = Utils.readTurtle(mappingFile);

        return new Executor(rmlStore, new RecordsFactory(new DataFetcher(mappingFile.getParent(), rmlStore)));
    }

    Executor createExecutor(String mapPath, FunctionLoader functionLoader) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        // execute mapping file
        File mappingFile = new File(classLoader.getResource(mapPath).getFile());
        QuadStore rmlStore = Utils.readTurtle(mappingFile);

        return new Executor(rmlStore, new RecordsFactory(new DataFetcher(mappingFile.getParent(), rmlStore)), functionLoader);
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
}

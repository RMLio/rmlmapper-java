package be.ugent.rml;

import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.RDF4JStore;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

abstract class TestCore {

    void doMapping(String mapPath, String outPath) {
        ClassLoader classLoader = getClass().getClassLoader();

        // execute mapping file
        File mappingFile = new File(classLoader.getResource(mapPath).getFile());
        QuadStore rmlStore = this.readTurtle(mappingFile);

        Executor executor = new Executor(rmlStore, new RecordsFactory(new DataFetcher(mappingFile.getParent(), rmlStore)), new FunctionLoader());

        try {
            QuadStore result = executor.execute(null);

            // load output file
            File outputFile = new File(classLoader.getResource(outPath).getFile());
            QuadStore outputStore;

            if (outPath.endsWith(".nq")) {
                outputStore = this.readTurtle(outputFile, RDFFormat.NQUADS);
            } else {
                outputStore = this.readTurtle(outputFile);
            }
            assertEquals(outputStore.toSortedString(), result.toSortedString());
        } catch (IOException e) {
            fail();
        }
    }

    void doMappingExpectError(String mapPath) {
        ClassLoader classLoader = getClass().getClassLoader();

        // execute mapping file
        File mappingFile = new File(classLoader.getResource(mapPath).getFile());
        QuadStore rmlStore = this.readTurtle(mappingFile);

        try {
            Executor executor = new Executor(rmlStore, new RecordsFactory(new DataFetcher(mappingFile.getParent(), rmlStore)), new FunctionLoader());
            QuadStore result = executor.execute(null);
        } catch (IOException e) {

        }
    }

    private QuadStore readTurtle(File mappingFile, RDFFormat format) {
        InputStream mappingStream;
        Model model = null;
        try {
            mappingStream = new FileInputStream(mappingFile);
            //model = Rio.parse(mappingStream, "", format);

            ParserConfig config = new ParserConfig();
            config.set(BasicParserSettings.PRESERVE_BNODE_IDS, true);
            model = Rio.parse(mappingStream, "", format, config, SimpleValueFactory.getInstance(), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new RDF4JStore(model);
    }

    private QuadStore readTurtle(File mappingFile) {
        return readTurtle(mappingFile, RDFFormat.TURTLE);
    }
}

package be.ugent.rml;

import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.RDF4JStore;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

abstract class TestCore {
    void doMapping(String mapPath, String outPath) {
        ClassLoader classLoader = getClass().getClassLoader();

        // execute mapping file
        File mappingFile = new File(classLoader.getResource(mapPath).getFile());
        QuadStore rmlStore = this.readTurtle(mappingFile);

        Executor executor = new Executor(rmlStore, new RecordsFactory(new DataFetcher(mappingFile.getParent(), rmlStore)), new FunctionLoader());
        QuadStore result = executor.execute(null);

        // load output file
        File outputFile = new File(classLoader.getResource(outPath).getFile());
        QuadStore outputStore = this.readTurtle(outputFile);

        assertEquals(outputStore.toSortedString(), result.toSortedString());
    }

    private QuadStore readTurtle(File mappingFile) {
        InputStream mappingStream;
        Model model = null;
        try {
            mappingStream = new FileInputStream(mappingFile);
            model = Rio.parse(mappingStream, "", RDFFormat.TURTLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new RDF4JStore(model);
    }
}

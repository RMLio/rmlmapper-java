package be.ugent.rml.store;

import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.File;
import java.io.InputStream;

import static be.ugent.rml.Utils.getInputStreamFromFile;

public class QuadStoreFactory {

    /**
     * Read from file, default Turtle format
     * @param file
     * @return
     */
    public static QuadStore read(File file) throws Exception {
        return read(file, RDFFormat.TURTLE);
    }

    /**
     * Read from file in given format
     * @param file
     * @param format
     * @return
     */
    public static QuadStore read(File file, RDFFormat format) throws Exception {
        return read(getInputStreamFromFile(file), format);
    }

    /**
     * Read from InputStream, default Turtle format
     * @param mappingStream
     * @return
     */
    public static QuadStore read(InputStream mappingStream) throws Exception {
        return read(mappingStream, RDFFormat.TURTLE);
    }

    /**
     * Read from InputStream in given format
     * @param mappingStream
     * @param format
     * @return
     */
    public static QuadStore read(InputStream mappingStream, RDFFormat format) throws Exception {
        QuadStore store = new RDF4JStore();

        store.read(mappingStream, "", format);

        return store;
    }
}

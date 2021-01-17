package be.ugent.rml.target;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * This interface represents the target of a knowledge graph.
 * For example, a local file, a SOLID pod, a Triple Store, and so on.
 */
public interface Target {

    /**
     * This method returns an OutputStream for the target.
     * @return the OutputStream corresponding to the target.
     * @throws IOException
     */
    OutputStream getOutputStream() throws IOException;

    /**
     * This method returns the serialization format of the target.
     * @return serialization format.
     */
    String getSerializationFormat();

    /**
     * This method closes the target.
     */
    void close();
}

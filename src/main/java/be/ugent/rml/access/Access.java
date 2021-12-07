package be.ugent.rml.access;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Map;

/**
 * This interface represents the access to a data source.
 * For example, a local file, a remote file, a relational database, and so on.
 */
public interface Access {

    /**
     * This method returns an InputStream for the access.
     * @return the InputStream corresponding to the access.
     * @throws IOException
     */
    InputStream getInputStream() throws IOException, SQLException, ClassNotFoundException;

    /**
     * This method returns a map of datatypes.
     * References to values are mapped to their datatypes, if available.
     * @return map of datatypes.
     */
    Map<String, String> getDataTypes();

    String getContentType();
}

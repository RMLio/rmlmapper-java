package be.ugent.rml.access;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import static be.ugent.rml.Utils.getHashOfString;
import static be.ugent.rml.Utils.getInputStreamFromFile;
import static org.apache.commons.io.FileUtils.getFile;

/**
 * This class represents access to a local file.
 */
public class LocalFileAccess implements Access {

    private String path;
    private String basePath;
    private String compression;

    /**
     * This constructor takes the path and the base path of a file.
     * @param path the relative path of the file.
     * @param basePath the used base path.
     */
    public LocalFileAccess(String path, String basePath, String compression) {
        this.path = path;
        this.basePath = basePath;
        this.compression = compression;
    }

    /**
     * This method returns the InputStream of the local file.
     * @return an InputStream.
     * @throws IOException
     */
    @Override
    public InputStream getInputStream() throws IOException {
        // Create File and allocate InputStream
        File file = new File(this.path);
        if (!file.isAbsolute()) {
            file = getFile(this.basePath, this.path);
        }
        InputStream in = getInputStreamFromFile(file);

        // Apply compression if necessary
        if(this.compression != null) {
            switch (this.compression) {
                case COMPRESSION.ZIP:
                    in = new ZipInputStream(in);
                    break;
                case COMPRESSION.GZIP:
                    in = new GZIPInputStream(in);
                    break;
                default:
                    throw new IOException("Compression " + this.compression + " not implemented!");
            }
        }

        return in;
    }

    /**
     * This methods returns the datatypes of the file.
     * This method always returns null, because the datatypes can't be determined from a local file for the moment.
     * @return the datatypes of the file.
     */
    @Override
    public Map<String, String> getDataTypes() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof LocalFileAccess) {
            LocalFileAccess access  = (LocalFileAccess) o;
            return path.equals(access.getPath()) && basePath.equals(access.getBasePath());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getHashOfString(getPath() + getBasePath());
    }

    /**
     * This method returns the path of the access.
     * @return the relative path.
     */
    public String getPath() {
        return path;
    }

    /**
     * This method returns the base path of the access.
     * @return the base path.
     */
    public String getBasePath() {
        return basePath;
    }

    @Override
    public String toString() {
        return this.path;
    }
}

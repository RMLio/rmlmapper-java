package be.ugent.rml.target;

import be.ugent.rml.access.COMPRESSION;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.apache.commons.io.FileUtils.getFile;

/**
 * This class represents a local file as target.
 */
public class LocalFileTarget implements Target {

    private String path;
    private String basePath;
    private String serializationFormat;
    private String compression;
    private OutputStream outputStream;
    private static final Logger logger = LoggerFactory.getLogger(LocalFileTarget.class);

    /**
     * This constructor takes the path and the base path of a file.
     * @param path the relative path of the file.
     * @param basePath the used base path.
     * @param serializationFormat serialization format to use.
     * @param compression compression to apply.
     */
    public LocalFileTarget(String path, String basePath, String serializationFormat, String compression) {
        this.path = path;
        this.basePath = basePath;
        this.serializationFormat = serializationFormat;
        this.compression = compression;
    }

    /**
     * This method returns the OutputStream of the local file.
     * @return an OutputStream.
     * @throws IOException
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        // Create File and allocate OutputStream
        File file = new File(this.path);
        if (!file.isAbsolute()) {
            file = getFile(this.basePath, this.path);
        }
        this.outputStream = new FileOutputStream(file);

        // Apply compression if necessary
        if(this.compression != null) {
            switch (this.compression.toLowerCase()) {
                case COMPRESSION.GZIP:
                    this.outputStream = new GZIPOutputStream(this.outputStream);
                    break;
                default:
                    throw new IOException("Compression " + this.compression + " not implemented!");
            }
        }

        return this.outputStream;
    }

    /**
     * This method returns the serialization format of the local file.
     * @return serialization format.
     */
    @Override
    public String getSerializationFormat() {
        return serializationFormat;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof LocalFileTarget) {
            LocalFileTarget target  = (LocalFileTarget) o;
            return path.equals(target.getPath()) && basePath.equals(target.getBasePath());
        } else {
            return false;
        }
    }

    /**
     * This method returns the path of the target.
     * @return the relative path.
     */
    public String getPath() {
        return path;
    }

    /**
     * This method returns the base path of the target.
     * @return the base path.
     */
    public String getBasePath() {
        return basePath;
    }

    @Override
    public String toString() {
        return this.path;
    }

    /**
     * This method closes the target.
     */
    @Override
    public void close() {
        logger.debug("Closing target");
        try {
            this.outputStream.close();
        }
        catch (Exception e) {
            logger.error("Failed to close target: " + e);
        }
    }
}
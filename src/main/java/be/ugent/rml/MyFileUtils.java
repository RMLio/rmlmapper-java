package be.ugent.rml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

class MyFileUtils {
    private static Logger logger = LoggerFactory.getLogger(MyFileUtils.class);

    /**
     * @param resource
     * @return
     * @throws IOException
     */
    static File getResourceAsFile(String resource) throws IOException {
        logger.debug("Searching for '{}' in resources.", resource);
        ClassLoader cl = MyFileUtils.class.getClassLoader();
        URL resourceUrl = cl.getResource(resource);
        logger.debug("default class loader found '{}'", resourceUrl);
        if (resourceUrl == null) {
            throw new IOException("Resource file " + resource + " doesn't exist");
        }
        if ("file".equals(resourceUrl.getProtocol())) {
            try {

                String path = resourceUrl.toURI().getRawPath();
                logger.debug("returning file '{}'", path);
                path = URLDecoder.decode(path, StandardCharsets.UTF_8);
                return new File(path);
            } catch (URISyntaxException e) {
                throw new IOException("Unable to get file through class loader: " + cl, e);
            }

        } else {
            throw new IOException(
                    "Unable to get file through class loader: " + cl);

        }
    }

    /**
     * This method returns the path of the parent of a file.
     * @param c The class to which the file path is relative.
     * @param path The path of the file.
     * @return The path of the parent.
     */
    public static String getParentPath(Class c, String path) {
        ClassLoader classLoader = c.getClassLoader();
        URL url = classLoader.getResource(path);

        if (url != null) {
            path = url.getFile();
            path = URLDecoder.decode(path, StandardCharsets.UTF_8);
        }

        File outputFile = new File(path);

        return outputFile.getParent();
    }
}

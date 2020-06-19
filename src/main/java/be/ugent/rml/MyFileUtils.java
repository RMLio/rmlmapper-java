package be.ugent.rml;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;

class MyFileUtils {

    /**
     * @param resource
     * @return
     * @throws IOException
     */
    static File getResourceAsFile(String resource) throws IOException {
        ClassLoader cl = MyFileUtils.class.getClassLoader();
        File file;
        FileResource fileResource = new URLClassLoaderFileResource(cl, resource);
        try {
            file = fileResource.getFile();
        } catch (IOException e) {
            try {
                fileResource = new ClasspathResourceFileResource(cl, resource);
                file = fileResource.getFile();
            } catch (Exception e2) {
                throw new IOException(e2);
            }
        }
        return file;
    }

    public interface FileResource {
        File getFile() throws IOException;
    }

    public static class ClasspathResourceFileResource implements FileResource {

        private ClassLoader cl;
        private String resource;
        private String extension;

        /**
         * @param cl
         * @param resource
         */
        ClasspathResourceFileResource(ClassLoader cl, String resource) {
            this.cl = cl;
            this.resource = resource;
            this.extension = FilenameUtils.getExtension(resource);
        }

        /**
         * @return
         * @throws IOException
         */
        public File getFile() throws IOException {
            String suffix = "temp";

            if (this.extension != null) {
                suffix += "." + this.extension;
            }

            InputStream cpResource = cl.getResourceAsStream(resource);
            File tmpFile = File.createTempFile("file", suffix);
            FileUtils.copyInputStreamToFile(cpResource, tmpFile);
            tmpFile.deleteOnExit();
            return tmpFile;
        }
    }

    public static class URLClassLoaderFileResource implements FileResource {

        private ClassLoader cl;
        private String resource;

        /**
         * @param cl
         * @param resourcePath
         */
        URLClassLoaderFileResource(ClassLoader cl, String resourcePath) {
            this.cl = cl;
            this.resource = resourcePath;
        }

        /**
         * @return
         * @throws IOException
         */
        public File getFile() throws IOException {
            File resourceFile = null;
            if (cl instanceof URLClassLoader) {
                URLClassLoader urlClassLoader = URLClassLoader.class.cast(cl);
                URL resourceUrl = urlClassLoader.findResource(resource);
                if (resourceUrl == null) {
                    throw new IOException("Resource file " + resource + " doesn't exist");
                }
                if ("file".equals(resourceUrl.getProtocol())) {
                    try {

                        URI uri = resourceUrl.toURI();
                        resourceFile = new File(uri);
                    } catch (URISyntaxException e) {
                        throw new IOException("Unable to get file through class loader: " + cl, e);
                    }

                }
            }
            if (resourceFile == null) {
                throw new IOException(
                        "Unable to get file through class loader: " + cl);
            }
            return resourceFile;
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
        }

        File outputFile = new File(path);

        return outputFile.getParent();
    }
}

package be.ugent.rml.access;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static be.ugent.rml.Utils.getInputStreamFromFile;
import static org.apache.commons.io.FileUtils.getFile;

public class LocalFileAccess implements Access {

    private String path;
    private String basePath;

    public LocalFileAccess(String path, String basePath) {
        this.path = path;
        this.basePath = basePath;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return getInputStreamFromFile(getFile(this.basePath, this.path));
    }

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

    public String getPath() {
        return path;
    }

    public String getBasePath() {
        return basePath;
    }
}

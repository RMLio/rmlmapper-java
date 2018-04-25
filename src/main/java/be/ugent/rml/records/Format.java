package be.ugent.rml.records;

import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class Format {

    public List<Record> get(String path) throws IOException {
        return get(path, System.getProperty("user.dir"));
    }

    public List<Record> get(String path, String cwd) throws IOException {
        File file = new File(path);

        if (!file.isAbsolute()) {
            path = cwd + "/" + path;
        }

        return _get(path);
    }

    abstract List<Record> _get(String path) throws IOException;
}

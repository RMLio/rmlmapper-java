package be.ugent.rml.records;

import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class IteratorFormat {

    public List<Record> get(String path, String iterator) throws IOException {
        return get(path, iterator, System.getProperty("user.dir"));
    }

    public List<Record> get(String path, String iterator, String cwd) throws IOException {
        File file = new File(path);

        if (!file.isAbsolute()) {
            path = cwd + "/" + path;
        }

        return _get(path, iterator);
    }

    abstract List<Record> _get(String path, String iterator) throws IOException;
}

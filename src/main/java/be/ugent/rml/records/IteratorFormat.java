package be.ugent.rml.records;

import be.ugent.rml.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class IteratorFormat {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<Record> get(String location, String iterator) throws IOException {
        return get(location, iterator, System.getProperty("user.dir"));
    }

    public List<Record> get(String location, String iterator, String cwd) throws IOException {
        File file = Utils.getFile(location, new File(cwd));

        return _get(file, iterator);
    }

    abstract List<Record> _get(File file, String iterator) throws IOException;
}

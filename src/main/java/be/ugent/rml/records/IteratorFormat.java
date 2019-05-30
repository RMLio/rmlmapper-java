package be.ugent.rml.records;

import be.ugent.rml.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public abstract class IteratorFormat {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<Record> get(String location, String iterator) throws IOException {
        return get(location, iterator, System.getProperty("user.dir"));
    }

    public List<Record> get(String location, String iterator, String cwd) throws IOException {
        InputStream stream = Utils.getInputStreamFromLocation(location, new File(cwd), getContentType());

        return _get(stream, iterator);
    }

    public List<Record> get(InputStream stream, String iterator) throws IOException {
        return _get(stream, iterator);
    }    
    
    abstract List<Record> _get(InputStream stream, String iterator) throws IOException;

    abstract String getContentType();
}

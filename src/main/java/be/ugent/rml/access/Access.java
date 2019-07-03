package be.ugent.rml.access;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface Access {

    InputStream getInputStream() throws IOException;

    Map<String, String> getDataTypes();
}

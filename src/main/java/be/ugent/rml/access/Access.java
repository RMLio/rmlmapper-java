package be.ugent.rml.access;

import java.io.IOException;
import java.io.InputStream;

public interface Access {

    InputStream getInputStream() throws IOException;
}

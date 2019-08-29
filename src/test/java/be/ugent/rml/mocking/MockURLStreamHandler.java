package be.ugent.rml.mocking;

import java.io.IOException;
import java.net.*;
import java.util.Map;

public class MockURLStreamHandler extends URLStreamHandler implements URLStreamHandlerFactory {
    private URLConnection mConnection;
    private Map<String, String> resourcePathMap;

    public MockURLStreamHandler(Map<String, String> resourcePathMap) {
        super();
        this.resourcePathMap = resourcePathMap;
    }


    public URLConnection getConnection() {
        return mConnection;
    }

    // *** URLStreamHandler

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        String path = resourcePathMap.get(u.toString());
        if (path == null) {
            throw new IOException("No mocked resource for: " + u.toString());
        }
        mConnection = new MockHttpURLConnection(u, path);
        return mConnection;
    }

    // *** URLStreamHandlerFactory

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        return this;
    }

}
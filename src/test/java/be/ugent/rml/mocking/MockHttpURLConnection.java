package be.ugent.rml.mocking;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MockHttpURLConnection extends HttpURLConnection {
    private String resourcePath;

    MockHttpURLConnection(URL url, String resourcePath) {
        super(url);
        this.resourcePath = resourcePath;
    }

    // *** HttpURLConnection

    @Override
    public InputStream getInputStream() {
        // Load file in res/raw/users.json of the test project
        return getClass().getClassLoader().getResourceAsStream(resourcePath);
    }

    @Override
    public void connect() {
    }

    @Override
    public void disconnect() {
    }

    @Override
    public boolean usingProxy() {
        return false;
    }

}
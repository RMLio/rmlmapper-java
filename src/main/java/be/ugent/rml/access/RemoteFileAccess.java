package be.ugent.rml.access;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import static be.ugent.rml.Utils.getInputStreamFromURL;

public class RemoteFileAccess implements Access {

    private String location;
    private String contentType;

    public RemoteFileAccess(String location) {
        this(location, "");
    }

    public RemoteFileAccess(String location, String contentType) {
        this.location = location;
        this.contentType = contentType;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return getInputStreamFromURL(new URL(location), contentType);
    }

    @Override
    public Map<String, String> getDataTypes() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof RemoteFileAccess) {
            RemoteFileAccess access  = (RemoteFileAccess) o;
            return location.equals(access.getLocation()) && contentType.equals(access.getContentType());
        } else {
            return false;
        }
    }

    public String getLocation() {
        return location;
    }

    public String getContentType() {
        return contentType;
    }
}

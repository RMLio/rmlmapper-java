package be.ugent.rml.access;

import be.ugent.rml.Utils;
import org.apache.jena.tdb.store.Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static be.ugent.rml.Utils.getHashOfString;
import static be.ugent.rml.Utils.getInputStreamFromURL;

public class WoTAccess implements Access {

    private static final Logger logger = LoggerFactory.getLogger(WoTAccess.class);
    private String location;
    private String contentType;
    private HashMap<String, String> headers;

    /**
     * This constructor of WoTAccess taking location and content type as arguments.
     * @param location the location of the WoT Thing.
     * @param contentType the content type of the WoT Thing.
     */
    public WoTAccess (String location, String contentType, HashMap<String, String> headers) {
        this.location = location;
        this.contentType = contentType;
        this.headers = headers;
        logger.debug("Created WoTAccess:\n\tlocation:" + this.location + "\n\tcontent-type:" + this.contentType);
        logger.debug(headers.toString());
        headers.forEach((name, value) -> {
            logger.debug("Header: " + name + ": " + value);
        });
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return getInputStreamFromURL(new URL(location), contentType, headers);
    }

    /**
     * This methods returns the datatypes of the WoT Thing.
     * This method always returns null, because the datatypes can't be determined from a WoT Thing for the moment.
     * @return the datatypes of the file.
     */
    @Override
    public Map<String, String> getDataTypes() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof WoTAccess) {
            WoTAccess access  = (WoTAccess) o;
            return location.equals(access.getLocation()) && contentType.equals(access.getContentType());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getHashOfString(getLocation() + getContentType());
    }

    /**
     * The method returns the location of the remote file.
     * @return the location.
     */
    public String getLocation() {
        return location;
    }

    /**
     * This method returns the content type of the remote file.
     * @return the content type.
     */
    public String getContentType() {
        return contentType;
    }
}
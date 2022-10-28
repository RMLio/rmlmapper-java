package be.ugent.rml.access;

import com.jayway.jsonpath.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static be.ugent.rml.Utils.*;


public class WoTAccess implements Access {

    private static final Logger logger = LoggerFactory.getLogger(WoTAccess.class);
    private final HashMap<String, HashMap<String, String>> auth;
    private String location;
    private String contentType;
    private HashMap<String, String> headers;

    /**
     * This constructor of WoTAccess taking location and content type as arguments.
     * @param location the location of the WoT Thing.
     * @param contentType the content type of the WoT Thing.
     */
    public WoTAccess (String location, String contentType, HashMap<String, String> headers, HashMap<String, HashMap<String, String>> auth) {
        this.location = location;
        this.contentType = contentType;
        this.headers = headers;
        this.auth = auth;

        logger.debug("Created WoTAccess:\n\tlocation: {}\n\tcontent-type: {}", this.location, this.contentType);
        logger.debug(headers.toString());
        headers.forEach((name, value) -> {
            logger.debug("Header: {} : {}", name, value);
        });
    }

    @Override
    public InputStream getInputStream() throws IOException {
        logger.debug("get inputstream");
        InputStream response ;

        if(auth.get("data").containsKey("refresh")){
            try{
                response = getInputStreamFromAuthURL(new URL(location), contentType, headers);
            } catch (Exception e){
                logger.debug("Refresh token");
                refreshToken();
                logger.debug("try again with new token");
                logger.debug("new token = {}", this.headers.get(this.auth.get("info").get("name")));
                return getInputStreamFromURL(new URL(location), contentType, headers);
            }
        } else {
            response = getInputStreamFromURL(new URL(location), contentType, headers);
        }
        return response;
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

    public void refreshToken() throws MalformedURLException {

        StringBuilder data = new StringBuilder();
        data.append("{\"grant_type\": \"refresh_token\"");
        for(String name: auth.get("data").keySet()) {
            data.append(" ,\"").append(name).append("\":\"").append(auth.get("data").get(name)).append("\"");
        }
        data.append("}");
        logger.debug(data.toString());
        InputStream response = getPostRequestResponse(new URL(auth.get("info").get("authorization")), contentType, data.toString().getBytes());
        HashMap<String, String> jsonResponse = (HashMap<String, String>) Configuration.defaultConfiguration().jsonProvider().parse(response, "utf-8");
        this.headers.put(auth.get("info").get("name"), "Bearer " + jsonResponse.get("access_token"));
    }
}
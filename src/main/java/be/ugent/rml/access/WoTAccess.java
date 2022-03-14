package be.ugent.rml.access;

import net.minidev.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.thrift.protocol.TJSONProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static be.ugent.rml.Utils.getHashOfString;
import static be.ugent.rml.Utils.getInputStreamFromURL;


public class WoTAccess implements Access {

    private static final Logger logger = LoggerFactory.getLogger(WoTAccess.class);
    private final HashMap<String, String> auth;
    private String location;
    private String contentType;
    private HashMap<String, String> headers;

    /**
     * This constructor of WoTAccess taking location and content type as arguments.
     * @param location the location of the WoT Thing.
     * @param contentType the content type of the WoT Thing.
     */
    public WoTAccess (String location, String contentType, HashMap<String, String> headers, HashMap<String, String> auth) {
        this.location = location;
        this.contentType = contentType;
        this.headers = headers;
        this.auth = auth;

        logger.debug("Created WoTAccess:\n\tlocation:" + this.location + "\n\tcontent-type:" + this.contentType);
        logger.debug(headers.toString());
        headers.forEach((name, value) -> {
            logger.debug("Header: " + name + ": " + value);
        });
    }

    @Override
    public InputStream getInputStream() throws IOException {
        InputStream response = getInputStreamFromURL(new URL(location), contentType, headers);
        if(response == null && auth.containsKey("refresh")){
            refreshToken();
            return getInputStreamFromURL(new URL(location), contentType, headers);
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

    public void refreshToken() throws UnsupportedEncodingException {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

            HttpPost httpPost = new HttpPost(location);
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("refresh_token", auth.get("refresh")));
            nvps.add(new BasicNameValuePair("grant_type", "refresh_token"));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));

            try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
                HttpEntity entity = response.getEntity();
                //TODO krijg new token uit response body
//                String new_token =
//                headers.put(auth.get("name"), new_token);

                EntityUtils.consume(entity);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
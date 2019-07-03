package be.ugent.rml.access;

import be.ugent.rml.Utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SPARQLEndpointAccess implements Access {

    private String contentType;
    private String endpoint;
    private String query;

    public SPARQLEndpointAccess(String contentType, String endpoint, String query) {
        this.contentType = contentType;
        this.endpoint = endpoint;
        this.query = query;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        // Query the endpoint
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Set 'Accept' header
        connection.setRequestProperty("Accept", contentType);

        // Set 'query' parameter
        Map<String, String> urlParams = new HashMap<String, String>() {{
            put("query", query);
        }};

        connection.setDoOutput(true);
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        out.writeBytes(Utils.getURLParamsString(urlParams));
        out.flush();
        out.close();

        // TODO check this code
        int status = connection.getResponseCode();

        return connection.getInputStream();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SPARQLEndpointAccess) {
            SPARQLEndpointAccess access  = (SPARQLEndpointAccess) o;
            return endpoint.equals(access.getEndpoint()) && contentType.equals(access.getContentType()) && query.equals(access.getQuery());
        } else {
            return false;
        }
    }

    @Override
    public Map<String, String> getDataTypes() {
        return null;
    }

    public String getContentType() {
        return contentType;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getQuery() {
        return query;
    }
}

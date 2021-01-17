package be.ugent.rml.target;

import org.apache.http.client.HttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class SPARQLEndpointTarget implements Target {

    private final String url;
    private final File tempFile;
    private static final Logger logger = LoggerFactory.getLogger(SPARQLEndpointTarget.class);
    private static final int BUFFER_SIZE = 8192;
    private static final String serializationFormat = "ntriples";

    /**
     * This constructor takes an URL of the SPARQL endpoint as argument.
     * @param url URL of the SPARQL endpoint
     */
    public SPARQLEndpointTarget(String url) throws IOException {
        this.url = url;
        this.tempFile = File.createTempFile("rmlmapper-", ".nt");
        this.tempFile.deleteOnExit();
    }

    /**
     * This method returns an OutputStream for the target.
     * @return the OutputStream corresponding to the target.
     * @throws IOException
     */
    public OutputStream getOutputStream() throws IOException {
        return new FileOutputStream(this.tempFile);
    }

    /**
     * This method returns the serialization format of the target.
     * @return serialization format.
     */
    @Override
    public String getSerializationFormat() {
        return this.serializationFormat;
    }

    /**
     * This method returns the url of the SPARQL endpoint target.
     * @return url.
     */
    public String getUrl() {
        return this.url;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SPARQLEndpointTarget) {
            SPARQLEndpointTarget target  = (SPARQLEndpointTarget) o;
            return this.url.equals(target.getUrl());
        } else {
            return false;
        }
    }

    /**
     * This method closes the target.
     * When closing a SPARQL endpoint target, the SPARQL UPDATE query is executed
     * to update the SPARQL triple store with the exported triples.
     */
    @Override
    public void close() {
        /*
        Read the temporary file containing the exported triples.
        Create a SPARQL UPDATE query
        Push the triples to a SPARQL store using the SPARQL UPDATE query.

        TODO: Named graph support
        */
        logger.debug("Closing target");

        try {
            // Create HTTP connection to SPARQL triple store
            URL url = new URL(this.url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/sparql-update");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // Create InputStream to read temporary file and OutputStream to write to connection
            File turtle = new File(this.tempFile.getAbsolutePath());
            InputStream in = new FileInputStream(turtle);
            OutputStream out = connection.getOutputStream();

            // SPARQL UPDATE Query starts with 'INSERT DATA {'
            out.write("INSERT DATA {".getBytes(StandardCharsets.UTF_8));

            // Java 9 has .transferTo() to copy an InputStream into an OutputStream, instead of this:
            byte[] buffer = new byte[BUFFER_SIZE];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            // Query ends with '}'
            out.write("}".getBytes(StandardCharsets.UTF_8));

            // Throw error if query failed (HTTP status code >= 300)
            int code = connection.getResponseCode();
            if (code >= HttpURLConnection.HTTP_MULT_CHOICE) {
                throw new HttpResponseException(code, "Executing SPARQL UPDATE query failed (" + code + ")");
            }

            // Close streams
            in.close();
            out.close();
        }
        catch (Exception e) {
            logger.error("Failed to close target: " + e);
        }
    }
}
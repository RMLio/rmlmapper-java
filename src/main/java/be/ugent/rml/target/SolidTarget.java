package be.ugent.rml.target;

import be.ugent.rml.store.Quad;
import org.apache.http.client.HttpResponseException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public abstract class SolidTarget implements Target {

    protected final Map<String,Object> solidTargetInfo;
    private final List<Quad> metadata;
    private String solidHelperUrl;
    private boolean solidHelperDocker;
    protected String solidHelperPath;
    private ByteArrayOutputStream byteArrayOutputStream;
    private static final Logger logger = LoggerFactory.getLogger(SolidTarget.class);
    private final String serializationFormat;


    private static final Map<String, String> serializationFormats = Map.of(
            "ntriples","application/n-triples",
            "turtle", "text/turtle",
            "jsonld", "application/ld+json",
            "trix", "text/xml",
            "trig", "application/trig",
            "nquads", "application/n-quads"
    ); // TODO hdt???

    /**
     * This constructor takes a JSON object with the solid target info, the serialization format and the metadata as argument.
     * @param solidTargetInfo JSON object with all the target info (resource url and authentication info)
     * @param serializationFormat String with the serialization format
     * @param metadata a list of Quads containing metadata
     */
    public SolidTarget(Map<String, Object> solidTargetInfo, String serializationFormat, List<Quad> metadata) throws IOException {
        this.solidTargetInfo = solidTargetInfo;
        this.metadata = metadata;
        this.serializationFormat = serializationFormat;
        byteArrayOutputStream = new ByteArrayOutputStream();
    }
    public void setSolidHelperUrl(String solidHelperUrl){
        this.solidHelperUrl = solidHelperUrl;
    }

    public void setSolidHelperDocker(boolean solidHelperDocker){
        this.solidHelperDocker = solidHelperDocker;
    }

    /**
     * This method returns an OutputStream for the target.
     * @return the OutputStream corresponding to the target.
     * @throws IOException
     */
    public OutputStream getOutputStream() throws IOException {
        return byteArrayOutputStream;
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
     * This method returns the url of the Solid pod target //TODO adapt
     * @return url.
     */
    public Map<String, Object> getSolidTargetInfo() {
        return this.solidTargetInfo;
    }

    /**
     * This method closes the target.
     * When closing a Solid target, the tempFile is put on the solid pod
     * with a PUT method.
     */
    @Override
    public void close() {
        /*
        Read the temporary file containing the exported triples.
        And send the output to the solid put with an authorised fetch.
        */
        GenericContainer<?> container = null;
        logger.debug("Closing target");
        try {
            if (solidHelperDocker) {
                //TODO move the docker image to an imec account
                container = new GenericContainer<>(DockerImageName.parse("elsdvlee/solid-target-helper:latest"))
                        .withExposedPorts(8080)
                        .withCommand("npm","start")
                        .waitingFor(Wait.forHealthcheck());
                container.start();
                solidHelperUrl = "http://" + container.getHost() + ":" + container.getMappedPort(8080) + "/";
            }
            // Create HTTP connection to Solid app that can handle authentication and fetch to solid pod
            // Workaround to avoid the need to use javascript libraries in java
            // The url can be adapted via the cli option -shu or --solidHelperUrl
            // The default value is "http://localhost:8080/"

            // differentiate between ldp:Resource and acl via class
            URL url = new URL(solidHelperUrl + solidHelperPath);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            OutputStream out = connection.getOutputStream();
            this.solidTargetInfo.put("data", this.byteArrayOutputStream.toString(StandardCharsets.UTF_8));
            // reset the outputstream to empty memory
            this.byteArrayOutputStream.reset();
            this.solidTargetInfo.put("contentType", serializationFormats.get(this.serializationFormat));
            // use JSONObject to escape all special characters
            JSONObject jsonObject = new JSONObject(this.solidTargetInfo);
            out.write((jsonObject.toString()).getBytes(StandardCharsets.UTF_8));

            // Close out stream
            out.close();

            // Throw error if query failed (HTTP status code >= 300)
            int code = connection.getResponseCode();
            if (code >= HttpURLConnection.HTTP_MULT_CHOICE) {
                throw new HttpResponseException(code, "Executing Authenticated Fetch to for " + solidHelperPath + " to " + this.solidTargetInfo.get("resourceUrl") + " failed (" + code + ")");
            }
        }
        catch (Exception e) {
            logger.error("Failed to close target for {} to {}= {}", solidHelperPath, this.solidTargetInfo.get("resourceUrl"), e.getMessage());
        }
        finally {
            if(container != null) {
                container.stop();
            }
        }
    }

    @Override
    public List<Quad> getMetadata() {
        return this.metadata;
    }
}

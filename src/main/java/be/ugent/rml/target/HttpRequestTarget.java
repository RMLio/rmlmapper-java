package be.ugent.rml.target;

import be.ugent.rml.store.Quad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public abstract class HttpRequestTarget implements Target {

    protected final Map<String, String> httpRequestInfo;
    protected final Map<String, String> httpRequestHeaders;
    private final List<Quad> metadata;
    private final ByteArrayOutputStream byteArrayOutputStream;
    protected final Logger logger;
    private final String serializationFormat;
    protected HttpRequestTargetHelper httpRequestTargetHelper;

    private static final Map<String, String> serializationFormats = Map.of(
            "ntriples","application/n-triples",
            "turtle", "text/turtle",
            "jsonld", "application/ld+json",
            "trix", "text/xml",
            "trig", "application/trig",
            "nquads", "application/n-quads"
    );

    /**
     * This constructor takes a map with the http request info, a map with the http request headers, the serialization format and the metadata as argument.
     * @param httpRequestInfo Map with all the target info
     * @param httpRequestHeaders Map with HTTP request headers
     * @param serializationFormat String with the serialization format
     * @param metadata a list of Quads containing metadata
     */
    public HttpRequestTarget(Map<String, String> httpRequestInfo, Map<String, String> httpRequestHeaders, String serializationFormat, List<Quad> metadata,
                             HttpRequestTargetHelper httpRequestTargetHelper) {
        this.httpRequestInfo = httpRequestInfo;
        this.httpRequestHeaders = httpRequestHeaders;
        this.metadata = metadata;
        this.serializationFormat = serializationFormat;
        byteArrayOutputStream = new ByteArrayOutputStream();
        this.logger = LoggerFactory.getLogger(HttpRequestTarget.class);
        this.httpRequestTargetHelper = httpRequestTargetHelper;
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
     * This method returns the http request info
     * @return map with http request info.
     */
    public Map<String, String> getHttpRequestInfo() {
        return this.httpRequestInfo;
    }

    /**
     * This method closes the target.
     * When closing a http request target, the content of the tempFile is added to the http request as body
     * and the http request is executed.
     */
    @Override
    public void close() {
        /*
        Read the temporary file containing the exported triples.
        The rest of the method is handled in the subclass
        */
        logger.debug("Closing target");
        this.httpRequestInfo.put("data", this.byteArrayOutputStream.toString(StandardCharsets.UTF_8));
        // reset the output stream to empty memory
        this.byteArrayOutputStream.reset();
        // add default values
        if (!httpRequestInfo.containsKey("methodName")){
            this.httpRequestInfo.put("methodName", HttpMethod.PUT.name());
        }
        if (!httpRequestHeaders.containsKey("content-type")){
            this.httpRequestHeaders.put("content-type", serializationFormats.get(this.serializationFormat));
        }
    }

    @Override
    public List<Quad> getMetadata() {
        return this.metadata;
    }
}

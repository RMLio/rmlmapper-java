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

public abstract class SolidTarget implements Target {

    protected final Map<String, String> solidTargetInfo;
    private final List<Quad> metadata;
    protected String solidHelperPath;
    private final ByteArrayOutputStream byteArrayOutputStream;
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
    public SolidTarget(Map<String, String> solidTargetInfo, String serializationFormat, List<Quad> metadata) {
        this.solidTargetInfo = solidTargetInfo;
        this.metadata = metadata;
        this.serializationFormat = serializationFormat;
        byteArrayOutputStream = new ByteArrayOutputStream();
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
    public Map<String, String> getSolidTargetInfo() {
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
        logger.debug("Closing target");
        try {
            this.solidTargetInfo.put("data", this.byteArrayOutputStream.toString(StandardCharsets.UTF_8));
            // reset the outputstream to empty memory
            this.byteArrayOutputStream.reset();
            this.solidTargetInfo.put("contentType", serializationFormats.get(this.serializationFormat));
            SolidTargetHelper helper = new SolidTargetHelper();
            helper.send(solidTargetInfo);
        }
        catch (Exception e) {
            logger.error("Failed to close target for {} to {}= {}", solidHelperPath, this.solidTargetInfo.get("resourceUrl"), e.getMessage());
        }
    }

    @Override
    public List<Quad> getMetadata() {
        return this.metadata;
    }
}

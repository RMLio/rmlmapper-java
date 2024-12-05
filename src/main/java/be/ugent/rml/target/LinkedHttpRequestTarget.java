package be.ugent.rml.target;

import be.ugent.rml.store.Quad;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public class LinkedHttpRequestTarget extends HttpRequestTarget {

    public LinkedHttpRequestTarget(Map<String, String> httpRequestInfo, String serializationFormat, List<Quad> metadata) throws IOException {
        super(httpRequestInfo, serializationFormat, metadata);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof LinkedHttpRequestTarget) {
            LinkedHttpRequestTarget target  = (LinkedHttpRequestTarget) o;
            return this.httpRequestInfo.get("linkingAbsoluteURI").equals(target.getHttpRequestInfo().get("linkingAbsoluteURI")) &
                    this.httpRequestInfo.get("linkRelation").equals(target.getHttpRequestInfo().get("linkRelation"));
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "resource linked to " + this.httpRequestInfo.get("linkingAbsoluteURI")
                + " with link relation " + this.httpRequestInfo.get("linkRelation");
    }

    @Override
    public void close() {
        super.close();
        try {
            HttpRequestTargetHelper helper = new HttpRequestTargetHelper();
            helper.executeLinkedHttpRequest(httpRequestInfo);
        } catch (Exception e) {
            logger.error("Failed to close http request target linked to {} with link relation {}: {}",
                    this.httpRequestInfo.get("linkingAbsoluteURI"), this.httpRequestInfo.get("linkRelation"), e.getMessage());
        }
    }
}


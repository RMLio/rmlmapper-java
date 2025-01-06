package be.ugent.rml.target;

import be.ugent.rml.store.Quad;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public class DirectHttpRequestTarget extends HttpRequestTarget {

    public DirectHttpRequestTarget(Map<String, String> httpRequestInfo, String serializationFormat, List<Quad> metadata, HttpRequestTargetHelper httpRequestTargetHelper) throws IOException {
        super(httpRequestInfo, serializationFormat, metadata, httpRequestTargetHelper);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DirectHttpRequestTarget) {
            DirectHttpRequestTarget target  = (DirectHttpRequestTarget) o;
            return this.httpRequestInfo.get("absoluteURI").equals(target.getHttpRequestInfo().get("absoluteURI"));
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "http request for " + this.httpRequestInfo.get("absoluteURI").toString();
    }

    @Override
    public void close() {
        super.close();
        try {
            this.httpRequestTargetHelper.executeHttpRequest(httpRequestInfo);
        } catch (Exception e) {
            logger.error("Failed to close http request target for {}: {}",
                    this.httpRequestInfo.get("absoluteURI"), e.getMessage());
        }
    }
}


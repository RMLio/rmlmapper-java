package be.ugent.rml.target;

import be.ugent.rml.store.Quad;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public class SolidResourceTarget extends SolidTarget {

    public SolidResourceTarget(Map<String, String> solidTargetInfo, String serializationFormat, List<Quad> metadata) throws IOException {
        super(solidTargetInfo, serializationFormat, metadata);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SolidResourceTarget) {
            SolidResourceTarget target  = (SolidResourceTarget) o;
            return this.solidTargetInfo.get("resourceUrl").equals(target.getSolidTargetInfo().get("resourceUrl"));
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "resource for " + this.solidTargetInfo.get("resourceUrl").toString();
    }

    @Override
    public void close() {
        super.close();
        try {
            SolidTargetHelper helper = new SolidTargetHelper();
            helper.addResource(solidTargetInfo);
        } catch (Exception e) {
            logger.error("Failed to close Solid resource target for {}: {}", this.solidTargetInfo.get("resourceUrl"), e.getMessage());
        }
    }
}


package be.ugent.rml.target;

import be.ugent.rml.store.Quad;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public class SolidResourceTarget extends SolidTarget {

    public SolidResourceTarget(Map<String, Object> solidTargetInfo, String serializationFormat, List<Quad> metadata) throws IOException {
        super(solidTargetInfo, serializationFormat, metadata);
        solidHelperPath = "addResource";
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
        return this.solidTargetInfo.get("resourceUrl").toString();
    }
}


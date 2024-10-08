package be.ugent.rml.target;

import be.ugent.rml.store.Quad;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;


public class SolidResourceTarget extends SolidTarget {

    public SolidResourceTarget(JSONObject solidTargetInfo, String serializationFormat, List<Quad> metadata) throws IOException {
        super(solidTargetInfo, serializationFormat, metadata);
        solidHelperPath = "addResource";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof SolidResourceTarget) {
            SolidResourceTarget target  = (SolidResourceTarget) o;
            return this.solidTargetInfo.get("resourceUrl").equals(target.getSolidTargetInfo().get("resourceUrl"));
        } else {
            return false;
        }
    }
}


package be.ugent.rml.target;

import be.ugent.rml.store.Quad;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;


public class SolidAclTarget extends SolidTarget {

    public SolidAclTarget(JSONObject solidTargetInfo, String serializationFormat, List<Quad> metadata) throws IOException {
        super(solidTargetInfo, serializationFormat, metadata);
        solidHelperPath = "addAcl";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof SolidAclTarget) {
            SolidAclTarget target  = (SolidAclTarget) o;
            return this.solidTargetInfo.get("resourceUrl").equals(target.getSolidTargetInfo().get("resourceUrl"));
        } else {
            return false;
        }
    }
}


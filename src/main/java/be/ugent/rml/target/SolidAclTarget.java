package be.ugent.rml.target;

import be.ugent.rml.store.Quad;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public class SolidAclTarget extends SolidTarget {

    public SolidAclTarget(Map<String, String> solidTargetInfo, String serializationFormat, List<Quad> metadata) throws IOException {
        super(solidTargetInfo, serializationFormat, metadata);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SolidAclTarget) {
            SolidAclTarget target  = (SolidAclTarget) o;
            return this.solidTargetInfo.get("resourceUrl").equals(target.getSolidTargetInfo().get("resourceUrl"));
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "acl for " + this.solidTargetInfo.get("resourceUrl");
    }

    @Override
    public void close() {
        super.close();
        try {
            SolidTargetHelper helper = new SolidTargetHelper();
            helper.addAcl(solidTargetInfo);
        } catch (Exception e) {
            logger.error("Failed to close Solid ACL target for {}: {}", this.solidTargetInfo.get("resourceUrl"), e.getMessage());
        }
    }
}


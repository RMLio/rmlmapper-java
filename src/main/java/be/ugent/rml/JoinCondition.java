package be.ugent.rml;

import java.util.List;

public class JoinCondition {

    private List<Element> parent, child;

    public JoinCondition(List<Element> parent, List<Element> child) {
        this.parent = parent;
        this.child = child;
    }

    public List<Element> getParent() {
        return parent;
    }

    public List<Element> getChild() {
        return child;
    }
}

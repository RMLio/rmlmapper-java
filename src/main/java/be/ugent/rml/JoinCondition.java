package be.ugent.rml;

public class JoinCondition {

    private Template parent, child;

    public JoinCondition(Template parent, Template child) {
        this.parent = parent;
        this.child = child;
    }

    public Template getParent() {
        return parent;
    }

    public Template getChild() {
        return child;
    }
}

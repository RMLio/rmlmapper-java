package be.ugent.rml.term;

import be.ugent.rml.Utils;

public class BlankNode extends AbstractTerm {

    public BlankNode(String suffix) {
        super(suffix);
    }

    public BlankNode() { super(Utils.randomString(10)); }

    @Override
    public String toString() {
        return "_:" + this.getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BlankNode) {
            return o.toString().equals(toString());
        } else {
            return false;
        }
    }
}
